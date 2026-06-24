package moscow.elegant.systems.modules.modules.other;

import lombok.Generated;
import moscow.elegant.systems.modules.api.ModuleCategory;
import moscow.elegant.systems.modules.api.ModuleInfo;
import moscow.elegant.systems.modules.impl.BaseModule;
import moscow.elegant.systems.setting.settings.StringSetting;
import moscow.elegant.utility.game.EntityUtility;

@ModuleInfo(
   name = "Name Protect",
   category = ModuleCategory.OTHER,
   desc = "Визуально скрывает ник игрока"
)
public class NameProtect extends BaseModule {
   private final StringSetting fakeName = new StringSetting(this, "modules.settings.name_protect.fake_name").text("Player");

   public String patchName(String text) {
      String clientUsername = mc.getSession().getUsername();
      if (EntityUtility.isInGame()) {
         text = text.replace(mc.player.getDisplayName().getString(), this.fakeName.getText());
      }

      return text.replace(clientUsername, this.fakeName.getText());
   }

   @Generated
   public StringSetting getFakeName() {
      return this.fakeName;
   }
}
