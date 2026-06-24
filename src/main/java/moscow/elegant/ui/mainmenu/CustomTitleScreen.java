package moscow.elegant.ui.mainmenu;

import java.util.ArrayList;
import java.util.List;
import moscow.elegant.elegant;
import moscow.elegant.framework.base.CustomScreen;
import moscow.elegant.framework.base.UIContext;
import moscow.elegant.framework.msdf.Font;
import moscow.elegant.framework.msdf.Fonts;
import moscow.elegant.framework.objects.BorderRadius;
import moscow.elegant.framework.objects.MouseButton;
import moscow.elegant.systems.modules.modules.other.Sounds;
import moscow.elegant.systems.modules.modules.visuals.Interface;
import moscow.elegant.utility.colors.ColorRGBA;
import moscow.elegant.utility.colors.Colors;
import moscow.elegant.utility.interfaces.IMinecraft;
import moscow.elegant.utility.render.DrawUtility;
import moscow.elegant.utility.sounds.ClientSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import ru.kotopushka.compiler.sdk.annotations.Compile;
import ru.kotopushka.compiler.sdk.annotations.VMProtect;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

public class CustomTitleScreen extends CustomScreen implements IMinecraft {
   private static boolean once;
   private final List<float[]> menuParticles = new ArrayList<>();
   private long lastParticleUpdate = 0;
   private static final String[] BUTTON_LABELS = new String[]{
      "SinglePlayer",
      "MultiPlayer",
      "Alt Manager",
      "Options",
      "Quit"
   };

   @Compile
   @VMProtect(
      type = VMProtectType.MUTATION
   )
   protected void init() {
      if (!once) {
         if (elegant.getInstance().getModuleManager().getModule(Sounds.class).isEnabled()) {
            ClientSounds.WELCOME.play(elegant.getInstance().getModuleManager().getModule(Sounds.class).getVolume().getCurrentValue());
         }
         // init basic particles
         this.menuParticles.clear();
         for (int i = 0; i < 25; i++) {
            this.menuParticles.add(new float[] {
               (float)(Math.random() * this.width),
               (float)(Math.random() * this.height),
               (float)(Math.random() * 2 + 1),
               (float)(Math.random() * 0.5 + 0.2)
            });
         }
         once = true;
      }

      super.init();
   }

   @Override
   public void render(UIContext context) {
      if (Fonts.isInitialized()) {
         Font titleFont = Fonts.ROUND_BOLD.getFont(22.0F);
         Font subtitleFont = Fonts.SEMIBOLD.getFont(10.5F);
         Font buttonFont = Fonts.MEDIUM.getFont(9.0F);
         Font smallFont = Fonts.MEDIUM.getFont(8.0F);

         // Force ONLY black/dark theme in main menu (ignore global theme)
         ColorRGBA bg = new ColorRGBA(10.0F, 10.0F, 15.0F, 160.0F);
         context.drawRoundedRect(0.0F, 0.0F, this.width, this.height, BorderRadius.ZERO, bg);
         DrawUtility.blurProgram.draw();

         float buttonWidth = 140.0F;
         float buttonHeight = 22.0F;
         float buttonOffset = 6.0F;
         int buttonCount = BUTTON_LABELS.length;
         float totalButtonsHeight = buttonCount * buttonHeight + (buttonCount - 1) * buttonOffset;

         float iconSize = 22.0F;
         float spaceIconToGreet = 6.0F;
         float greetHeight = subtitleFont.height();
         float spaceGreetToButtons = 10.0F;
         float totalHeight = iconSize + spaceIconToGreet + greetHeight + spaceGreetToButtons + totalButtonsHeight;

         float topY = (this.height - totalHeight) / 2.0F;

         // SliznyakVisual always white + beautiful font (ROUND_BOLD)
         ColorRGBA logoColor = ColorRGBA.WHITE;
         context.drawCenteredText(
            titleFont,
            "SliznyakVisual",
            this.width / 2.0F,
            topY + iconSize / 4.0F,
            logoColor
         );

         String lang = mc.getLanguageManager().getLanguage();
         java.time.LocalTime now = java.time.LocalTime.now();
         String timeOfDay;
         if (now.isBefore(java.time.LocalTime.of(6, 0))) {
            timeOfDay = lang.equals("ru_ru") ? "Доброй ночи" : "Good night";
         } else if (now.isBefore(java.time.LocalTime.NOON)) {
            timeOfDay = lang.equals("ru_ru") ? "Доброе утро" : "Good morning";
         } else if (now.isBefore(java.time.LocalTime.of(18, 0))) {
            timeOfDay = lang.equals("ru_ru") ? "Добрый день" : "Good afternoon";
         } else {
            timeOfDay = lang.equals("ru_ru") ? "Добрый вечер" : "Good evening";
         }

         String username = mc.getSession() != null ? mc.getSession().getUsername() : "Player";
         String greet = timeOfDay + ", " + username + "!";

         float greetY = topY + iconSize + spaceIconToGreet;
         // Greet text ("Good night", "Добрый день" etc.) always white
         ColorRGBA greetColor = ColorRGBA.WHITE;
         context.drawCenteredText(
            subtitleFont,
            greet,
            this.width / 2.0F,
            greetY,
            greetColor
         );

         // simple digital clock - nice light color on black theme
         String clock = String.format("%02d:%02d", now.getHour(), now.getMinute());
         context.drawCenteredText(
            smallFont,
            clock,
            this.width / 2.0F,
            greetY + greetHeight + 2,
            new ColorRGBA(220, 220, 230)
         );

         // update and draw basic particles
         long pnow = System.currentTimeMillis();
         if (pnow - this.lastParticleUpdate > 16) {
            for (float[] p : this.menuParticles) {
               p[0] += (float)(Math.sin(pnow / 800.0) * 0.3);
               p[1] += (float)(Math.cos(pnow / 700.0) * 0.2 + 0.05);
               if (p[0] < 0) p[0] = this.width;
               if (p[0] > this.width) p[0] = 0;
               if (p[1] < 0) p[1] = this.height;
               if (p[1] > this.height) p[1] = 0;
            }
            this.lastParticleUpdate = pnow;
         }
         for (float[] p : this.menuParticles) {
            float alpha = (float)(0.25 + Math.sin(pnow / 500.0 + p[3]) * 0.15);
            // Always dark theme particles (accent on black menu)
            ColorRGBA particleColor = Colors.getAccentColor().withAlpha(255.0F * alpha * 0.65F);
            context.drawRoundedRect(p[0] - p[2]/2, p[1] - p[2]/2, p[2], p[2], BorderRadius.all(p[2]), particleColor);
         }

         float startY = greetY + greetHeight + spaceGreetToButtons + 14;
         float x = this.width / 2.0F - buttonWidth / 2.0F;

         float mouseX = (float)context.getMouseX();
         float mouseY = (float)context.getMouseY();

         boolean isGlass = Interface.showGlass();

         for (int i = 0; i < buttonCount; i++) {
            float y = startY + i * (buttonHeight + buttonOffset);
            boolean hovered = mouseX >= x && mouseX <= x + buttonWidth && mouseY >= y && mouseY <= y + buttonHeight;

            float hoverFactor = hovered ? 1.05F : 1.0F;
            float bx = x + (buttonWidth * (1 - hoverFactor)) / 2;
            float by = y + (buttonHeight * (1 - hoverFactor)) / 2;
            float bw = buttonWidth * hoverFactor;
            float bh = buttonHeight * hoverFactor;

            // Always white text + dark buttons (black theme only in main menu)
            ColorRGBA textColor = ColorRGBA.WHITE;

            if (isGlass) {
               ColorRGBA glassBase = Colors.getLiquidGlassColor().withAlpha(hovered ? 245 : 205);
               context.drawLiquidGlass(bx, by, bw, bh, 15.0F, 0.04F, BorderRadius.all(15.0F), glassBase);
            } else {
               ColorRGBA baseButton = new ColorRGBA(40, 40, 45, hovered ? 240 : 200);
               context.drawRoundedRect(bx, by, bw, bh, BorderRadius.all(12.0F), baseButton);
            }

            Font btnFont = Fonts.MEDIUM.getFont(hovered ? 10.5F : 9.5F);
            context.drawCenteredText(
               btnFont,
               BUTTON_LABELS[i],
               this.width / 2.0F,
               by + bh / 2.0F - btnFont.height() / 2.0F + 0.5F,
               textColor
            );
         }
      }

   }

   @Compile
   @Override
   public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
      float buttonWidth = 140.0F;
      float buttonHeight = 22.0F;
      float buttonOffset = 6.0F;
      int buttonCount = BUTTON_LABELS.length;
      float totalButtonsHeight = buttonCount * buttonHeight + (buttonCount - 1) * buttonOffset;

      float iconSize = 22.0F;
      float spaceIconToGreet = 6.0F;
      float greetHeight = Fonts.SEMIBOLD.getFont(10.5F).height();
      float spaceGreetToButtons = 10.0F;
      float totalHeight = iconSize + spaceIconToGreet + greetHeight + spaceGreetToButtons + totalButtonsHeight;
      float topY = (this.height - totalHeight) / 2.0F;
      float greetY = topY + iconSize + spaceIconToGreet;
      float startY = greetY + greetHeight + spaceGreetToButtons + 14;
      float x = this.width / 2.0F - buttonWidth / 2.0F;

      for (int i = 0; i < buttonCount; i++) {
         float y = startY + i * (buttonHeight + buttonOffset);
         boolean hovered = mouseX >= x && mouseX <= x + buttonWidth && mouseY >= y && mouseY <= y + buttonHeight;
         if (hovered && button == MouseButton.LEFT) {
            switch (i) {
               case 0 -> mc.setScreen(new SelectWorldScreen(this));
               case 1 -> mc.setScreen(new MultiplayerScreen(this));
               case 2 -> mc.setScreen(new AltManager(this));
               case 3 -> mc.setScreen(new OptionsScreen(this, mc.options));
               case 4 -> mc.stop();
            }
            return;
         }
      }

      super.onMouseClicked(mouseX, mouseY, button);
   }

   @Compile
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (Screen.hasControlDown() && keyCode == 82) {
         MinecraftClient.getInstance().setScreen(new MultiplayerScreen(this));
      }

      if (Screen.hasControlDown() && keyCode == 84) {
         MinecraftClient.getInstance().setScreen(new SelectWorldScreen(this));
      }

      return super.keyPressed(keyCode, scanCode, modifiers);
   }
}
