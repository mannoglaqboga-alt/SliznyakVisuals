package moscow.elegant.systems.event.impl.render;

import lombok.Generated;
import moscow.elegant.framework.base.CustomDrawContext;
import moscow.elegant.systems.event.Event;

public class ScreenRenderEvent extends Event {
   private final CustomDrawContext context;
   private final float tickDelta;

   @Generated
   public CustomDrawContext getContext() {
      return this.context;
   }

   @Generated
   public float getTickDelta() {
      return this.tickDelta;
   }

   @Generated
   public ScreenRenderEvent(CustomDrawContext context, float tickDelta) {
      this.context = context;
      this.tickDelta = tickDelta;
   }
}
