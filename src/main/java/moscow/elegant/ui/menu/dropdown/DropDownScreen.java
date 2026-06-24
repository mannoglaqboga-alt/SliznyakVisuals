package moscow.elegant.ui.menu.dropdown;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import moscow.elegant.elegant;
import moscow.elegant.framework.base.UIContext;
import moscow.elegant.framework.msdf.Fonts;
import moscow.elegant.framework.objects.BorderRadius;
import moscow.elegant.framework.objects.MouseButton;
import moscow.elegant.systems.localization.Localizator;
import moscow.elegant.systems.modules.Module;
import moscow.elegant.systems.modules.modules.other.Sounds;
import moscow.elegant.systems.modules.modules.visuals.Interface;
import moscow.elegant.systems.modules.modules.visuals.MenuModule;
import moscow.elegant.systems.theme.Theme;
import moscow.elegant.ui.components.ColorPicker;
import moscow.elegant.ui.components.animated.AnimatedText;
import moscow.elegant.ui.components.textfield.FieldAction;
import moscow.elegant.ui.components.textfield.TextField;
import moscow.elegant.ui.menu.MenuScreen;
import moscow.elegant.ui.menu.api.MenuCategory;
import moscow.elegant.ui.menu.dropdown.components.MenuPanel;
import moscow.elegant.ui.menu.dropdown.components.module.ModuleComponent;
import moscow.elegant.ui.menu.dropdown.components.settings.impl.BezierSettingComponent;
import moscow.elegant.ui.menu.dropdown.components.settings.impl.BindSettingComponent;
import moscow.elegant.ui.menu.dropdown.components.settings.impl.BooleanSettingComponent;
import moscow.elegant.ui.menu.dropdown.components.settings.impl.ButtonSettingComponent;
import moscow.elegant.ui.menu.dropdown.components.settings.impl.ColorSettingComponent;
import moscow.elegant.ui.menu.dropdown.components.settings.impl.ModeSettingComponent;
import moscow.elegant.ui.menu.dropdown.components.settings.impl.RangeSettingComponent;
import moscow.elegant.ui.menu.dropdown.components.settings.impl.SliderSettingComponent;
import moscow.elegant.ui.menu.dropdown.components.settings.impl.StringSettingComponent;
import moscow.elegant.utility.animation.base.Animation;
import moscow.elegant.utility.animation.base.Easing;
import moscow.elegant.utility.colors.ColorRGBA;
import moscow.elegant.utility.colors.Colors;
import moscow.elegant.utility.gui.GuiUtility;
import moscow.elegant.utility.interfaces.IMinecraft;
import moscow.elegant.utility.math.MathUtility;
import moscow.elegant.utility.render.DrawUtility;
import moscow.elegant.utility.render.RenderUtility;
import moscow.elegant.utility.render.ScissorUtility;
import moscow.elegant.utility.render.batching.Batching;
import moscow.elegant.utility.render.batching.impl.FontBatching;
import moscow.elegant.utility.render.batching.impl.IconBatching;
import moscow.elegant.utility.render.batching.impl.RectBatching;
import moscow.elegant.utility.sounds.ClientSounds;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.screen.Screen;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class DropDownScreen extends MenuScreen implements IMinecraft {
   private final Animation searchAnimation = new Animation(300L, Easing.BAKEK);
   private final Animation appendingAnim = new Animation(300L, Easing.BAKEK);
   private final Animation configAnim = new Animation(250L, Easing.BAKEK);
   private boolean closing;
   private final List<MenuPanel> panels = Arrays.stream(MenuCategory.values()).map(MenuPanel::new).toList();
   private final java.util.Map<moscow.elegant.ui.menu.api.MenuCategory, moscow.elegant.systems.modules.Module> openedSettings = new java.util.HashMap<>();
   private float panelWidth;
   private float panelHeight;
   private String desc = "";
   private AnimatedText descText;
   private final List<ColorPicker> colorPickers = new ArrayList<>();
   private TextField searchField;
   private TextField configNameField;
   private boolean showConfigs = false;

   @Compile
   protected void init() {
      this.descText = new AnimatedText(Fonts.REGULAR.getFont(10.0F), 10.0F, 300L, Easing.BAKEK).centered();
      this.closing = false;
      this.panelWidth = 115.0F;
      this.panelHeight = 240.0F;

      for (MenuPanel panel : this.panels) {
         panel.setWidth(this.panelWidth);
         panel.setHeight(this.panelHeight);
         panel.onInit();
      }

      // restore previously opened module settings
      for (MenuPanel panel : this.panels) {
         Module remembered = this.openedSettings.get(panel.getCategory());
         if (remembered != null) {
            for (ModuleComponent comp : panel.getModuleComponents()) {
               if (comp.getModule() == remembered) {
                  panel.setSelectedModuleComponent(comp);
                  break;
               }
            }
         }
      }

      this.searchField = new TextField(Fonts.REGULAR.getFont(12.0F));
      this.configNameField = new TextField(Fonts.MEDIUM.getFont(8.0F));
      this.configNameField.setPreview("config name");
      Map<String, FieldAction> append = new HashMap<>();

      for (Module module : elegant.getInstance().getModuleManager().getModules()) {
         if (!module.isHidden()) {
            FieldAction action = new FieldAction(
               module::toggle,
               () -> this.panels
                  .forEach(panelx -> panelx.getModuleComponents().stream().filter(component -> component.getModule() == module).forEach(ModuleComponent::open))
            );
            append.put(module.getName().replace(" ", ""), action);
            append.put(module.getName(), action);
         }
      }

      this.searchField.setAppend(append);
      super.init();
   }

   public void tick() {
      this.handleMovementKeys();
      super.tick();
   }

   @Compile
   @Override
   public void render(UIContext context) {
      this.menuAnimation.setEasing(Easing.LINEAR);
      this.menuAnimation.update(this.isClosing() ? 0.0F : 1.0F);
      this.menuAnimation.setDuration(this.isClosing() ? 300L : 500L);
      this.configAnim.update(showConfigs ? 1.0F : 0.0F);
      this.desc = "";

      float spacing = 10.0F;
      float containerWidth = 650.0F;
      float containerHeight = 275.0F;
      float containerX = (this.width - containerWidth) / 2.0F;
      float containerY = (this.height - containerHeight) / 2.0F;

      float x = containerX + (containerWidth - (this.panelWidth + spacing) * this.panels.size() + spacing) / 2.0F;
      float y = containerY + (containerHeight - this.panelHeight) / 2.0F;

      float revealProgress = this.menuAnimation.getValue();
      float scale = MathUtility.interpolate(1.06F, 1.0F, revealProgress);
      float centerX = containerX + containerWidth / 2.0F;
      float centerY = containerY + containerHeight / 2.0F;

      context.pushMatrix();
      context.getMatrices().translate(centerX, centerY, 0.0F);
      context.getMatrices().scale(scale, scale, 1.0F);
      context.getMatrices().translate(-centerX, -centerY, 0.0F);

      boolean darkTheme = elegant.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
      float cardAlpha = this.menuAnimation.getValue();

      if (Interface.showMinimalizm()) {
         context.drawBlurredRect(
            containerX,
            containerY,
            containerWidth,
            containerHeight,
            18.0F,
            BorderRadius.all(12.0F),
            ColorRGBA.WHITE.withAlpha(220.0F * cardAlpha)
         );
      }

      if (Interface.showGlass()) {
         context.drawLiquidGlass(
            containerX,
            containerY,
            containerWidth,
            containerHeight,
            8.0F,
            0.12F,
            BorderRadius.all(12.0F),
            Colors.getLiquidGlassColor().withAlpha(255.0F * cardAlpha)
         );
      }

      context.drawRoundedRect(
         containerX,
         containerY,
         containerWidth,
         containerHeight,
         BorderRadius.all(12.0F),
         Colors.getBackgroundColor().withAlpha(255.0F * (darkTheme ? 0.94F : 0.85F) * cardAlpha)
      );

      float offset = 0.0F;

      for (MenuPanel panel : this.panels) {
         panel.setX(
            MathUtility.interpolate(x + offset, centerX - this.panelWidth / 2.0F, this.closing ? 1.0F - this.menuAnimation.getValue() : 0.0)
         );
         panel.setY(y);
         panel.setWidth(this.panelWidth);
         panel.setHeight(this.panelHeight);
         offset += this.panelWidth + spacing;
      }

      if (Interface.showGlass()) {
         DrawUtility.updateBuffer();
      }

      for (MenuPanel panel : this.panels) {
         panel.renderBlur(context);
      }

      for (MenuPanel panel : this.panels) {
         panel.render(context);
      }

      Batching icon = new IconBatching(VertexFormats.POSITION_TEXTURE_COLOR, context.getMatrices());

      for (MenuPanel panel : this.panels) {
         panel.drawType(context);
      }

      icon.draw();

      for (MenuPanel panel : this.panels) {
         this.scissor(context, panel, () -> {
            Batching font = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.REGULAR);
            panel.drawRegular8(context);
            font.draw();
            Batching icon1 = new IconBatching(VertexFormats.POSITION_TEXTURE_COLOR, context.getMatrices());
            panel.drawIcons(context);
            icon1.draw();
            Batching split = new RectBatching(VertexFormats.POSITION_COLOR, context.getMatrices());
            panel.drawSplit(context);
            split.draw();
         });
      }

      // === CONFIG BUTTON / PANEL ON THE RIGHT ===
      float configBtnX = containerX + containerWidth + 10;
      float configBtnY = containerY + (containerHeight - 18) / 2.0F;
      float configBtnW = 85;
      float configBtnH = 18;

      float menuAlpha = this.menuAnimation.getValue();
      float panelAlpha = menuAlpha * this.configAnim.getValue();
      boolean dark = elegant.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;

      if (!showConfigs) {
         // Nicely styled Configs button with hover animation
         boolean cfgHover = GuiUtility.isHovered(configBtnX, configBtnY, configBtnW, configBtnH, context.getMouseX(), context.getMouseY());
         float btnScale = cfgHover ? 1.08f : 1.0f;

         context.getMatrices().push();
         float bCx = configBtnX + configBtnW / 2f;
         float bCy = configBtnY + configBtnH / 2f;
         context.getMatrices().translate(bCx, bCy, 0);
         context.getMatrices().scale(btnScale, btnScale, 1);
         context.getMatrices().translate(-bCx, -bCy, 0);

         // Background with better styling
         ColorRGBA btnBg = cfgHover 
            ? Colors.getAccentColor().withAlpha(210 * menuAlpha) 
            : Colors.getBackgroundColor().withAlpha(220 * menuAlpha);
         context.drawRoundedRect(configBtnX, configBtnY, configBtnW, configBtnH, BorderRadius.all(5.0F), btnBg);

         // Small icon (config-like)
         float iconX = configBtnX + 6;
         float iconY = configBtnY + 4;
         context.drawRoundedRect(iconX, iconY, 7, 9, BorderRadius.all(1.5F), Colors.getTextColor().withAlpha(180 * menuAlpha));
         context.drawRoundedRect(iconX + 1.5f, iconY + 2, 4, 1, BorderRadius.all(0.5F), Colors.getBackgroundColor().withAlpha(200 * menuAlpha));

         context.drawText(Fonts.MEDIUM.getFont(7.0F), "Configs", configBtnX + 17, configBtnY + 5, Colors.getTextColor().withAlpha(255 * menuAlpha));

         context.getMatrices().pop();
      } else {
         // Full compact panel when opened
         float configW = 155;
         float configH = 160;
         float configX = containerX + containerWidth + 8;
         float configY = containerY + containerHeight - configH - 5;

         context.pushMatrix();
         float pCx = configX + configW / 2f;
         float pCy = configY + configH / 2f;
         float pScale = 0.85f + 0.15f * panelAlpha;
         context.getMatrices().translate(pCx, pCy, 0);
         context.getMatrices().scale(pScale, pScale, 1);
         context.getMatrices().translate(-pCx, -pCy, 0);

         if (Interface.showMinimalizm()) {
            context.drawBlurredRect(configX, configY, configW, configH, 8.0F, BorderRadius.all(6.0F), ColorRGBA.WHITE.withAlpha(160 * panelAlpha));
         }
         if (Interface.showGlass()) {
            context.drawLiquidGlass(configX, configY, configW, configH, 6.0F, 0.08F, BorderRadius.all(6.0F), Colors.getLiquidGlassColor().withAlpha(180 * panelAlpha));
         }
         context.drawRoundedRect(configX, configY, configW, configH, BorderRadius.all(6.0F), Colors.getBackgroundColor().withAlpha(230 * panelAlpha * (dark ? 0.96F : 0.92F)));

         // Back button (hide)
         float backW = 50;
         float backH = 14;
         boolean backHover = GuiUtility.isHovered(configX + configW - backW - 6, configY + 5, backW, backH, context.getMouseX(), context.getMouseY());
         context.drawRoundedRect(configX + configW - backW - 6, configY + 5, backW, backH, BorderRadius.all(3.0F), backHover ? Colors.getAccentColor().withAlpha(180 * panelAlpha) : Colors.getBackgroundColor().withAlpha(150 * panelAlpha));
         context.drawText(Fonts.REGULAR.getFont(6.0F), "Back", configX + configW - backW + 5, configY + 8, Colors.getTextColor().withAlpha(255 * panelAlpha));

         // Title
         context.drawText(Fonts.MEDIUM.getFont(8.0F), "Configs", configX + 8, configY + 6, Colors.getTextColor().withAlpha(255 * panelAlpha));

         // Name field
         float nameY = configY + 20;
         this.configNameField.setX(configX + 6);
         this.configNameField.setY(nameY);
         this.configNameField.setWidth(configW - 12);
         this.configNameField.setHeight(15);
         this.configNameField.setAlpha(panelAlpha);
         this.configNameField.render(context);

         // Buttons with Delete + hover animations
         float btnY = nameY + 20;
         float btnH = 15;
         float gap = 5;
         float btnW = (configW - 12 - gap * 2) / 3f;

         float saveX = configX + 6;
         float loadX = saveX + btnW + gap;
         float delX = loadX + btnW + gap;

         // Save button with scale anim
         boolean saveH = GuiUtility.isHovered(saveX, btnY, btnW, btnH, context.getMouseX(), context.getMouseY());
         float saveS = saveH ? 1.06f : 1.0f;
         context.getMatrices().push();
         float sCx = saveX + btnW / 2f;
         float sCy = btnY + btnH / 2f;
         context.getMatrices().translate(sCx, sCy, 0);
         context.getMatrices().scale(saveS, saveS, 1);
         context.getMatrices().translate(-sCx, -sCy, 0);
         context.drawRoundedRect(saveX, btnY, btnW, btnH, BorderRadius.all(3.0F), saveH ? Colors.getAccentColor().withAlpha(220 * panelAlpha) : Colors.getBackgroundColor().withAlpha(190 * panelAlpha));
         context.drawText(Fonts.REGULAR.getFont(6.5F), "Save", saveX + btnW/2 - 8, btnY + 4, Colors.getTextColor().withAlpha(255 * panelAlpha));
         context.getMatrices().pop();

         // Load button with scale anim
         boolean loadH = GuiUtility.isHovered(loadX, btnY, btnW, btnH, context.getMouseX(), context.getMouseY());
         float loadS = loadH ? 1.06f : 1.0f;
         context.getMatrices().push();
         float lCx = loadX + btnW / 2f;
         float lCy = btnY + btnH / 2f;
         context.getMatrices().translate(lCx, lCy, 0);
         context.getMatrices().scale(loadS, loadS, 1);
         context.getMatrices().translate(-lCx, -lCy, 0);
         context.drawRoundedRect(loadX, btnY, btnW, btnH, BorderRadius.all(3.0F), loadH ? Colors.getAccentColor().withAlpha(220 * panelAlpha) : Colors.getBackgroundColor().withAlpha(190 * panelAlpha));
         context.drawText(Fonts.REGULAR.getFont(6.5F), "Load", loadX + btnW/2 - 8, btnY + 4, Colors.getTextColor().withAlpha(255 * panelAlpha));
         context.getMatrices().pop();

         // Delete button (red + scale anim)
         boolean delH = GuiUtility.isHovered(delX, btnY, btnW, btnH, context.getMouseX(), context.getMouseY());
         float delS = delH ? 1.06f : 1.0f;
         context.getMatrices().push();
         float dCx = delX + btnW / 2f;
         float dCy = btnY + btnH / 2f;
         context.getMatrices().translate(dCx, dCy, 0);
         context.getMatrices().scale(delS, delS, 1);
         context.getMatrices().translate(-dCx, -dCy, 0);
         ColorRGBA delBg = delH 
            ? new ColorRGBA(255, 90, 90).withAlpha(230 * panelAlpha) 
            : new ColorRGBA(180, 55, 55).withAlpha(200 * panelAlpha);
         context.drawRoundedRect(delX, btnY, btnW, btnH, BorderRadius.all(3.0F), delBg);
         context.drawText(Fonts.REGULAR.getFont(6.5F), "Delete", delX + btnW/2 - 11, btnY + 4, Colors.getTextColor().withAlpha(255 * panelAlpha));
         context.getMatrices().pop();

         // List with animations and nice delete
         context.drawText(Fonts.REGULAR.getFont(6.5F), "List:", configX + 6, btnY + btnH + 6, Colors.getTextColor().withAlpha(200 * panelAlpha));

         moscow.elegant.systems.config.ConfigManager cm = elegant.getInstance().getConfigManager();
         cm.refresh();
         List<moscow.elegant.systems.config.ConfigFile> cfgs = cm.getConfigFiles();
         float listStartY = btnY + btnH + 14;
         float itemH = 11;
         int maxShow = 6;
         float deleteZoneWidth = 13;

         for (int i = 0; i < Math.min(cfgs.size(), maxShow); i++) {
            moscow.elegant.systems.config.ConfigFile cf = cfgs.get(i);
            float iy = listStartY + i * itemH;
            float rowX = configX + 6;
            float rowWidth = configW - 12;

            boolean rowHover = GuiUtility.isHovered(rowX, iy, rowWidth, itemH, context.getMouseX(), context.getMouseY());
            boolean deleteHover = GuiUtility.isHovered(rowX + rowWidth - deleteZoneWidth, iy, deleteZoneWidth, itemH, context.getMouseX(), context.getMouseY());

            // Staggered appear animation
            float itemProgress = Math.min(1f, Math.max(0f, (panelAlpha * 2.5f - i * 0.15f)));
            float itemAlpha = itemProgress * panelAlpha;

            // Hover scale animation for row
            float rowS = rowHover ? 1.04f : 1.0f;

            context.getMatrices().push();
            float rowCx = rowX + rowWidth / 2f;
            float rowCy = iy + itemH / 2f;
            context.getMatrices().translate(rowCx, rowCy, 0);
            context.getMatrices().scale(rowS, rowS, 1);
            context.getMatrices().translate(-rowCx, -rowCy, 0);

            if (rowHover) {
               context.drawRoundedRect(rowX, iy, rowWidth, itemH, BorderRadius.all(2.0F), Colors.getAccentColor().withAlpha(85 * itemAlpha));
            }

            // Config name
            String name = cf.getFileName();
            context.drawText(Fonts.REGULAR.getFont(6.0F), name, configX + 8, iy + 2, Colors.getTextColor().withAlpha(240 * itemAlpha));

            // Nice delete button on the right (animated)
            if (rowHover) {
               float delBtnX = rowX + rowWidth - deleteZoneWidth + 1;
               float delBtnW = deleteZoneWidth - 2;
               ColorRGBA delCol = deleteHover 
                  ? new ColorRGBA(255, 100, 100).withAlpha(240 * itemAlpha)
                  : new ColorRGBA(200, 60, 60).withAlpha(180 * itemAlpha);
               context.drawRoundedRect(delBtnX, iy + 1.5f, delBtnW, itemH - 3, BorderRadius.all(2.0F), delCol);
               context.drawText(Fonts.MEDIUM.getFont(8.0F), "×", delBtnX + 2.5f, iy + 2, Colors.getTextColor().withAlpha(255 * itemAlpha));
            }

            context.getMatrices().pop();
         }

         context.drawText(Fonts.REGULAR.getFont(5.5F), "Click list to load • Hover right to delete", configX + 6, configY + configH - 12, Colors.getTextColor().withAlpha(130 * panelAlpha));

         context.popMatrix();
      }

      this.searchAnimation.update(this.searchField.isFocused());
      float searchAlpha = this.menuAnimation.getValue() * this.searchAnimation.getValue();
      if (searchAlpha > 0.0F) {
         float searchWidth = 125.0F;
         float searchHeight = 23.0F;
         float searchX = containerX + (containerWidth - searchWidth) / 2.0F;
         float searchY = containerY + containerHeight + 8.0F;

         this.searchField.set(searchX, searchY, searchWidth, searchHeight);
         this.searchField.setAlpha(searchAlpha);
         this.searchField.setTextColor(Colors.getTextColor());

         if (Interface.showMinimalizm()) {
            context.drawBlurredRect(
               this.searchField.getX(),
               this.searchField.getY(),
               this.searchField.getWidth(),
               this.searchField.getHeight(),
               45.0F,
               BorderRadius.all(6.0F),
               ColorRGBA.WHITE.withAlpha(255.0F * searchAlpha)
            );
         }

         if (Interface.showGlass()) {
            context.drawLiquidGlass(
               this.searchField.getX(),
               this.searchField.getY(),
               this.searchField.getWidth(),
               this.searchField.getHeight(),
               2.0F,
               0.08F,
               BorderRadius.all(6.0F),
               ColorRGBA.WHITE.withAlpha(255.0F * searchAlpha)
            );
         }

         context.drawRoundedRect(
            this.searchField.getX(),
            this.searchField.getY(),
            this.searchField.getWidth(),
            this.searchField.getHeight(),
            BorderRadius.all(6.0F),
            Colors.getBackgroundColor().withAlpha(255.0F * (darkTheme ? 0.9F - 0.7F * Interface.glass() : 0.7F) * searchAlpha)
         );

         this.searchField.render(context);
         this.appendingAnim.update(!this.searchField.getAppending().isBlank());
         context.drawCenteredText(
            Fonts.MEDIUM.getFont(11.0F),
            Localizator.translate("search.tooltip.tab"),
            this.width / 2.0F,
            this.height - 65 - 10.0F * searchAlpha * this.appendingAnim.getValue(),
            ColorRGBA.WHITE.withAlpha(150.0F * searchAlpha * this.appendingAnim.getValue())
         );
         context.drawCenteredText(
            Fonts.MEDIUM.getFont(11.0F),
            Localizator.translate("search.tooltip.enter"),
            this.width / 2.0F,
            this.height - 50 - 10.0F * searchAlpha * this.appendingAnim.getValue(),
            ColorRGBA.WHITE.withAlpha(150.0F * searchAlpha * this.appendingAnim.getValue())
         );
      } else {
         this.searchField.clear();
      }

      context.popMatrix();

      if (this.menuAnimation.getValue() < 0.5F) {
         this.desc = "";
      }

      context.drawCenteredText(
         Fonts.MEDIUM.getFont(11.0F),
         Localizator.translate("search.tooltip"),
         this.width / 2.0F,
         this.height - 20 - 10.0F * this.menuAnimation.getValue() * (1.0F - this.searchAnimation.getValue()),
         ColorRGBA.WHITE.withAlpha(150.0F * this.menuAnimation.getValue() * (1.0F - this.searchAnimation.getValue()))
      );
      this.descText.pos(this.width / 2.0F, this.height / 2.0F - 150.0F);
      if (!this.desc.contains(".description")) {
         this.descText.update(this.desc);
         this.descText.render(context);
      }

      for (ColorPicker colorPicker : this.colorPickers) {
         colorPicker.render(context);
         if (!(mc.currentScreen instanceof DropDownScreen)) {
            colorPicker.setShowing(false);
         }
      }

      this.colorPickers.removeIf(popup -> popup.getAnimation().getValue() == 0.0F && !popup.isShowing());
   }

   @Compile
   private void handleMovementKeys() {
      if (mc.player != null && !this.isTyping()) {
         long windowHandle = mc.getWindow().getHandle();
         KeyBinding[] movementKeys = new KeyBinding[]{
            mc.options.forwardKey, mc.options.backKey, mc.options.leftKey, mc.options.rightKey, mc.options.jumpKey
         };

         for (KeyBinding key : movementKeys) {
            int keyCode = InputUtil.fromTranslationKey(key.getBoundKeyTranslationKey()).getCode();
            key.setPressed(InputUtil.isKeyPressed(windowHandle, keyCode));
         }

         if (mc.player.getAbilities().flying) {
            int keyCode = InputUtil.fromTranslationKey(mc.options.sneakKey.getBoundKeyTranslationKey()).getCode();
            mc.options.sneakKey.setPressed(InputUtil.isKeyPressed(windowHandle, keyCode));
         }
      }
   }

   private boolean isTyping() {
      return mc.currentScreen != null && TextField.LAST_FIELD != null && TextField.LAST_FIELD.isFocused();
   }

   public boolean isBindingModule() {
      return this.panels.stream().flatMap(panel -> panel.getModuleComponents().stream()).anyMatch(ModuleComponent::isBindingMode);
   }

   private void scissor(UIContext context, MenuPanel panel, Runnable runnable) {
      panel.scale(context);
      panel.push(context);
      runnable.run();
      ScissorUtility.pop();
      RenderUtility.end(context.getMatrices());
   }

   @Compile
   @Override
   public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
      if (!elegant.getInstance().getHud().getIsland().handleClick((float)mouseX, (float)mouseY, button.getButtonIndex())) {
         for (ColorPicker colorPicker : this.colorPickers) {
            boolean isPick = colorPicker.isPick();
            colorPicker.onMouseClicked(mouseX, mouseY, button);
            if (colorPicker.isHovered(mouseX, mouseY) || isPick) {
               return;
            }

            colorPicker.setShowing(false);
         }

         for (MenuPanel panel : this.panels) {
            if (panel.isHovered(mouseX, mouseY)) {
               panel.onMouseClicked(mouseX, mouseY, button);
            }
         }

         if (this.searchField.isFocused() && button != MouseButton.MIDDLE) {
            this.searchField.onMouseClicked(mouseX, mouseY, button);
         }

         // Config button / panel clicks
         float cfgBtnX = (this.width - 650.0F) / 2.0F + 650.0F + 10;
         float cfgBtnY = (this.height - 275.0F) / 2.0F + (275 - 18) / 2.0F;
         float cfgBtnW = 85;
         float cfgBtnH = 18;

         if (!showConfigs) {
            if (GuiUtility.isHovered(cfgBtnX, cfgBtnY, cfgBtnW, cfgBtnH, mouseX, mouseY) && button == MouseButton.LEFT) {
               showConfigs = true;
               this.configNameField.setFocused(true);
               Sounds soundsModule = elegant.getInstance().getModuleManager().getModule(Sounds.class);
               if (soundsModule != null && soundsModule.isEnabled()) {
                  ClientSounds.CLICKGUI_OPEN.play(soundsModule.getVolume().getCurrentValue(), 1.0F);
               }
            }
         } else {
            // inside the open panel
            float configX = (this.width - 650.0F) / 2.0F + 650.0F + 8;
            float configY = (this.height - 275.0F) / 2.0F + 275 - 160 - 5;
            float configW = 155;
            float configH = 160;

            // Back button
            float backW = 50;
            float backH = 14;
            if (GuiUtility.isHovered(configX + configW - backW - 6, configY + 5, backW, backH, mouseX, mouseY) && button == MouseButton.LEFT) {
               showConfigs = false;
               this.configNameField.setFocused(false);
               Sounds soundsModule = elegant.getInstance().getModuleManager().getModule(Sounds.class);
               if (soundsModule != null && soundsModule.isEnabled()) {
                  ClientSounds.CLICKGUI_OPEN.play(soundsModule.getVolume().getCurrentValue(), 0.8F);
               }
               return;
            }

            if (GuiUtility.isHovered(configX, configY, configW, configH, mouseX, mouseY)) {
               this.configNameField.onMouseClicked(mouseX, mouseY, button);
               if (button == MouseButton.LEFT) {
                  float nameY = configY + 20;
                  float btnY = nameY + 20;
                  float btnH = 15;
                  float gap = 5;
                  float btnW = (configW - 12 - gap * 2) / 3f;

                  float saveX = configX + 6;
                  float loadX = saveX + btnW + gap;
                  float delX = loadX + btnW + gap;

                  moscow.elegant.systems.config.ConfigManager cm = elegant.getInstance().getConfigManager();

                  // Save
                  if (GuiUtility.isHovered(saveX, btnY, btnW, btnH, mouseX, mouseY)) {
                     String name = this.configNameField.getBuiltText().trim();
                     if (name.isEmpty()) name = "default";
                     cm.createConfig(name);
                     elegant.getInstance().getNotificationManager().addNotification(moscow.elegant.systems.notifications.NotificationType.SUCCESS, "Saved: " + name);
                  }

                  // Load
                  if (GuiUtility.isHovered(loadX, btnY, btnW, btnH, mouseX, mouseY)) {
                     String name = this.configNameField.getBuiltText().trim();
                     if (name.isEmpty()) name = "autosave";
                     moscow.elegant.systems.config.ConfigFile cfg = cm.getConfig(name, true);
                     if (cfg != null) {
                        cfg.load();
                        elegant.getInstance().getNotificationManager().addNotification(moscow.elegant.systems.notifications.NotificationType.SUCCESS, "Loaded: " + name);
                     }
                  }

                  // Delete button
                  if (GuiUtility.isHovered(delX, btnY, btnW, btnH, mouseX, mouseY)) {
                     String name = this.configNameField.getBuiltText().trim();
                     if (!name.isEmpty() && !name.equalsIgnoreCase("autosave")) {
                        boolean deleted = cm.deleteConfig(name);
                        if (deleted) {
                           elegant.getInstance().getNotificationManager().addNotification(
                              moscow.elegant.systems.notifications.NotificationType.SUCCESS, 
                              "Deleted: " + name
                           );
                           this.configNameField.clear();
                        }
                     }
                  }

                  // List (click = load, right hover area = quick delete)
                  float listStartY = btnY + btnH + 14;
                  float itemH = 11;
                  int maxShow = 6;
                  float deleteZoneWidth = 13;
                  List<moscow.elegant.systems.config.ConfigFile> cfgs = cm.getConfigFiles();
                  for (int i = 0; i < Math.min(cfgs.size(), maxShow); i++) {
                     float iy = listStartY + i * itemH;
                     float rowX = configX + 6;
                     float rowW = configW - 12;

                     boolean rowHovered = GuiUtility.isHovered(rowX, iy, rowW, itemH, mouseX, mouseY);
                     boolean deleteHovered = GuiUtility.isHovered(rowX + rowW - deleteZoneWidth, iy, deleteZoneWidth, itemH, mouseX, mouseY);

                     if (rowHovered) {
                        String n = cfgs.get(i).getFileName();

                        if (deleteHovered && !n.equalsIgnoreCase("autosave")) {
                           boolean deleted = cm.deleteConfig(n);
                           if (deleted) {
                              elegant.getInstance().getNotificationManager().addNotification(
                                 moscow.elegant.systems.notifications.NotificationType.SUCCESS, 
                                 "Deleted: " + n
                              );
                           }
                           break;
                        } else {
                           // Load
                           this.configNameField.clear();
                           this.configNameField.paste(n);
                           cfgs.get(i).load();
                           elegant.getInstance().getNotificationManager().addNotification(moscow.elegant.systems.notifications.NotificationType.SUCCESS, "Loaded: " + n);
                           break;
                        }
                     }
                  }
               }
            }
         }

         super.onMouseClicked(mouseX, mouseY, button);
      }
   }

   @Compile
   @Override
   public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
      for (ColorPicker colorPicker : this.colorPickers) {
         colorPicker.onMouseReleased(mouseX, mouseY, button);
      }

      for (MenuPanel panel : this.panels) {
         panel.onMouseReleased(mouseX, mouseY, button);
      }

      if (this.searchField.isFocused()) {
         this.searchField.onMouseReleased(mouseX, mouseY, button);
      }

      this.configNameField.onMouseReleased(mouseX, mouseY, button);

      super.onMouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      for (MenuPanel panel : this.panels) {
         panel.onScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
      }

      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   @Compile
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      for (ColorPicker colorPicker : this.colorPickers) {
         colorPicker.onKeyPressed(keyCode, scanCode, modifiers);
      }

      if (this.searchField != null && !this.searchField.isFocused() && Screen.hasControlDown() && keyCode == 70) {
         this.searchField.setFocused(true);
      }

      for (MenuPanel panel : this.panels) {
         panel.onKeyPressed(keyCode, scanCode, modifiers);
      }

      if (this.searchField.isFocused() && !this.isBindingModule()) {
         this.searchField.onKeyPressed(keyCode, scanCode, modifiers);
      }

      if (this.configNameField.isFocused()) {
         this.configNameField.onKeyPressed(keyCode, scanCode, modifiers);
      }

      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   @Compile
   public boolean charTyped(char chr, int modifiers) {
      if (this.searchField.isFocused() && !this.isBindingModule()) {
         this.searchField.charTyped(chr, modifiers);
      }

      if (this.configNameField.isFocused()) {
         this.configNameField.charTyped(chr, modifiers);
      }

      for (MenuPanel panel : this.panels) {
         panel.charTyped(chr, modifiers);
      }

      return super.charTyped(chr, modifiers);
   }

   @Compile
   public void close() {
      this.closing = true;
      elegant.getInstance().getModuleManager().getModule(MenuModule.class).disable();
      Sounds soundsModule = elegant.getInstance().getModuleManager().getModule(Sounds.class);
      if (soundsModule.isEnabled()) {
         ClientSounds.CLICKGUI_OPEN.play(soundsModule.getVolume().getCurrentValue(), 1.0F);
      }

      elegant.getInstance().getFileManager().writeFile("client");
      if (TextField.LAST_FIELD != null) {
         TextField.LAST_FIELD.setFocused(false);
      }

      super.close();
   }

   public boolean shouldPause() {
      return false;
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
   }

   public boolean shouldCloseOnEsc() {
      return true;
   }

   @Generated
   public Animation getSearchAnimation() {
      return this.searchAnimation;
   }

   @Generated
   public Animation getAppendingAnim() {
      return this.appendingAnim;
   }

   @Generated
   @Override
   public boolean isClosing() {
      return this.closing;
   }

   @Generated
   public List<MenuPanel> getPanels() {
      return this.panels;
   }

   @Generated
   public float getPanelWidth() {
      return this.panelWidth;
   }

   @Generated
   public float getPanelHeight() {
      return this.panelHeight;
   }

   @Generated
   public String getDesc() {
      return this.desc;
   }

   @Generated
   public AnimatedText getDescText() {
      return this.descText;
   }

   @Generated
   public List<ColorPicker> getColorPickers() {
      return this.colorPickers;
   }

   @Generated
   @Override
   public void setClosing(boolean closing) {
      this.closing = closing;
   }

   @Generated
   public void setDesc(String desc) {
      this.desc = desc;
   }

   @Generated
   public TextField getSearchField() {
      return this.searchField;
   }

   public void rememberOpened(moscow.elegant.ui.menu.api.MenuCategory category, moscow.elegant.systems.modules.Module module) {
      if (module != null && category != null) {
         this.openedSettings.put(category, module);
      }
   }

   public void forgetOpened(moscow.elegant.ui.menu.api.MenuCategory category) {
      if (category != null) {
         this.openedSettings.remove(category);
      }
   }

   static {
      new MenuPanel(null);
      new BezierSettingComponent(null, null);
      new BindSettingComponent(null, null);
      new BooleanSettingComponent(null, null);
      new ModeSettingComponent(null, null);
      new ButtonSettingComponent(null, null);
      new ColorSettingComponent(null, null);
      new StringSettingComponent(null, null);
      new RangeSettingComponent(null, null);
      new SliderSettingComponent(null, null);
   }
}
