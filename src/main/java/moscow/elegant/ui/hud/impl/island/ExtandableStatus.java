package moscow.elegant.ui.hud.impl.island;

import moscow.elegant.systems.setting.settings.SelectSetting;

public class ExtandableStatus extends IslandStatus {
   public ExtandableStatus(SelectSetting setting, String name) {
      super(setting, name);
   }

   @Override
   public boolean canShow() {
      return false;
   }
}
