package moscow.elegant;

import java.awt.image.BufferedImage;
import java.net.URI;
import javax.imageio.ImageIO;
import lombok.Generated;
import moscow.elegant.framework.shader.GlProgram;
import moscow.elegant.systems.commands.CommandRegistry;
import moscow.elegant.systems.config.ConfigDropHandler;
import moscow.elegant.systems.config.ConfigFile;
import moscow.elegant.systems.config.ConfigManager;
import moscow.elegant.systems.event.EventIntegration;
import moscow.elegant.systems.event.EventManager;
import moscow.elegant.systems.event.handlers.ServerConnectionHandler;
import moscow.elegant.systems.file.FileManager;
import moscow.elegant.systems.friends.FriendManager;
import moscow.elegant.systems.localization.Localizator;
import moscow.elegant.systems.modules.ModuleManager;
import moscow.elegant.systems.modules.constructions.swinganim.SwingManager;
import moscow.elegant.systems.modules.constructions.swinganim.presets.SwingPresetManager;
import moscow.elegant.systems.modules.listeners.ModuleTickListener;
import moscow.elegant.systems.modules.listeners.ModuleWidgetRenderer;
import moscow.elegant.systems.notifications.NotificationManager;
import moscow.elegant.systems.poshalko.PoshalkoHandler;
import moscow.elegant.systems.target.TargetManager;
import moscow.elegant.systems.theme.ThemeManager;
import moscow.elegant.systems.waypoints.WayPointsManager;
import moscow.elegant.ui.hud.Hud;
import moscow.elegant.ui.menu.MenuScreen;
import moscow.elegant.utility.debug.DebugModeDetector;
import moscow.elegant.utility.debug.ProcessWatchdog;
import moscow.elegant.protection.client.AuthManager;
import moscow.elegant.utility.debug.ThreadDebugger;
import moscow.elegant.utility.game.TitleBarHelper;
import moscow.elegant.utility.game.WebUtility;
import moscow.elegant.utility.game.server.TPSHandler;
import moscow.elegant.utility.interfaces.IMinecraft;
import moscow.elegant.utility.math.calculator.ChatListener;
import moscow.elegant.utility.render.DrawUtility;
import moscow.elegant.utility.rotations.RotationHandler;
import moscow.elegant.utility.rotations.RotationUpdateListener;
import moscow.elegant.utility.sounds.MusicTracker;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kotopushka.compiler.sdk.annotations.Compile;
import ru.kotopushka.compiler.sdk.annotations.CompileBytecode;
import ru.kotopushka.compiler.sdk.annotations.Initialization;

public enum elegant implements IMinecraft {
   INSTANCE;

   public static final String NAME = "SliznyakVisual by 126id";
   public static final String BUILD_TYPE = "Beta";
   public static final String VERSION = "INTELIJ IDEA READY";
   public static final String MOD_ID = "elegant".toLowerCase();
   public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
   private EventManager eventManager;
   private ThemeManager themeManager;
   private ModuleManager moduleManager;
   private CommandRegistry commandManager;
   private FriendManager friendManager;
   private RotationHandler rotationHandler;
   private TargetManager targetManager;
   private MusicTracker musicTracker;
   private FileManager fileManager;
   private NotificationManager notificationManager;
   private moscow.elegant.systems.update.UpdateManager updateManager;
   private ConfigManager configManager;
   private SwingManager swingManager;
   private TPSHandler tpsHandler;
   private Hud hud;
   private ServerConnectionHandler serverConnectionHandler;
   private PoshalkoHandler poshalkoHandler;
   private WayPointsManager wayPointsManager;
   private SwingPresetManager swingPresetManager;
   private MenuScreen menuScreen;
   private ChatListener chatListener;
   private boolean panic;

   @Compile
   @Initialization
   public void initialize() {
      // Ждём результат авторизации (максимум 7 секунд) — синхронно
      AuthManager.waitForResult(7000);

      // Если сервер вернул явный бан (allowed:false) — крашим игру
      AuthManager.enforceServerBan();

      LOGGER.info("Initializing {}...", "SliznyakVisual");
      this.musicTracker = new MusicTracker();
      this.wayPointsManager = new WayPointsManager();
      this.eventManager = new EventManager();
      this.friendManager = new FriendManager();
      this.themeManager = new ThemeManager();
      this.rotationHandler = new RotationHandler(new RotationUpdateListener());
      this.targetManager = new TargetManager();
      this.fileManager = new FileManager();
      this.moduleManager = new ModuleManager(new ModuleTickListener(), new ModuleWidgetRenderer());
      this.hud = new Hud();
      this.tpsHandler = new TPSHandler();
      this.notificationManager = new NotificationManager();
      this.updateManager = new moscow.elegant.systems.update.UpdateManager();
      this.configManager = new ConfigManager();
      this.fileManager.registerClientFiles();
      this.moduleManager.registerModules();
      this.moduleManager.enableModules();

      // HWID/Auth block (для не-банов)
      if (AuthManager.isBanned()) {
         elegant.LOGGER.error("[Auth] Banned by server: {}", AuthManager.getBanReason());
         // Дальше сработает краш в enforceServerBan, но на всякий случай
      } else if (!AuthManager.isAllowed()) {
         elegant.LOGGER.error("Auth denied or failed: {}", AuthManager.getBlockReason());
         this.moduleManager.disableAllModules();
      }

      this.configManager.handle();
      this.commandManager = new CommandRegistry();
      this.commandManager.initCommands();
      this.swingManager = new SwingManager();
      this.swingPresetManager = new SwingPresetManager();
      this.swingPresetManager.handle();
      this.fileManager.loadClientFiles();

      // Проверяем обновления асинхронно (не блокируем запуск)
      this.updateManager.checkForUpdates();
      if (this.hud != null) {
         String[] coreHudNames = new String[] {
            "hud.armor",
            "hud.effects",
            "hud.hotbar",
            "hud.inventory",
            "hud.keybinds",
            "hud.targethud",
            "hud.watermark"
         };

         for (String name : coreHudNames) {
            try {
               moscow.elegant.ui.hud.HudElement element = this.hud.getElementByName(name);
               if (element != null) {
                  element.setShowing(true);
               }
            } catch (Exception ignored) {
            }
         }
      }
      ConfigFile autosaveConfig = this.configManager.getConfig("autosave", true);
      if (autosaveConfig != null) {
         LOGGER.info("Loading autosave config on startup...");
         autosaveConfig.load();
         this.configManager.setCurrent(autosaveConfig);
         LOGGER.info(
            "Autosave config loaded and set as current. Modules enabled: {}", this.moduleManager.getModules().stream().filter(m -> m.isEnabled()).count()
         );
      } else {
         LOGGER.error("Autosave config not found!");
      }

      ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
         public Identifier getFabricId() {
            return elegant.id("after_shader_load");
         }

         public void reload(ResourceManager manager) {
            try {
               GlProgram.loadAndSetupPrograms();
            } catch (Exception var3) {
               elegant.LOGGER.warn("Failed to load shader programs, continuing without them", var3);
            }
         }
      });
      DrawUtility.initializeShaders();
      Localizator.loadTranslations();
      this.chatListener = new ChatListener();
      this.serverConnectionHandler = new ServerConnectionHandler();
      this.poshalkoHandler = new PoshalkoHandler();
      String osName = System.getProperty("os.name");
      String pcName = System.getProperty("user.name");
      if (osName.toLowerCase().contains("windows") && !pcName.equals("nesquik")) {
      }

      ConfigDropHandler.init();
      TitleBarHelper.setDarkTitleBar();
      new EventIntegration();
      this.createAvatar();
      ProcessWatchdog.startWatchdog();
      LOGGER.info("{} initialized", "SliznyakVisual");
   }

   public void shutdown() {
      LOGGER.error("=== elegant SHUTDOWN STARTED ===");
      System.err.println("=== elegant SHUTDOWN STARTED ===");
      ProcessWatchdog.triggerShutdown();
      boolean isDebugMode = DebugModeDetector.isDebugMode();
      if (this.moduleManager != null) {
         this.moduleManager.disableAllModules();
      }

      this.fileManager.saveClientFiles();
      if (!this.isPanic()) {
         LOGGER.error("=== SAVING AUTOSAVE CONFIG ===");
         System.err.println("=== SAVING AUTOSAVE CONFIG ===");
         this.swingPresetManager.getAutoSavePreset().save();
         ConfigFile autosaveConfig = this.configManager.getConfig("autosave", false);
         if (autosaveConfig != null) {
            autosaveConfig.save();
         }

         LOGGER.error("=== AUTOSAVE CONFIG SAVED ===");
         System.err.println("=== AUTOSAVE CONFIG SAVED ===");
      } else {
         LOGGER.error("=== PANIC MODE - SKIPPING AUTOSAVE ===");
      }


      if (this.musicTracker != null) {
         this.musicTracker.shutdown();
      }

      try {
         Class<?> elegantClass = Class.forName("ru.elegant.elegant");
         Object elegantInstance = elegantClass.getMethod("getInstance").invoke(null);
         if (elegantInstance != null) {
            elegantClass.getMethod("shutdown").invoke(elegantInstance);
         }
      } catch (ClassNotFoundException var4) {
         LOGGER.warn("SliznyakVisuals not found, skipping shutdown");
      } catch (Exception var5) {
         LOGGER.error("Error shutting down SliznyakVisuals: {}", var5.getMessage());
      }

      this.setPanic(false);
      Thread shutdownThread = new Thread(() -> {
         try {
            Thread.sleep(300L);
            ThreadDebugger.logAllThreads();
            ThreadDebugger.interruptAllNonDaemonThreads();
            System.gc();
            Thread.sleep(500L);
            LOGGER.info("Force exiting JVM");
            System.exit(0);
            Runtime.getRuntime().halt(0);
         } catch (InterruptedException var1x) {
            Thread.currentThread().interrupt();
            Runtime.getRuntime().halt(0);
         } catch (Exception var2x) {
            LOGGER.error("Error during shutdown: {}", var2x.getMessage());
            Runtime.getRuntime().halt(0);
         }
      });
      shutdownThread.setDaemon(true);
      shutdownThread.start();
   }

   public static elegant getInstance() {
      return INSTANCE;
   }

   public static Identifier id(String path) {
      return Identifier.of(MOD_ID, path);
   }

   @CompileBytecode
   private void createAvatar() {
      try {
         BufferedImage bufferedImage = ImageIO.read(URI.create("https://elegant.pub/api/avatars/ConeTin.jpg?t=1754613855632").toURL());
         if (bufferedImage == null) {
            return;
         }

         Identifier id = id("temp/avatar");
         mc.getTextureManager().registerTexture(id, new NativeImageBackedTexture(WebUtility.bufferedImageToNativeImage(bufferedImage, true)));
      } catch (Exception var3) {
      }
   }

   @Generated
   public EventManager getEventManager() {
      return this.eventManager;
   }

   @Generated
   public ThemeManager getThemeManager() {
      return this.themeManager;
   }

   @Generated
   public ModuleManager getModuleManager() {
      return this.moduleManager;
   }

   @Generated
   public CommandRegistry getCommandManager() {
      return this.commandManager;
   }

   @Generated
   public FriendManager getFriendManager() {
      return this.friendManager;
   }

   @Generated
   public RotationHandler getRotationHandler() {
      return this.rotationHandler;
   }

   @Generated
   public TargetManager getTargetManager() {
      return this.targetManager;
   }

   @Generated
   public MusicTracker getMusicTracker() {
      return this.musicTracker;
   }

   @Generated
   public FileManager getFileManager() {
      return this.fileManager;
   }

   @Generated
   public NotificationManager getNotificationManager() {
      return this.notificationManager;
   }

   public moscow.elegant.systems.update.UpdateManager getUpdateManager() {
      return this.updateManager;
   }

   @Generated
   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   @Generated
   public SwingManager getSwingManager() {
      return this.swingManager;
   }

   @Generated
   public SwingPresetManager getSwingPresetManager() {
      return this.swingPresetManager;
   }

   @Generated
   public TPSHandler getTpsHandler() {
      return this.tpsHandler;
   }

   @Generated
   public Hud getHud() {
      return this.hud;
   }

   @Generated
   public ServerConnectionHandler getServerConnectionHandler() {
      return this.serverConnectionHandler;
   }

   @Generated
   public PoshalkoHandler getPoshalkoHandler() {
      return this.poshalkoHandler;
   }

   @Generated
   public WayPointsManager getWayPointsManager() {
      return this.wayPointsManager;
   }

   @Generated
   public MenuScreen getMenuScreen() {
      return this.menuScreen;
   }

   @Generated
   public ChatListener getChatListener() {
      return this.chatListener;
   }

   @Generated
   public boolean isPanic() {
      return this.panic;
   }

   @Generated
   public void setMenuScreen(MenuScreen menuScreen) {
      this.menuScreen = menuScreen;
   }

   @Generated
   public void setPanic(boolean panic) {
      this.panic = panic;
   }
}
