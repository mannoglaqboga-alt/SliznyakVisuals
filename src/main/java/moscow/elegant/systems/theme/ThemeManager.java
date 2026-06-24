package moscow.elegant.systems.theme;

import lombok.Generated;
import moscow.elegant.elegant;
import moscow.elegant.systems.modules.modules.visuals.Interface;

public class ThemeManager {
   private Theme currentTheme = Theme.DARK;

   public void switchTheme() {
      this.currentTheme = this.currentTheme == Theme.DARK ? Theme.LIGHT : Theme.DARK;
   }

   public Theme getCurrentTheme() {
      Interface iface = elegant.getInstance().getModuleManager().getModule(Interface.class);
      if (iface != null && iface.getLight().isSelected()) {
         return Theme.LIGHT;
      }
      if (Interface.glassSelected()) {
         return Theme.DARK;
      }
      return this.currentTheme;
   }
   @Generated
   public void setCurrentTheme(Theme currentTheme) {
      this.currentTheme = currentTheme;
   }
}
