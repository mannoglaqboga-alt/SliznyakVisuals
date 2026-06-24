package moscow.elegant.utility.rotations;

import moscow.elegant.elegant;
import moscow.elegant.systems.event.EventListener;
import moscow.elegant.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.elegant.systems.event.impl.player.InputEvent;
import moscow.elegant.systems.event.impl.render.Render3DEvent;

public class RotationUpdateListener {
   private final EventListener<ClientPlayerTickEvent> onTick = event -> elegant.getInstance().getRotationHandler().update();
   private final EventListener<Render3DEvent> onRender = event -> elegant.getInstance().getRotationHandler().updateRender(event.getTickDelta());
   private final EventListener<InputEvent> onInputEvent = event -> {
      RotationHandler rotationHandler = elegant.INSTANCE.getRotationHandler();
      RotationTask currentTask = rotationHandler.getCurrentTask();
      if (!rotationHandler.isIdling() && currentTask != null && currentTask.getMoveCorrection() == MoveCorrection.SILENT) {
         event.setYaw(elegant.getInstance().getRotationHandler().getCurrentRotation().getYaw());
      }
   };

   public RotationUpdateListener() {
      elegant.getInstance().getEventManager().subscribe(this);
   }
}
