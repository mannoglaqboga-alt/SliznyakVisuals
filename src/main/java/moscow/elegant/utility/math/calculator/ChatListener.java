package moscow.elegant.utility.math.calculator;

import moscow.elegant.elegant;
import moscow.elegant.systems.event.EventListener;
import moscow.elegant.systems.event.impl.network.SendPacketEvent;
import moscow.elegant.utility.interfaces.IMinecraft;
import moscow.elegant.utility.math.MathUtility;
import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;

public class ChatListener implements IMinecraft {
   private final EventListener<SendPacketEvent> onSendPacket = event -> {
      if (event.getPacket() instanceof ChatCommandSignedC2SPacket packet) {
         if (mc.player == null) {
            return;
         }

         String message = packet.command();
         if (message.startsWith("ah me")) {
            mc.player.networkHandler.sendChatMessage("/ah " + mc.player.getName().getString());
            event.cancel();
         }

         if (message.startsWith("ah sell ")) {
            String expression = message.replaceFirst("ah sell ", "");
            String result = MathUtility.calculate(expression);
            mc.player.networkHandler.sendChatMessage("/ah sell " + Math.round(Float.parseFloat(result)));
            event.cancel();
         }
      }
   };

   public ChatListener() {
      elegant.getInstance().getEventManager().subscribe(this);
   }
}
