package moscow.elegant.systems.modules.modules.visuals;

import moscow.elegant.elegant;
import moscow.elegant.systems.modules.api.ModuleCategory;
import moscow.elegant.systems.modules.api.ModuleInfo;
import moscow.elegant.systems.modules.impl.BaseModule;
import moscow.elegant.systems.modules.modules.other.Sounds;
import moscow.elegant.systems.modules.modules.visuals.Interface;
import moscow.elegant.systems.setting.settings.BindSetting;
import moscow.elegant.systems.setting.settings.ModeSetting;
import moscow.elegant.systems.theme.Theme;
import moscow.elegant.ui.menu.MenuScreen;
import moscow.elegant.ui.menu.api.MenuCloseListener;
import moscow.elegant.utility.sounds.ClientSounds;

@ModuleInfo(
   name = "Menu",
   category = ModuleCategory.VISUALS,
   key = -1,
   desc = "modules.descriptions.menu"
)
public class MenuModule extends BaseModule {
   private static final MenuCloseListener menuCloseListener = new MenuCloseListener();
   private final ModeSetting mode = new ModeSetting(this, "modules.settings.menu.mode");
   private final ModeSetting.Value dropdown = new ModeSetting.Value(this.mode, "modules.settings.menu.mode.dropdown");
   private final BindSetting menuBind = new BindSetting(this, "modules.settings.menu.bind").key(344);

   @Override
   public void onEnable() {
      updateTheme();
      if (!(mc.currentScreen instanceof MenuScreen)) {
         MenuScreen menuScreen = elegant.getInstance().getMenuScreen();
         mc.setScreen(menuScreen);
         Sounds soundsModule = elegant.getInstance().getModuleManager().getModule(Sounds.class);
         if (soundsModule.isEnabled()) {
            ClientSounds.CLICKGUI_OPEN.play(soundsModule.getVolume().getCurrentValue());
         }

         super.onEnable();
      }
   }

   @Override
   public void onDisable() {
      if (mc.currentScreen instanceof MenuScreen) {
         mc.setScreen(null);
         elegant.getInstance().getMenuScreen().setClosing(true);
      }

      super.onDisable();
   }

   @Override
   public int getKey() {
      return this.menuBind.getKey();
   }

   @Override
   public void setKey(int key) {
      this.menuBind.setKey(key);
   }

   private void updateTheme() {
      Interface iface = elegant.getInstance().getModuleManager().getModule(Interface.class);
      if (iface != null) {
         Theme t = iface.getLight().isSelected() ? Theme.LIGHT : Theme.DARK;
         elegant.getInstance().getThemeManager().setCurrentTheme(t);
      }
   }
}
