package moscow.elegant.ui.components.popup.list;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.elegant.elegant;
import moscow.elegant.framework.base.UIContext;
import moscow.elegant.framework.msdf.Font;
import moscow.elegant.framework.msdf.Fonts;
import moscow.elegant.framework.objects.MouseButton;
import moscow.elegant.systems.localization.Localizator;
import moscow.elegant.ui.components.popup.Popup;
import moscow.elegant.ui.components.popup.PopupAction;
import moscow.elegant.ui.components.popup.PopupComponent;
import moscow.elegant.utility.animation.base.Animation;
import moscow.elegant.utility.animation.base.Easing;
import moscow.elegant.utility.colors.ColorRGBA;
import moscow.elegant.utility.colors.Colors;
import moscow.elegant.utility.game.cursor.CursorType;
import moscow.elegant.utility.game.cursor.CursorUtility;
import moscow.elegant.utility.gui.GuiUtility;

public class Button extends PopupComponent {
   private final Popup popup;
   private final String text;
   private final String icon;
   private PopupAction action;
   private final Animation hoverAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);

   @Override
   protected void renderComponent(UIContext context) {
      Font nameFont = Fonts.REGULAR.getFont(8.0F);
      float nameLeftPadding = 8.0F;
      float nameHeight = nameFont.height();
      this.hoverAnimation.update(this.isHovered(context.getMouseX(), context.getMouseY()));
      if (this.isHovered(context.getMouseX(), context.getMouseY())) {
         CursorUtility.set(CursorType.HAND);
      }

      ColorRGBA color = !this.text.equals(Localizator.translate("remove")) ? Colors.getTextColor() : ColorRGBA.RED.mix(ColorRGBA.WHITE, 0.3F);
      context.drawFadeoutText(
         nameFont,
         this.text,
         this.x + nameLeftPadding,
         this.y + GuiUtility.getMiddleOfBox(nameHeight, this.height),
         color.withAlpha(RenderSystem.getShaderColor()[3] * 255.0F * (0.75F + 0.25F * this.hoverAnimation.getValue())),
         0.8F,
         1.0F,
         this.width - 24.0F
      );
      context.drawTexture(
         elegant.id(this.icon),
         this.x + this.width - 16.0F,
         this.y + 6.0F,
         8.0F,
         8.0F,
         color.withAlpha(RenderSystem.getShaderColor()[3] * 255.0F * (0.75F + 0.25F * this.hoverAnimation.getValue()))
      );
      if (this.isHovered(context)) {
         CursorUtility.set(CursorType.HAND);
      }
   }

   @Override
   public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
      if (this.isHovered(mouseX, mouseY) && button == MouseButton.LEFT) {
         this.action.run(this.popup);
      }

      super.onMouseClicked(mouseX, mouseY, button);
   }

   @Override
   public float getHeight() {
      return this.height = 19.0F;
   }

   @Generated
   public Button(Popup popup, String text, String icon, PopupAction action) {
      this.popup = popup;
      this.text = text;
      this.icon = icon;
      this.action = action;
   }
}
