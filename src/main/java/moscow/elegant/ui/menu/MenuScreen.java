package moscow.elegant.ui.menu;

import lombok.Generated;
import moscow.elegant.framework.base.CustomScreen;
import moscow.elegant.utility.animation.base.Animation;
import moscow.elegant.utility.animation.base.Easing;

public abstract class MenuScreen extends CustomScreen {
   protected final Animation menuAnimation = new Animation(500L, Easing.LINEAR);
   protected boolean closing = true;

   @Generated
   public Animation getMenuAnimation() {
      return this.menuAnimation;
   }

   @Generated
   public boolean isClosing() {
      return this.closing;
   }

   @Generated
   public void setClosing(boolean closing) {
      this.closing = closing;
   }
}
