package moscow.elegant.systems.modules.modules.other;

import moscow.elegant.elegant;
import moscow.elegant.systems.event.EventListener;
import moscow.elegant.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.elegant.systems.modules.api.ModuleCategory;
import moscow.elegant.systems.modules.api.ModuleInfo;
import moscow.elegant.systems.modules.impl.BaseModule;
import moscow.elegant.systems.setting.settings.BooleanSetting;
import moscow.elegant.systems.waypoints.WayPointsManager;
import moscow.elegant.utility.game.MessageUtility;
import net.minecraft.text.Text;
import net.minecraft.client.gui.screen.DeathScreen;

@ModuleInfo(
   name = "Death Cords",
   category = ModuleCategory.OTHER,
   desc = "Отправляет координаты смерти в чат"
)
public class DeathCords extends BaseModule {
   private boolean death;
   private final BooleanSetting wayDeath = new BooleanSetting(this, "Ставить метку");
   private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> {
      if (!(mc.currentScreen instanceof DeathScreen) || mc.player == null) {
         this.death = true;
      } else if (this.death) {
         int xCord = (int)mc.player.getX();
         int yCord = (int)mc.player.getY();
         int zCord = (int)mc.player.getZ();
         MessageUtility.info(Text.of("Координаты смерти: " + xCord + " " + yCord + " " + zCord));
         if (this.wayDeath.isEnabled()) {
            WayPointsManager wayPointsManager = elegant.getInstance().getWayPointsManager();
            if (wayPointsManager.contains("Death")) {
               wayPointsManager.del("Death");
            }

            wayPointsManager.add("Death", xCord, yCord, zCord);
         }

         this.death = false;
      }
   };
}
