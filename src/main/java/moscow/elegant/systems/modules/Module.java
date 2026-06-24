package moscow.elegant.systems.modules;

import moscow.elegant.systems.localization.Localizator;
import moscow.elegant.systems.modules.api.ModuleCategory;
import moscow.elegant.systems.modules.api.ModuleInfo;
import moscow.elegant.systems.setting.SettingsContainer;
import moscow.elegant.utility.animation.base.Animation;
import moscow.elegant.utility.interfaces.IMinecraft;
import moscow.elegant.utility.interfaces.IScaledResolution;
import moscow.elegant.utility.interfaces.Toggleable;

public interface Module extends Toggleable, IMinecraft, IScaledResolution, SettingsContainer {
   void disable();

   void enable();

   void tick();

   ModuleInfo getInfo();

   String getName();

   default String getDescription() {
      String translationKey = "modules.descriptions.%s".formatted(this.getName().toLowerCase().replace(" ", "_"));
      return Localizator.translate(translationKey);
   }

   int getKey();

   ModuleCategory getCategory();

   boolean isEnabled();

   boolean isHidden();

   Animation getKeybindsAnimation();

   void setKey(int var1);

   void setEnabled(boolean var1, boolean var2);
}
