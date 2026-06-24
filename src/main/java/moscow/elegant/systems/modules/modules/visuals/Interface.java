package moscow.elegant.systems.modules.modules.visuals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.Generated;
import moscow.elegant.elegant;
import moscow.elegant.systems.event.EventListener;
import moscow.elegant.systems.event.impl.render.HudRenderEvent;
import moscow.elegant.systems.localization.Language;
import moscow.elegant.systems.localization.Localizator;
import moscow.elegant.systems.modules.api.ModuleCategory;
import moscow.elegant.systems.modules.api.ModuleInfo;
import moscow.elegant.systems.modules.impl.BaseModule;
import moscow.elegant.systems.setting.settings.BooleanSetting;
import moscow.elegant.systems.setting.settings.ButtonSetting;
import moscow.elegant.systems.setting.settings.ColorSetting;
import moscow.elegant.systems.setting.settings.ModeSetting;
import moscow.elegant.systems.setting.settings.SliderSetting;
import moscow.elegant.systems.setting.settings.StringSetting;
import moscow.elegant.systems.config.ConfigFile;
import moscow.elegant.systems.notifications.NotificationType;
import moscow.elegant.systems.theme.Theme;
import moscow.elegant.utility.animation.base.Animation;
import moscow.elegant.utility.animation.base.Easing;
import moscow.elegant.utility.colors.ColorRGBA;

@ModuleInfo(
   name = "Interface",
   category = ModuleCategory.VISUALS,
   enabledByDefault = true
)
public class Interface extends BaseModule {
   private final ModeSetting mode = new ModeSetting(this, "modules.settings.interface.mode");
   private final ModeSetting.Value minimalism = new ModeSetting.Value(this.mode, "modules.settings.interface.minimalism");
   private final ModeSetting.Value liquidGlass = new ModeSetting.Value(this.mode, "modules.settings.interface.liquidGlass").select();
   private final ModeSetting themeMode = new ModeSetting(this, "modules.settings.interface.themeMode");
   public final ModeSetting.Value dark = new ModeSetting.Value(this.themeMode, "modules.settings.interface.dark");
   public final ModeSetting.Value light = new ModeSetting.Value(this.themeMode, "modules.settings.interface.light").select();
   private final ModeSetting language = new ModeSetting(this, "modules.settings.interface.language");

   private final Animation liquidGlassAnim = new Animation(500L, Easing.BOTH_CUBIC);
   public final ColorSetting minimalismMainColor = new ColorSetting(
         this, "modules.settings.interface.minimalism.mainColor", () -> this.minimalism.isSelected()
      )
      .color(new ColorRGBA(151.0F, 71.0F, 255.0F, 255.0F))
      .alpha(false);
   public final ColorSetting liquidGlassColor = new ColorSetting(
         this, "modules.settings.interface.liquidGlass.glassColor", () -> this.liquidGlass.isSelected()
      )
      .color(new ColorRGBA(200.0F, 220.0F, 255.0F, 100.0F))
      .alpha(true);
   public final ColorSetting liquidClientColor = new ColorSetting(
         this, "modules.settings.interface.liquidGlass.clientColor", () -> this.liquidGlass.isSelected()
      )
      .color(new ColorRGBA(151.0F, 71.0F, 255.0F, 255.0F))
      .alpha(true);
   private final BooleanSetting showArmorHud = new BooleanSetting(this, "modules.settings.interface.show_armor").enabled(true);
   private final BooleanSetting showEffectsHud = new BooleanSetting(this, "modules.settings.interface.show_effects").enabled(true);
   private final BooleanSetting showHotbarHud = new BooleanSetting(this, "modules.settings.interface.show_hotbar").enabled(true);
   private final BooleanSetting showInventoryHud = new BooleanSetting(this, "modules.settings.interface.show_inventory").enabled(true);
   private final BooleanSetting showKeyBinds = new BooleanSetting(this, "modules.settings.interface.show_keybinds").enabled(true);
   private final BooleanSetting showTargetHud = new BooleanSetting(this, "modules.settings.interface.show_targethud").enabled(true);
   private final BooleanSetting showWatermark = new BooleanSetting(this, "modules.settings.interface.show_watermark").enabled(true);
   private final BooleanSetting showChinahat = new BooleanSetting(this, "modules.settings.interface.show_chinahat").enabled(true);
   private final BooleanSetting targetHudAdaptive = new BooleanSetting(this, "modules.settings.interface.targethud_adaptive").enabled(true);
   public final SliderSetting liquidDistortion = new SliderSetting(
         this, "modules.settings.interface.liquidGlass.distortion", () -> this.liquidGlass.isSelected()
      )
      .min(-0.2F)
      .max(0.2F)
      .step(0.01F)
      .currentValue(0.08F);
   private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "Interface-Thread");
      t.setDaemon(true);
      return t;
   });
   private boolean languageAutoDetected;
   private int lastLang = 0;
   private final EventListener<HudRenderEvent> onHudRenderEvent = event -> {
      this.liquidGlassAnim.setEasing(Easing.FIGMA_EASE_IN_OUT);
      int lang = this.language.getValues().indexOf(this.language.getValue());
      if (lang != this.lastLang) {
         Localizator.setLanguage(lang == 0 ? Language.EN_US : (lang == 1 ? Language.RU_RU : (lang == 2 ? Language.UK_UA : Language.PL_PL)));
         this.languageAutoDetected = false;
      }

      this.lastLang = lang;
      elegant.getInstance().getThemeManager().setCurrentTheme(this.dark.isSelected() ? Theme.DARK : Theme.LIGHT);
   };

   public Interface() {
      new ModeSetting.Value(this.language, "English").select();
      new ModeSetting.Value(this.language, "Русский");
      new ModeSetting.Value(this.language, "Українська");
      new ModeSetting.Value(this.language, "polski");
   }

   private void detectLanguageByIP() {
      this.executor.submit(() -> {
         try {
            String countryCode = this.getCountryCodeByIP();
            if (countryCode != null) {
               String var2 = countryCode.toUpperCase();
               switch (var2) {
                  case "UA":
                     this.language.setValue(this.language.getValues().get(2));
                     Localizator.setLanguage(Language.UK_UA);
                     break;
                  case "PL":
                     this.language.setValue(this.language.getValues().get(3));
                     Localizator.setLanguage(Language.PL_PL);
                     break;
                  default:
                     this.language.setValue(this.language.getValues().getFirst());
                     Localizator.setLanguage(Language.EN_US);
               }

               this.languageAutoDetected = true;
               this.lastLang = this.language.getValues().indexOf(this.language.getValue());
            }
         } catch (Exception var5) {
            elegant.LOGGER.error("Failed to detect language by IP", var5);
         }
      });
   }

   private String getCountryCodeByIP() throws IOException {
      URL url = new URL("http://ip-api.com/json/?fields=countryCode");
      HttpURLConnection connection = (HttpURLConnection)url.openConnection();
      connection.setRequestMethod("GET");

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
         StringBuilder response = new StringBuilder();

         String line;
         while ((line = reader.readLine()) != null) {
            response.append(line);
         }

         String json = response.toString();
         int start = json.indexOf("countryCode\":\"") + 14;
         if (start >= 14) {
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
         }
      }

      return null;
   }

   public static float glass() {
      Interface module = elegant.getInstance().getModuleManager().getModule(Interface.class);
      if (module == null) {
         return 0.0F;
      }

      module.liquidGlassAnim.update(module.liquidGlass.isSelected() ? 1.0F : 0.0F);
      return module.liquidGlassAnim.getValue();
   }

   public static float minimalizm() {
      return 1.0F - glass();
   }

   public static boolean showGlass() {
      return glass() > 0.0F;
   }

   public static boolean showMinimalizm() {
      return glass() < 1.0F;
   }

   public static float getDistortion() {
      Interface module = elegant.getInstance().getModuleManager().getModule(Interface.class);
      return module != null ? module.liquidDistortion.getCurrentValue() : 0.08F;
   }

   public static boolean glassSelected() {
      Interface module = elegant.getInstance().getModuleManager().getModule(Interface.class);
      return module != null && module.liquidGlass.isSelected();
   }

   public static boolean showArmorHud() {
      Interface module = elegant.getInstance().getModuleManager().getModule(Interface.class);
      return module == null || module.showArmorHud.isEnabled();
   }

   public static boolean showEffects() {
      Interface module = elegant.getInstance().getModuleManager().getModule(Interface.class);
      return module == null || module.showEffectsHud.isEnabled();
   }

   public static boolean showHotbarHud() {
      Interface module = elegant.getInstance().getModuleManager().getModule(Interface.class);
      return module == null || module.showHotbarHud.isEnabled();
   }

   public static boolean showInventoryHud() {
      Interface module = elegant.getInstance().getModuleManager().getModule(Interface.class);
      return module == null || module.showInventoryHud.isEnabled();
   }

   public static boolean showKeyBinds() {
      Interface module = elegant.getInstance().getModuleManager().getModule(Interface.class);
      return module == null || module.showKeyBinds.isEnabled();
   }

   public static boolean showTargetHud() {
      Interface module = elegant.getInstance().getModuleManager().getModule(Interface.class);
      return module == null || module.showTargetHud.isEnabled();
   }

   public static boolean showWatermark() {
      Interface module = elegant.getInstance().getModuleManager().getModule(Interface.class);
      return module == null || module.showWatermark.isEnabled();
   }

   public static boolean showChinahat() {
      Interface module = elegant.getInstance().getModuleManager().getModule(Interface.class);
      return module == null || module.showChinahat.isEnabled();
   }

   public static boolean targetHudAdaptive() {
      Interface module = elegant.getInstance().getModuleManager().getModule(Interface.class);
      return module == null || module.targetHudAdaptive.isEnabled();
   }

   @Generated
   public ModeSetting getMode() {
      return this.mode;
   }

   @Generated
   public ModeSetting.Value getLiquidGlass() {
      return this.liquidGlass;
   }

   @Generated
   public ModeSetting.Value getMinimalism() {
      return this.minimalism;
   }

   @Generated
   public ModeSetting getThemeMode() {
      return this.themeMode;
   }

   @Generated
   public ModeSetting.Value getDark() {
      return this.dark;
   }

   @Generated
   public ModeSetting.Value getLight() {
      return this.light;
   }

   @Generated
   public ModeSetting getLanguage() {
      return this.language;
   }

   @Generated
   public Animation getLiquidGlassAnim() {
      return this.liquidGlassAnim;
   }

   @Generated
   public ExecutorService getExecutor() {
      return this.executor;
   }

   @Generated
   public boolean isLanguageAutoDetected() {
      return this.languageAutoDetected;
   }

   @Generated
   public int getLastLang() {
      return this.lastLang;
   }

   @Generated
   public EventListener<HudRenderEvent> getOnHudRenderEvent() {
      return this.onHudRenderEvent;
   }

   @Generated
   public ColorSetting getMinimalismMainColor() {
      return this.minimalismMainColor;
   }

   @Generated
   public ColorSetting getLiquidGlassColor() {
      return this.liquidGlassColor;
   }

   @Generated
   public ColorSetting getLiquidClientColor() {
      return this.liquidClientColor;
   }

   @Generated
   public SliderSetting getLiquidDistortion() {
      return this.liquidDistortion;
   }

   @Override
   public void onEnable() {
      super.onEnable();
   }

   @Override
   public void onDisable() {
      if (this.executor != null && !this.executor.isShutdown()) {
         this.executor.shutdownNow();
      }
   }
}
