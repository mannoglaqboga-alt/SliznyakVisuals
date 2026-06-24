package moscow.elegant.systems.modules.listeners;

import moscow.elegant.elegant;
import moscow.elegant.systems.event.EventListener;
import moscow.elegant.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.elegant.systems.modules.Module;

public class ModuleTickListener implements EventListener<ClientPlayerTickEvent> {
   public void onEvent(ClientPlayerTickEvent event) {
      for (Module module : elegant.getInstance().getModuleManager().getModules()) {
         if (module.isEnabled()) {
            module.tick();
         }
      }
   }
}
