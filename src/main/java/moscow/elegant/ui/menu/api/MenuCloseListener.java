package moscow.elegant.ui.menu.api;

import moscow.elegant.elegant;
import moscow.elegant.framework.base.UIContext;
import moscow.elegant.systems.event.EventListener;
import moscow.elegant.systems.event.impl.render.HudRenderEvent;
import moscow.elegant.systems.modules.modules.visuals.MenuModule;
import moscow.elegant.ui.menu.MenuScreen;
import moscow.elegant.ui.menu.dropdown.DropDownScreen;
import moscow.elegant.utility.interfaces.IMinecraft;
import net.minecraft.client.MinecraftClient;

public class MenuCloseListener implements IMinecraft {
   private final EventListener<HudRenderEvent> onHudRender = event -> {
      MenuScreen menuScreen = elegant.getInstance().getMenuScreen();
      if (mc.currentScreen == null) {
         MenuModule menuModule = elegant.getInstance().getModuleManager().getModule(MenuModule.class);
         if (!(menuScreen instanceof DropDownScreen)) {
            elegant.getInstance().setMenuScreen(new DropDownScreen());
         }
      }

      if (menuScreen != null) {
         menuScreen.getMenuAnimation().update(menuScreen.isClosing() ? 0.0F : 1.0F);
         if (!(mc.currentScreen instanceof MenuScreen) && elegant.getInstance().getModuleManager().getModule(MenuModule.class).isEnabled()) {
            elegant.getInstance().getModuleManager().getModule(MenuModule.class).setEnabled(false);
         }

         if (menuScreen.getMenuAnimation().getValue() > 0.1F && !(mc.currentScreen instanceof MenuScreen) && menuScreen.isClosing()) {
            UIContext context = UIContext.of(event.getContext(), -1, -1, MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));
            menuScreen.render(context);
         }
      }
   };

   public MenuCloseListener() {
      elegant.getInstance().getEventManager().subscribe(this);
   }
}
