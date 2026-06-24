package moscow.elegant.systems.event.impl.network;

import lombok.Generated;
import moscow.elegant.systems.event.EventCancellable;
import net.minecraft.network.packet.Packet;

public class SendPacketEvent extends EventCancellable {
   private Packet<?> packet;

   @Generated
   public Packet<?> getPacket() {
      return this.packet;
   }

   @Generated
   public void setPacket(Packet<?> packet) {
      this.packet = packet;
   }

   @Generated
   public SendPacketEvent(Packet<?> packet) {
      this.packet = packet;
   }
}
