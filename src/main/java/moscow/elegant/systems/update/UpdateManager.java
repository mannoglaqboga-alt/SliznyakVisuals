package moscow.elegant.systems.update;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import moscow.elegant.elegant;
import moscow.elegant.utility.game.WebUtility;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

/**
 * Простая система обновлений с GitHub.
 * Показывает уведомление в игре (через NotificationManager или Dynamic Island).
 * Позволяет скачать новую версию и предложить перезапуск.
 */
public class UpdateManager {

    private static final String GITHUB_REPO = "mannoglaqboga-alt/SliznyakVisuals";
    private static final String USER_AGENT = "SliznyakVisual-UpdateChecker";

    private boolean checked = false;
    private boolean updateAvailable = false;
    private String currentVersion;
    private String latestVersion;
    private String downloadUrl;
    private boolean downloading = false;
    private boolean downloadComplete = false;
    private float downloadProgress = 0f;

    public UpdateManager() {
        this.currentVersion = getCurrentVersion();
        handleOldJars();
    }

    private void handleOldJars() {
        try {
            java.nio.file.Path modsDir = net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir().resolve("mods");
            java.io.File[] files = modsDir.toFile().listFiles(f -> f.isFile() && f.getName().matches("sliznyakvisual-.*\\.jar"));
            if (files == null || files.length <= 1) return;
            String maxV = "0.0.0";
            for (java.io.File f : files) {
                String v = f.getName().replace("sliznyakvisual-", "").replace(".jar", "");
                if (isNewer(v, maxV)) {
                    maxV = v;
                }
            }
            for (java.io.File f : files) {
                String v = f.getName().replace("sliznyakvisual-", "").replace(".jar", "");
                if (!v.equals(maxV)) {
                    f.delete();
                }
            }
        } catch (Exception e) {}
    }

    private String getCurrentVersion() {
        return FabricLoader.getInstance()
            .getModContainer("sliznyakvisual")
            .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
            .orElse("2.0.8");
    }

    public void checkForUpdates() {
        if (checked) return;
        checked = true;

        CompletableFuture.runAsync(() -> {
            try {
                String apiUrl = "https://api.github.com/repos/" + GITHUB_REPO + "/releases";
                HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
                conn.setRequestProperty("User-Agent", USER_AGENT);
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                if (conn.getResponseCode() != 200) {
                    elegant.LOGGER.warn("[Update] GitHub API returned {}", conn.getResponseCode());
                    return;
                }

                try (InputStream is = conn.getInputStream()) {
                    String json = new String(is.readAllBytes());
                    var arr = JsonParser.parseString(json).getAsJsonArray();

                    JsonObject latestObj = null;
                    String latestVer = "0.0.0";

                    for (int i = 0; i < arr.size(); i++) {
                        JsonObject obj = arr.get(i).getAsJsonObject();
                        if (obj.get("draft").getAsBoolean() || obj.get("prerelease").getAsBoolean()) continue;

                        String rawTag = obj.get("tag_name").getAsString();
                        String ver = rawTag.replace("v", "").replace("V", "").trim();

                        if (isNewer(ver, latestVer)) {
                            latestVer = ver;
                            latestObj = obj;
                        }
                    }

                    if (latestObj == null) return;

                    String rawTag = latestObj.get("tag_name").getAsString();
                    this.latestVersion = rawTag.replace("v", "").replace("V", "").trim();

                    // Ищем jar asset
                    if (latestObj.has("assets")) {
                        latestObj.getAsJsonArray("assets").forEach(asset -> {
                            JsonObject a = asset.getAsJsonObject();
                            String name = a.get("name").getAsString();
                            if (name.endsWith(".jar") && (name.contains("sliznyak") || name.contains("Sliznyak"))) {
                                this.downloadUrl = a.get("browser_download_url").getAsString();
                            }
                        });
                    }

                    if (this.downloadUrl == null) {
                        // fallback
                        this.downloadUrl = "https://github.com/" + GITHUB_REPO + "/releases/download/" + rawTag + "/sliznyakvisual-" + this.latestVersion + ".jar";
                    }

                    if (isNewer(latestVersion, currentVersion)) {
                        this.updateAvailable = true;
                        elegant.LOGGER.info("[Update] New version available: {} (current {})", latestVersion, currentVersion);

                        elegant.getInstance().getNotificationManager()
                            .addNotificationOther(
                                moscow.elegant.systems.notifications.NotificationType.INFO,
                                "Доступно обновление",
                                "v" + latestVersion + " — нажми в меню для загрузки"
                            );
                    }
                }
            } catch (Exception e) {
                elegant.LOGGER.warn("[Update] Failed to check updates: {}", e.getMessage());
            }
        });
    }

    private boolean isNewer(String latest, String current) {
        try {
            String[] l = latest.split("\\.");
            String[] c = current.split("\\.");
            for (int i = 0; i < Math.min(l.length, c.length); i++) {
                int lv = Integer.parseInt(l[i]);
                int cv = Integer.parseInt(c[i]);
                if (lv > cv) return true;
                if (lv < cv) return false;
            }
            return l.length > c.length;
        } catch (Exception e) {
            return !latest.equals(current);
        }
    }

    public void downloadUpdate() {
        if (!updateAvailable || downloading || downloadUrl == null) return;

        downloading = true;
        downloadProgress = 0f;

        CompletableFuture.runAsync(() -> {
            try {
                elegant.LOGGER.info("[Update] Downloading {} ...", downloadUrl);

                HttpURLConnection conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
                conn.setRequestProperty("User-Agent", USER_AGENT);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);

                long contentLength = conn.getContentLengthLong();

                Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
                Files.createDirectories(modsDir);

                String fileName = "sliznyakvisual-" + latestVersion + ".jar";
                Path target = modsDir.resolve(fileName);

                try (InputStream in = conn.getInputStream()) {
                    long total = contentLength > 0 ? contentLength : -1;
                    long downloaded = 0;

                    try (var out = Files.newOutputStream(target)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = in.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                            downloaded += len;
                            if (total > 0) {
                                downloadProgress = (float) downloaded / total;
                            }
                        }
                    }
                }

                this.downloadComplete = true;
                this.downloading = false;

                elegant.LOGGER.info("[Update] Downloaded to {}", target);

                elegant.getInstance().getNotificationManager()
                    .addNotificationOther(
                        moscow.elegant.systems.notifications.NotificationType.SUCCESS,
                        "Обновление загружено",
                        "v" + latestVersion + " готово к установке"
                    );

            } catch (Exception e) {
                this.downloading = false;
                elegant.LOGGER.error("[Update] Download failed", e);
                elegant.getInstance().getNotificationManager()
                    .addNotificationOther(
                        moscow.elegant.systems.notifications.NotificationType.ERROR,
                        "Ошибка загрузки",
                        "Не удалось скачать обновление"
                    );
            }
        });
    }

    public void restartNow() {
        if (MinecraftClient.getInstance() != null) {
            MinecraftClient.getInstance().scheduleStop();
        }
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public boolean isDownloading() {
        return downloading;
    }

    public boolean isDownloadComplete() {
        return downloadComplete;
    }

    public float getDownloadProgress() {
        return downloadProgress;
    }

    public void reset() {
        this.updateAvailable = false;
        this.downloadComplete = false;
        this.downloading = false;
    }
}
