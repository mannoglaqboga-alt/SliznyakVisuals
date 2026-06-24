package moscow.elegant.protection.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import moscow.elegant.elegant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OperatingSystem;

/**
 * AuthManager — обработка HWID авторизации.
 *
 * Изменения для обработки бана:
 * - Разделены случаи:
 *   1. explicit {"allowed": false, ...} от сервера → БАН → красное сообщение + краш через 1-2 сек.
 *   2. Любая сетевая ошибка / сервер недоступен → FAIL-OPEN: просто логируем, игра разрешена (не крашим).
 * - Ожидание ответа синхронное для главного потока через CountDownLatch (waitForResult).
 * - Краш выполняется через MinecraftClient.getInstance().execute(...) + задержку + mc.stop().
 * - Добавлен enforceServerBan(), который вызывается в главном потоке после ожидания.
 * - Добавлены флаги banned / banReason.
 * - Все действия с UI (sendMessage, stop) идут через execute(), чтобы быть в правильном потоке.
 */
public final class AuthManager {

    private static final String AUTH_URL = "http://2.59.161.47:8000/auth";
    private static final String HEARTBEAT_URL = "http://2.59.161.47:8000/heartbeat";

    // === Состояние ===
    private static volatile boolean allowed = false;
    private static volatile String blockReason = null;

    // Новое состояние для явного бана (только при ответе allowed:false от сервера)
    private static volatile boolean banned = false;
    private static volatile String banReason = null;

    private static final CountDownLatch AUTH_LATCH = new CountDownLatch(1);

    private static OkHttpClient httpClient;
    private static ScheduledExecutorService heartbeatScheduler;

    private AuthManager() {}

    /**
     * Запуск авторизации в отдельном потоке.
     * Главный поток потом блокируется в waitForResult().
     */
    public static void initialize() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        Thread authThread = new Thread(() -> {
            try {
                performAuthentication();
            } catch (Throwable t) {
                // При падении треда — fail-open, не блокируем игрока
                allowed = true;
                banned = false;
                elegant.LOGGER.error("[Auth] Auth thread crashed (fail-open)", t);
            } finally {
                if (AUTH_LATCH.getCount() > 0) {
                    AUTH_LATCH.countDown();
                }
            }
        }, "Elegant-AuthThread");
        authThread.setDaemon(true);
        authThread.start();
    }

    private static void performAuthentication() {
        // Получаем ник и uuid (ждём готовности клиента)
        String username = "unknown";
        String uuid = "unknown";

        long deadline = System.currentTimeMillis() + 7000;
        while (System.currentTimeMillis() < deadline) {
            try {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc != null && mc.getSession() != null) {
                    username = mc.getSession().getUsername();
                    var u = mc.getSession().getUuidOrNull();
                    if (u != null) {
                        uuid = u.toString();
                    }
                    break;
                }
            } catch (Throwable ignored) {}
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }

        String hwid = generateHwid();

        JsonObject json = new JsonObject();
        json.addProperty("hwid", hwid);
        json.addProperty("username", username);
        json.addProperty("uuid", uuid);

        try {
            // Выполняем POST. Это происходит в отдельном потоке.
            // Главный поток ждёт через AUTH_LATCH (синхронно для решения о бане).
            String response = post(AUTH_URL, json.toString());
            JsonObject obj = JsonParser.parseString(response).getAsJsonObject();

            boolean serverAllowed = obj.has("allowed") && obj.get("allowed").getAsBoolean();

            if (serverAllowed) {
                allowed = true;
                banned = false;
                banReason = null;
                blockReason = null;
                elegant.LOGGER.info("[Auth] Успешная авторизация: {} | HWID: {}", username, hwid.substring(0, Math.min(16, hwid.length())));
                startHeartbeat(hwid, username, uuid);
            } else {
                // === ЯВНЫЙ БАН ОТ СЕРВЕРА ===
                banned = true;
                allowed = false;
                banReason = obj.has("message") ? obj.get("message").getAsString() : "Доступ запрещён";
                blockReason = banReason;

                elegant.LOGGER.error("[Auth] Получен бан от сервера: {}", banReason);
                // Дальше главный поток разблокируется и вызовет enforceServerBan()
            }
        } catch (Exception e) {
            // === СЕТЕВАЯ ОШИБКА ИЛИ СЕРВЕР НЕ ОТВЕЧАЕТ ===
            // По требованию: НЕ крашим игру. Разрешаем играть.
            allowed = true;
            banned = false;
            banReason = null;
            blockReason = null;
            elegant.LOGGER.warn("[Auth] Сервер авторизации недоступен (сетевая ошибка). Игра разрешена. {}", e.getMessage());
        } finally {
            AUTH_LATCH.countDown();
        }
    }

    /**
     * Должен вызываться в ГЛАВНОМ ПОТОКЕ после AuthManager.waitForResult(...).
     * Если был бан (allowed:false от сервера) — выводит красное сообщение и через ~1.5с крашит игру.
     */
    public static void enforceServerBan() {
        if (!banned) {
            return;
        }

        final String reason = (banReason != null && !banReason.isEmpty())
                ? banReason
                : "Доступ к этому моду запрещён";

        elegant.LOGGER.error("=== БАН: {} ===", reason);

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) {
            // Крайний случай
            try { Thread.sleep(2000); } catch (Exception ignored) {}
            System.exit(0);
            return;
        }

        // 1. Красное сообщение в чат через player.sendMessage()
        mc.execute(() -> {
            try {
                if (mc.player != null) {
                    // Красное сообщение, как просил пользователь
                    mc.player.sendMessage(
                            Text.literal("§c§l[БАН] §c" + reason),
                            false
                    );
                }
            } catch (Throwable ignored) {}
        });

        // 2. Через 1-2 секунды останавливаем клиент
        new Thread(() -> {
            try {
                Thread.sleep(1600); // ~1.6 секунды
            } catch (InterruptedException ignored) {}

            mc.execute(() -> {
                elegant.LOGGER.error("Останавливаем игру из-за бана...");
                try {
                    // Официальный способ завершения MinecraftClient
                    mc.stop();
                } catch (Throwable t) {
                    elegant.LOGGER.error("mc.stop() не сработал, используем halt", t);
                    Runtime.getRuntime().halt(0);
                }
            });
        }, "Elegant-BanCrashDelay").start();
    }

    private static void startHeartbeat(String hwid, String username, String uuid) {
        heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Elegant-Heartbeat");
            t.setDaemon(true);
            return t;
        });

        heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                JsonObject payload = new JsonObject();
                payload.addProperty("hwid", hwid);
                payload.addProperty("username", username);
                payload.addProperty("uuid", uuid);

                String resp = post(HEARTBEAT_URL, payload.toString());
                elegant.LOGGER.debug("[Auth] Heartbeat OK");
            } catch (Exception ex) {
                elegant.LOGGER.warn("[Auth] Heartbeat failed: {}", ex.getMessage());
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    private static String post(String url, String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("User-Agent", "SliznyakVisual/2.0.6")
                .header("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP error: " + response.code());
            }
            ResponseBody rb = response.body();
            return (rb != null) ? rb.string() : "{}";
        }
    }

    public static String generateHwid() {
        try {
            SystemInfo si = new SystemInfo();
            HardwareAbstractionLayer hal = si.getHardware();
            ComputerSystem cs = hal.getComputerSystem();
            CentralProcessor processor = hal.getProcessor();
            OperatingSystem os = si.getOperatingSystem();

            StringBuilder sb = new StringBuilder(256);

            sb.append("CPU_ID:").append(processor.getProcessorIdentifier().getProcessorID()).append(';');
            sb.append("CPU_NAME:").append(processor.getProcessorIdentifier().getName()).append(';');

            sb.append("BASEBOARD:").append(cs.getBaseboard().getSerialNumber()).append(';');
            sb.append("CS_SERIAL:").append(cs.getSerialNumber()).append(';');
            sb.append("MANUFACTURER:").append(cs.getManufacturer()).append(';');

            sb.append("OS:").append(os.getFamily()).append(' ').append(os.getVersionInfo()).append(';');

            hal.getDiskStores().stream().limit(4).forEach(disk -> {
                String serial = disk.getSerial();
                if (serial != null && !serial.isBlank()) {
                    sb.append("DISK:").append(serial).append(';');
                }
            });

            for (NetworkIF net : hal.getNetworkIFs()) {
                String mac = net.getMacaddr();
                if (mac != null && !mac.isBlank() && !mac.equalsIgnoreCase("00:00:00:00:00:00")) {
                    sb.append("MAC:").append(mac).append(';');
                }
            }

            return sha256(sb.toString());
        } catch (Throwable t) {
            String fallback = System.getProperty("os.name")
                    + "|" + System.getProperty("user.name")
                    + "|" + System.getProperty("os.arch")
                    + "|" + System.getProperty("java.vm.name");
            return sha256(fallback);
        }
    }

    private static String sha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                String h = Integer.toHexString(b & 0xFF);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            return Integer.toHexString(data.hashCode()) + "fb";
        }
    }

    public static boolean isAllowed() {
        return allowed;
    }

    public static String getBlockReason() {
        return blockReason == null ? "" : blockReason;
    }

    /** true — если сервер явно ответил allowed:false */
    public static boolean isBanned() {
        return banned;
    }

    public static String getBanReason() {
        return banReason == null ? "" : banReason;
    }

    /**
     * Блокирует главный поток до получения ответа от сервера (или таймаута).
     * После этого можно безопасно вызывать enforceServerBan().
     */
    public static void waitForResult(long timeoutMs) {
        try {
            AUTH_LATCH.await(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    public static void shutdown() {
        if (heartbeatScheduler != null) {
            heartbeatScheduler.shutdownNow();
            heartbeatScheduler = null;
        }
    }
}
