package moscow.elegant.systems.modules.modules.movement;

import moscow.elegant.systems.event.EventListener;
import moscow.elegant.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.elegant.systems.modules.api.ModuleCategory;
import moscow.elegant.systems.modules.api.ModuleInfo;
import moscow.elegant.systems.modules.impl.BaseModule;

@ModuleInfo(
   name = "Auto Sprint",
   category = ModuleCategory.MOVEMENT,
   enabledByDefault = true
)
public class AutoSprint extends BaseModule {
   private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> mc.options.sprintKey.setPressed(true);
}
