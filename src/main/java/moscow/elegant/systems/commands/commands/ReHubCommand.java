package moscow.elegant.systems.commands.commands;

import moscow.elegant.elegant;
import moscow.elegant.systems.commands.Command;
import moscow.elegant.systems.commands.CommandBuilder;
import moscow.elegant.systems.commands.CommandContext;
import moscow.elegant.systems.event.EventListener;
import moscow.elegant.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.elegant.systems.localization.Localizator;
import moscow.elegant.utility.game.MessageUtility;
import moscow.elegant.utility.game.server.ServerUtility;
import moscow.elegant.utility.interfaces.IMinecraft;
import moscow.elegant.utility.time.Timer;
import net.minecraft.world.Difficulty;
import net.minecraft.text.Text;

public class ReHubCommand implements IMinecraft {
   private boolean processing;
   private final Timer timer = new Timer();
   private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> {
      if (this.processing
         && mc.world != null
         && mc.player != null
         && (ServerUtility.isFT() || ServerUtility.isFT())
         && mc.world.getDifficulty() == Difficulty.EASY
         && this.timer.finished(1000L)) {
         mc.player.networkHandler.sendChatCommand("an" + ServerUtility.ftAn);
         this.timer.reset();
         this.processing = false;
      }
   };

   public ReHubCommand() {
      elegant.getInstance().getEventManager().subscribe(this);
   }

   public Command command() {
      return CommandBuilder.begin("rct").aliases("reconnect").desc("commands.rehub.description").handler(this::handle).build();
   }

   private void handle(CommandContext ctx) {
      if (mc.player != null && mc.world != null) {
         if (ServerUtility.hasCT) {
            MessageUtility.error(Text.of(Localizator.translate("commands_rehub.ct")));
         } else {
            this.timer.reset();
            mc.player.networkHandler.sendChatCommand("hub");
            this.processing = true;
         }
      }
   }
}
