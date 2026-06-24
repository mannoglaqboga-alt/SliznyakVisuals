package moscow.elegant.ui.components.popup.list;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.elegant.framework.base.UIContext;
import moscow.elegant.framework.msdf.Font;
import moscow.elegant.framework.msdf.Fonts;
import moscow.elegant.systems.localization.Localizator;
import moscow.elegant.ui.components.popup.PopupComponent;
import moscow.elegant.utility.colors.Colors;
import moscow.elegant.utility.gui.GuiUtility;

public class Title extends PopupComponent {
   private final String text;

   public Title(String text) {
      this.text = text;
   }

   @Override
   protected void renderComponent(UIContext context) {
      Font nameFont = Fonts.REGULAR.getFont(8.0F);
      float nameLeftPadding = 8.0F;
      float nameHeight = nameFont.height();
      context.drawFadeoutText(
         nameFont,
         this.text != null && !this.text.trim().isEmpty() ? Localizator.translate(this.text) : " ",
         this.x + nameLeftPadding,
         this.y + GuiUtility.getMiddleOfBox(nameHeight, this.height),
         Colors.getTextColor().withAlpha(RenderSystem.getShaderColor()[3] * 255.0F),
         0.8F,
         1.0F,
         this.width - 12.0F
      );
   }

   @Override
   public float getHeight() {
      return this.height = 18.0F;
   }
}
