package moscow.elegant.systems.modules.modules.combat;

import moscow.elegant.systems.event.EventListener;
import moscow.elegant.systems.event.impl.game.TotemLossEvent;
import moscow.elegant.systems.modules.api.ModuleCategory;
import moscow.elegant.systems.modules.api.ModuleInfo;
import moscow.elegant.systems.modules.impl.BaseModule;
import net.minecraft.text.Text;

@ModuleInfo(
   name = "Totem Tracker",
   category = ModuleCategory.COMBAT,
   desc = "modules.descriptions.totem_tracker"
)
public class TotemTracker extends BaseModule {
   private long lastTotemTime = 0L;
   private String lastPlayerName = "";
   private final EventListener<TotemLossEvent> onTotemLoss = event -> {
      String playerName = event.getPlayer().getName().getString();
      boolean wasEnchanted = event.wasEnchanted();
      long currentTime = System.currentTimeMillis();
      if (currentTime - this.lastTotemTime >= 100L || !playerName.equals(this.lastPlayerName)) {
         this.lastTotemTime = currentTime;
         this.lastPlayerName = playerName;
         String totemType = wasEnchanted ? "Зачарованный" : "Не зачарованный";
         int totemColor = wasEnchanted ? 5635925 : 16733525;
         Text message = Text.literal("")
            .append(Text.literal("[elegant]").withColor(10190335))
            .append(Text.literal(" "))
            .append(Text.literal("Игрок ").withColor(16777215))
            .append(Text.literal(playerName).withColor(16777215))
            .append(Text.literal(" потерял ").withColor(16777215))
            .append(Text.literal(totemType).withColor(totemColor))
            .append(Text.literal(" тотем").withColor(16777215));
         if (mc.player != null) {
            mc.player.sendMessage(message, false);
         }
      }
   };

   @Override
   public void onEnable() {
      super.onEnable();
   }

   @Override
   public void onDisable() {
      super.onDisable();
   }
}
