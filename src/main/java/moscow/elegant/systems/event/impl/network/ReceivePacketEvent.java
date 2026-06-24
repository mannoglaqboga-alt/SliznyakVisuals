package moscow.elegant.systems.event.impl.network;

import lombok.Generated;
import moscow.elegant.systems.event.EventCancellable;
import net.minecraft.network.packet.Packet;

public class ReceivePacketEvent extends EventCancellable {
   private final Packet<?> packet;

   @Generated
   public Packet<?> getPacket() {
      return this.packet;
   }

   @Generated
   public ReceivePacketEvent(Packet<?> packet) {
      this.packet = packet;
   }
}
