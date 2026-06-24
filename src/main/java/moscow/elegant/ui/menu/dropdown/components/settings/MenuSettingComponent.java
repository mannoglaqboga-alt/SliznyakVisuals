package moscow.elegant.ui.menu.dropdown.components.settings;

import lombok.Generated;
import moscow.elegant.elegant;
import moscow.elegant.framework.base.CustomComponent;
import moscow.elegant.framework.base.UIContext;
import moscow.elegant.systems.localization.Localizator;
import moscow.elegant.systems.setting.Setting;
import moscow.elegant.ui.components.popup.Popup;
import moscow.elegant.ui.menu.dropdown.DropDownScreen;
import moscow.elegant.ui.menu.dropdown.components.module.ModuleComponent;
import moscow.elegant.utility.animation.base.Animation;
import moscow.elegant.utility.animation.base.Easing;

public abstract class MenuSettingComponent<T extends Setting> extends CustomComponent {
   private final CustomComponent parent;
   protected final T setting;
   private final Animation visibilityAnimation = new Animation(300L, Easing.BAKEK_PAGES);
   protected final Animation hoverAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);

   public MenuSettingComponent(T setting, CustomComponent parent) {
      this.parent = parent;
      this.setting = setting;
   }

   @Override
   public void update(UIContext context) {
      String translatedDescription = Localizator.translateOrEmpty(this.setting.getDescription());
      if (this.parent instanceof ModuleComponent component
         && (component.getParent().isHovered(context) && this.isHovered(context) || elegant.getInstance().getMenuScreen() instanceof DropDownScreen)) {
         ((DropDownScreen)elegant.getInstance().getMenuScreen()).setDesc(translatedDescription);
      }

      if (this.parent instanceof Popup && this.isHovered(context)) {
         elegant.getInstance().getHud().setDesc(translatedDescription);
      }

      super.update(context);
   }

   @Override
   public void onInit() {
      super.onInit();
   }

   public float getOpacity() {
      return this.visibilityAnimation.getValue();
   }

   public void drawRegular8(UIContext context) {
   }

   public void drawSplit(UIContext context) {
   }

   @Generated
   public CustomComponent getParent() {
      return this.parent;
   }

   @Generated
   public T getSetting() {
      return this.setting;
   }

   @Generated
   public Animation getVisibilityAnimation() {
      return this.visibilityAnimation;
   }

   @Generated
   public Animation getHoverAnimation() {
      return this.hoverAnimation;
   }
}
