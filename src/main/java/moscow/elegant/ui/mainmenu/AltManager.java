package moscow.elegant.ui.mainmenu;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import moscow.elegant.elegant;
import moscow.elegant.framework.base.CustomScreen;
import moscow.elegant.framework.base.UIContext;
import moscow.elegant.framework.msdf.Font;
import moscow.elegant.framework.msdf.Fonts;
import moscow.elegant.framework.objects.BorderRadius;
import moscow.elegant.elegant;
import moscow.elegant.framework.objects.MouseButton;
import moscow.elegant.mixin.minecraft.client.IMinecraftClient;
import moscow.elegant.systems.modules.modules.other.Sounds;
import moscow.elegant.systems.file.impl.ClientDataFile;
import moscow.elegant.utility.colors.ColorRGBA;
import moscow.elegant.utility.colors.Colors;
import moscow.elegant.utility.game.TextUtility;
import moscow.elegant.utility.gui.GuiUtility;
import moscow.elegant.utility.interfaces.IMinecraft;
import moscow.elegant.utility.sounds.ClientSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.Session.AccountType;

public class AltManager extends CustomScreen implements IMinecraft {
   private final Screen parent;

   private final List<String> accounts = new ArrayList<>();
   private float scrollOffset = 0;
   private float targetScroll = 0;

   private boolean typing = false;
   private final StringBuilder input = new StringBuilder();

   private int selectedIndex = -1;

   private float addHover = 0, randomHover = 0, clearHover = 0, backHover = 0;
   private float inputHover = 0;

   private float[] useHoverAnims;
   private float[] delHoverAnims;

   private final List<Float> entryAnims = new ArrayList<>();

   private ClientDataFile dataFile;
   private Sounds soundsModule; // cached for perf (avoid getInstance every frame)

   public AltManager(Screen parent) {
      this.parent = parent;
      this.dataFile = (ClientDataFile) elegant.getInstance().getFileManager().getClientFile("client");
      this.soundsModule = elegant.getInstance().getModuleManager().getModule(Sounds.class);
      if (this.dataFile != null) {
         this.accounts.addAll(this.dataFile.getAlts());
         String sel = this.dataFile.getSelectedAlt();
         if (sel != null && this.accounts.contains(sel)) {
            this.selectedIndex = this.accounts.indexOf(sel);
         }
      }
      if (this.accounts.isEmpty()) {
         String current = mc.getSession() != null ? mc.getSession().getUsername() : "Player";
         this.accounts.add(current);
         if (this.dataFile != null) this.dataFile.addAlt(current);
      }

      // init entry animations
      this.entryAnims.clear();
      for (int i = 0; i < this.accounts.size(); i++) {
         this.entryAnims.add(1f);
      }
   }

   private void syncAlts() {
      if (this.dataFile != null) {
         // ensure saved
      }
   }

   private void loginTo(String name) {
      if (name == null || name.isEmpty()) return;
      try {
         // Stable offline UUID (common pattern for alt managers)
         UUID offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));

         // Use LEGACY for pure offline alts - more stable than MOJANG with empty token
         Session newSession = new Session(
            name,
            offlineUuid,
            "",
            Optional.empty(),
            Optional.empty(),
            AccountType.LEGACY
         );

         // Try the accessor first (with access widener + @Mutable)
         try {
            IMinecraftClient acc = (IMinecraftClient) mc;
            acc.setSession(newSession);
         } catch (Throwable t) {
            // Fallback to direct Unsafe reflection for final field
            forceSetSession(newSession);
         }

         if (this.dataFile != null) {
            this.dataFile.setSelectedAlt(name);
         }
         if (!this.accounts.contains(name)) {
            this.accounts.add(name);
            this.entryAnims.add(0f);
            if (this.dataFile != null) this.dataFile.addAlt(name);
         }
         this.selectedIndex = this.accounts.indexOf(name);
      } catch (Exception e) {
         e.printStackTrace();
         // At minimum keep the list consistent
         if (!this.accounts.contains(name)) {
            this.accounts.add(name);
            this.entryAnims.add(0f);
            if (this.dataFile != null) this.dataFile.addAlt(name);
         }
         this.selectedIndex = this.accounts.indexOf(name);
      }
   }

   /**
    * Force sets the session field using Unsafe because the field is final.
    * This is a last-resort fallback.
    */
   private static void forceSetSession(Session session) {
      try {
         MinecraftClient mcInstance = MinecraftClient.getInstance();

         // Try to find the field (works in both yarn dev and obfuscated)
         java.lang.reflect.Field sessionField = null;
         try {
            sessionField = MinecraftClient.class.getDeclaredField("session");
         } catch (NoSuchFieldException ignored) {
            sessionField = MinecraftClient.class.getDeclaredField("field_1726");
         }

         sessionField.setAccessible(true);

         // Remove final modifier using Unsafe
         sun.misc.Unsafe unsafe = getUnsafe();
         long offset = unsafe.objectFieldOffset(sessionField);
         unsafe.putObject(mcInstance, offset, session);
      } catch (Throwable e) {
         e.printStackTrace();
      }
   }

   private static sun.misc.Unsafe getUnsafe() throws Exception {
      java.lang.reflect.Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
      theUnsafe.setAccessible(true);
      return (sun.misc.Unsafe) theUnsafe.get(null);
   }

   // Cached colors to reduce allocations in render (optimization)
   private static final ColorRGBA BG_DARK = new ColorRGBA(8, 8, 12, 235);
   private static final ColorRGBA PANEL_BG = new ColorRGBA(18, 18, 24, 250);
   private static final ColorRGBA BORDER_HIGHLIGHT = new ColorRGBA(60, 60, 70, 90);
   private static final ColorRGBA LIST_BG = new ColorRGBA(22, 22, 28, 200);
   private static final ColorRGBA INPUT_BG = new ColorRGBA(28, 28, 35, 255);
   private static final ColorRGBA CURRENT_ACCENT = new ColorRGBA(180, 185, 200);
   private static final ColorRGBA PLACEHOLDER = new ColorRGBA(140, 145, 155);
   private static final ColorRGBA BASE_ROW = new ColorRGBA(30, 30, 37, 180);
   private static final ColorRGBA SELECTED_ROW = new ColorRGBA(45, 48, 60, 235);
   private static final ColorRGBA ROW_HOVER_MIX = new ColorRGBA(48, 50, 62);
   private static final ColorRGBA USE_BASE = new ColorRGBA(55, 120, 80, 220);
   private static final ColorRGBA USE_HOVER = new ColorRGBA(70, 150, 100);
   private static final ColorRGBA DEL_BASE = new ColorRGBA(110, 45, 45, 215);
   private static final ColorRGBA DEL_HOVER = new ColorRGBA(145, 55, 55);
   private static final ColorRGBA BTN_BASE = new ColorRGBA(42, 42, 52, 235);
   private static final ColorRGBA BTN_HOVER_MIX = new ColorRGBA(62, 62, 75);

   private int lastListSize = 0;

   @Override
   public void render(UIContext context) {
      if (!Fonts.isInitialized()) {
         context.drawRect(0, 0, this.width, this.height, BG_DARK);
         return;
      }
      // Dark elegant background matching main menu
      context.drawRect(0, 0, this.width, this.height, BG_DARK);

      Font titleF = Fonts.ROUND_BOLD.getFont(18.0F);
      Font nameF = Fonts.MEDIUM.getFont(9.5F);
      Font smallF = Fonts.MEDIUM.getFont(7.5F);
      Font btnF = Fonts.MEDIUM.getFont(8.0F);

      float panelW = Math.min(340.0F, this.width * 0.82F);
      float panelH = Math.min(380.0F, this.height * 0.86F);
      float px = (this.width - panelW) / 2.0F;
      float py = (this.height - panelH) / 2.0F - 10.0F;

      // Shadow + main panel
      context.drawShadow(px - 8, py - 8, panelW + 16, panelH + 16, 22.0F, BorderRadius.all(14.0F), ColorRGBA.BLACK.withAlpha(110));
      context.drawSquircle(px, py, panelW, panelH, 9.0F, BorderRadius.all(12.0F), PANEL_BG);

      // Subtle border highlight
      context.drawRoundedBorder(px + 0.5F, py + 0.5F, panelW - 1, panelH - 1, 1.2F, BorderRadius.all(11.5F), BORDER_HIGHLIGHT);

      // Title
      float titleY = py + 16;
      context.drawCenteredText(titleF, "Alt Manager", this.width / 2.0F, titleY, ColorRGBA.WHITE);

      // Current account
      String currentName = mc.getSession() != null ? mc.getSession().getUsername() : "Player";
      float curY = titleY + 22;
      context.drawCenteredText(smallF, "Current: " + currentName, this.width / 2.0F, curY, CURRENT_ACCENT);

      // INPUT ROW
      float inputY = curY + 22;
      float inputX = px + 16;
      float inputW = panelW - 32 - 68;
      float inputH = 22;

      boolean ih = GuiUtility.isHovered(inputX, inputY, inputW, inputH, context);
      this.inputHover = Math.max(0, Math.min(1, this.inputHover + (ih ? 0.12f : -0.1f)));

      context.drawRoundedRect(inputX, inputY, inputW, inputH, BorderRadius.all(7.0F), INPUT_BG);

      if (this.inputHover > 0.01f) {
         context.drawRoundedBorder(inputX, inputY, inputW, inputH, 1.0F, BorderRadius.all(7.0F), new ColorRGBA(90, 95, 110).withAlpha(120 * this.inputHover));
      }

      String place = typing ? "" : "Enter username...";
      String display = typing ? (input.toString() + ((System.currentTimeMillis() / 420) % 2 == 0 ? "_" : "")) : place;
      ColorRGBA txtCol = typing ? ColorRGBA.WHITE : PLACEHOLDER;
      context.drawText(smallF, display, inputX + 9, inputY + 6.5F, txtCol);

      // Add button
      float addX = inputX + inputW + 8;
      float addW = 58;
      boolean addHov = GuiUtility.isHovered(addX, inputY, addW, inputH, context);
      this.addHover = Math.max(0, Math.min(1, this.addHover + (addHov ? 0.15f : -0.12f)));
      ColorRGBA addCol = BTN_BASE.mix(BTN_HOVER_MIX, this.addHover);
      context.drawRoundedRect(addX, inputY, addW, inputH, BorderRadius.all(7.0F), addCol);
      context.drawCenteredText(btnF, "Add", addX + addW / 2, inputY + 7, ColorRGBA.WHITE);

      // LIST
      float listY = inputY + inputH + 12;
      float listH = panelH - (listY - py) - 78;
      float listX = px + 16;
      float listW = panelW - 32;

      // list bg
      context.drawRoundedRect(listX, listY, listW, listH, BorderRadius.all(8.0F), LIST_BG);

      float itemH = 28.0F;
      float startY = listY + 6 - scrollOffset;

      List<String> toRender = this.accounts; // direct reference - no allocation per frame (perf)

      int currentSize = toRender.size();

      // Only sync anim arrays when size actually changed (optimization)
      if (currentSize != lastListSize) {
         while (entryAnims.size() < currentSize) entryAnims.add(0f);
         while (entryAnims.size() > currentSize) entryAnims.remove(entryAnims.size() - 1);

         if (useHoverAnims == null || useHoverAnims.length != currentSize) {
            useHoverAnims = new float[currentSize];
            delHoverAnims = new float[currentSize];
         }
         lastListSize = currentSize;
      }

      // Update entry animations (pop in for >=0 , shrink for <0 )
      for (int j = 0; j < entryAnims.size(); j++) {
         float val = entryAnims.get(j);
         if (val >= 0) {
            // adding or normal: grow to 1
            val = val * 0.75f + 1.0f * 0.25f;
         } else {
            // removing: keep shrinking
            val *= 0.6f;
         }
         entryAnims.set(j, val);
      }

      // cleanup fully shrunk removing entries (for delete effect)
      for (int j = entryAnims.size() - 1; j >= 0; j--) {
         if (entryAnims.get(j) < 0 && Math.abs(entryAnims.get(j)) < 0.05f) {
            if (j < accounts.size()) accounts.remove(j);
            entryAnims.remove(j);
            if (selectedIndex == j) selectedIndex = -1;
            else if (selectedIndex > j) selectedIndex--;
            lastListSize = -1; // force resync next frame
         }
      }

      if (useHoverAnims.length != accounts.size()) {
         useHoverAnims = new float[accounts.size()];
         delHoverAnims = new float[accounts.size()];
         lastListSize = -1;
      }

      // Draw items
      for (int i = 0; i < accounts.size(); i++) {
         float iy = startY + i * itemH;
         if (iy + itemH < listY || iy > listY + listH) continue;

         boolean isSel = (i == selectedIndex);
         float rowX = listX + 4;
         float rowW = listW - 8;

         double mx = context.getMouseX();
         double my = context.getMouseY();
         boolean rowHov = mx >= rowX && mx <= rowX + rowW && my >= iy && my < iy + itemH - 3;

         float rawAnim = (i < entryAnims.size()) ? entryAnims.get(i) : 1f;
         float itemAnim = Math.abs(rawAnim);
         float rowScale = Math.max(0.15f, itemAnim);
         float drawH = (itemH - 3) * rowScale;
         float drawY = iy + ((itemH - 3) - drawH) * 0.5f;

         ColorRGBA baseRow = isSel ? SELECTED_ROW : BASE_ROW;
         ColorRGBA rowBg = rowHov && !isSel ? baseRow.mix(ROW_HOVER_MIX, 0.45f) : baseRow;
         rowBg = rowBg.withAlpha((int)(rowBg.getAlpha() * Math.max(0.2f, itemAnim)));
         context.drawRoundedRect(rowX, drawY, rowW, drawH, BorderRadius.all(6.0F), rowBg);

         // name with slight alpha
         String accName = accounts.get(i);
         ColorRGBA nameCol = ColorRGBA.WHITE.withAlpha((int)(255 * Math.max(0.3f, itemAnim)));
         context.drawText(nameF, accName, rowX + 10, drawY + drawH * 0.3f, nameCol);

         // Use btn with animation + entry effect
         float useW = 36;
         float useX = rowX + rowW - useW - 42;
         boolean useH = GuiUtility.isHovered(useX, iy + 3, useW, 18, context);
         useHoverAnims[i] = useHoverAnims[i] * 0.75f + (useH ? 0.25f : 0f);
         float useScale = (1.0f + useHoverAnims[i] * 0.12f) * rowScale;
         float useDrawW = useW * useScale;
         float useDrawX = useX + (useW - useDrawW) / 2f;
         float useDrawY = drawY + (drawH - 17 * useScale) / 2f;
         ColorRGBA useC = USE_BASE.mix(USE_HOVER, useHoverAnims[i]);
         useC = useC.withAlpha((int)(useC.getAlpha() * Math.max(0.2f, itemAnim)));
         context.drawRoundedRect(useDrawX, useDrawY, useDrawW, 17 * useScale, BorderRadius.all(4.5F), useC);
         context.drawCenteredText(smallF, "Use", useDrawX + useDrawW / 2f, useDrawY + (17 * useScale) / 2f - 4.5f, ColorRGBA.WHITE.withAlpha((int)(255 * Math.max(0.3f, itemAnim))));

         // Delete btn with animation + entry effect
         float delW = 28;
         float delX = rowX + rowW - delW - 8;
         boolean delH = GuiUtility.isHovered(delX, iy + 3, delW, 18, context);
         delHoverAnims[i] = delHoverAnims[i] * 0.75f + (delH ? 0.25f : 0f);
         float delScale = (1.0f + delHoverAnims[i] * 0.12f) * rowScale;
         float delDrawW = delW * delScale;
         float delDrawX = delX + (delW - delDrawW) / 2f;
         float delDrawY = drawY + (drawH - 17 * delScale) / 2f;
         ColorRGBA delC = DEL_BASE.mix(DEL_HOVER, delHoverAnims[i]);
         delC = delC.withAlpha((int)(delC.getAlpha() * Math.max(0.2f, itemAnim)));
         context.drawRoundedRect(delDrawX, delDrawY, delDrawW, 17 * delScale, BorderRadius.all(4.5F), delC);
         context.drawCenteredText(smallF, "×", delDrawX + delDrawW / 2f, delDrawY + (17 * delScale) / 2f - 4.5f, ColorRGBA.WHITE.withAlpha((int)(255 * Math.max(0.3f, itemAnim))));
      }

      // scroll limit
      float maxScroll = Math.max(0, toRender.size() * itemH - listH + 8);
      targetScroll = Math.max(0, Math.min(targetScroll, maxScroll));
      scrollOffset = scrollOffset + (targetScroll - scrollOffset) * 0.18f;

      // BOTTOM BUTTONS
      float btnY = py + panelH - 52;
      float btnH = 26;
      float gap = 10;
      float btnW = (panelW - 32 - gap * 2) / 3f;

      float bx1 = px + 16;
      float bx2 = bx1 + btnW + gap;
      float bx3 = bx2 + btnW + gap;

      // Add from input (big) wait reuse or separate small buttons row: Random | Clear | Back

      boolean randH = GuiUtility.isHovered(bx1, btnY, btnW, btnH, context);
      this.randomHover = Math.max(0, Math.min(1, this.randomHover + (randH ? 0.14f : -0.11f)));
      ColorRGBA rbg = BTN_BASE.mix(BTN_HOVER_MIX, this.randomHover);
      context.drawRoundedRect(bx1, btnY, btnW, btnH, BorderRadius.all(7.0F), rbg);
      context.drawCenteredText(btnF, "Random", bx1 + btnW / 2, btnY + 8, ColorRGBA.WHITE);

      boolean clrH = GuiUtility.isHovered(bx2, btnY, btnW, btnH, context);
      this.clearHover = Math.max(0, Math.min(1, this.clearHover + (clrH ? 0.14f : -0.11f)));
      ColorRGBA cbg = DEL_BASE.mix(DEL_HOVER, this.clearHover);
      context.drawRoundedRect(bx2, btnY, btnW, btnH, BorderRadius.all(7.0F), cbg);
      context.drawCenteredText(btnF, "Clear All", bx2 + btnW / 2, btnY + 8, ColorRGBA.WHITE);

      boolean bckH = GuiUtility.isHovered(bx3, btnY, btnW, btnH, context);
      this.backHover = Math.max(0, Math.min(1, this.backHover + (bckH ? 0.14f : -0.11f)));
      ColorRGBA bbg = BTN_BASE.mix(BTN_HOVER_MIX, this.backHover);
      context.drawRoundedRect(bx3, btnY, btnW, btnH, BorderRadius.all(7.0F), bbg);
      context.drawCenteredText(btnF, "Back", bx3 + btnW / 2, btnY + 8, ColorRGBA.WHITE);

      // Footer hint
      context.drawCenteredText(smallF, "Random → Sliz + letters/digits (max 12) • Offline alts only", this.width / 2.0F, py + panelH - 22, new ColorRGBA(105, 108, 120));
   }

   @Override
   public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
      if (button != MouseButton.LEFT) {
         super.onMouseClicked(mouseX, mouseY, button);
         return;
      }

      Font smallF = Fonts.MEDIUM.getFont(7.5F);
      float panelW = Math.min(340.0F, this.width * 0.82F);
      float panelH = Math.min(380.0F, this.height * 0.86F);
      float px = (this.width - panelW) / 2.0F;
      float py = (this.height - panelH) / 2.0F - 10.0F;

      float titleY = py + 16;
      float curY = titleY + 22;
      float inputY = curY + 22;
      float inputX = px + 16;
      float inputW = panelW - 32 - 68;
      float inputH = 22;

      float addX = inputX + inputW + 8;
      float addW = 58;

      // Click input
      if (GuiUtility.isHovered(inputX, inputY, inputW, inputH, mouseX, mouseY)) {
         typing = true;
         return;
      } else {
         if (typing && !GuiUtility.isHovered(inputX, inputY, inputW + addW + 16, inputH, mouseX, mouseY)) {
            typing = false;
         }
      }

      // Add button
      if (GuiUtility.isHovered(addX, inputY, addW, inputH, mouseX, mouseY)) {
         String n = input.toString().trim();
         if (!n.isEmpty() && n.length() <= 16) {
            if (!accounts.contains(n)) {
               accounts.add(n);
               entryAnims.add(0f);
               if (dataFile != null) dataFile.addAlt(n);
            }
            loginTo(n);
            input.setLength(0);
            typing = false;
            playActionSound();
         }
         return;
      }

      // List area
      float listY = inputY + inputH + 12;
      float listH = panelH - (listY - py) - 78;
      float listX = px + 16;
      float listW = panelW - 32;
      float itemH = 28.0F;

      if (mouseY >= listY && mouseY <= listY + listH) {
         float startY = listY + 6 - scrollOffset;
         for (int i = 0; i < accounts.size(); i++) {
            float iy = startY + i * itemH;
            if (iy + itemH < listY || iy > listY + listH) continue;

            float rowX = listX + 4;
            float rowW = listW - 8;

            // Delete button (highest priority)
            float delW = 28;
            float delX = rowX + rowW - delW - 8;
            if (mouseX >= delX && mouseX <= delX + delW && mouseY >= iy + 3 && mouseY <= iy + 3 + 18) {
               String toDel = accounts.get(i);
               if (dataFile != null) dataFile.removeAlt(toDel);
               if (i < entryAnims.size()) entryAnims.set(i, -1f); // trigger shrink animation (negative = removing)
               // actual removal + index fix happens in render cleanup for visual effect
               float maxS = Math.max(0, accounts.size() * itemH - listH + 8);
               targetScroll = Math.min(targetScroll, maxS);
               playActionSound();
               return;
            }

            // Use button
            float useW = 36;
            float useX = rowX + rowW - useW - 42;
            if (mouseX >= useX && mouseX <= useX + useW && mouseY >= iy + 3 && mouseY <= iy + 3 + 18) {
               loginTo(accounts.get(i));
               playActionSound();
               return;
            }

            // Full row select (only if click not on buttons)
            if (mouseX >= rowX && mouseX <= rowX + rowW && mouseY >= iy && mouseY <= iy + itemH - 3) {
               String sel = accounts.get(i);
               loginTo(sel);
               playActionSound();
               return;
            }
         }
      }

      // Bottom buttons
      float btnY = py + panelH - 52;
      float btnH = 26;
      float gap = 10;
      float btnW = (panelW - 32 - gap * 2) / 3f;

      float bx1 = px + 16; // random
      float bx2 = bx1 + btnW + gap; // clear
      float bx3 = bx2 + btnW + gap; // back

      if (GuiUtility.isHovered(bx1, btnY, btnW, btnH, mouseX, mouseY)) {
         // Random
         String rand = generateRandomName();
         if (!accounts.contains(rand)) {
            accounts.add(rand);
            entryAnims.add(0f);
            if (dataFile != null) dataFile.addAlt(rand);
         }
         loginTo(rand);
         playActionSound();
         return;
      }

      if (GuiUtility.isHovered(bx2, btnY, btnW, btnH, mouseX, mouseY)) {
         // Clear all
         if (dataFile != null) dataFile.clearAlts();
         accounts.clear();
         entryAnims.clear();
         String cur = mc.getSession() != null ? mc.getSession().getUsername() : "Player";
         accounts.add(cur);
         entryAnims.add(1f);
         if (dataFile != null) dataFile.addAlt(cur);
         selectedIndex = 0;
         loginTo(cur);
         playActionSound();
         return;
      }

      if (GuiUtility.isHovered(bx3, btnY, btnW, btnH, mouseX, mouseY)) {
         close();
         return;
      }

      super.onMouseClicked(mouseX, mouseY, button);
   }

   private String generateRandomName() {
      Random r = new Random();
      String prefix = "Sliz";
      int maxTotalLength = 12;
      int suffixLength = r.nextInt(8) + 1; // 1 to 8 characters → total length 5-12

      String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
      StringBuilder sb = new StringBuilder(prefix);
      for (int i = 0; i < suffixLength; i++) {
         sb.append(chars.charAt(r.nextInt(chars.length())));
      }
      return sb.toString();
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      float panelW = Math.min(340.0F, this.width * 0.82F);
      float panelH = Math.min(380.0F, this.height * 0.86F);
      float px = (this.width - panelW) / 2.0F;
      float py = (this.height - panelH) / 2.0F - 10.0F;

      float inputY = py + 60;
      float listY = inputY + 22 + 12;
      float listH = panelH - (listY - py) - 78;

      if (mouseY > listY && mouseY < listY + listH) {
         targetScroll -= (float) (verticalAmount * 26);
         float max = Math.max(0, accounts.size() * 28.0F - listH + 8);
         targetScroll = Math.max(0, Math.min(targetScroll, max));
         return true;
      }
      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (typing) {
         if (keyCode == 256) { // ESC
            typing = false;
            input.setLength(0);
            return true;
         }
         if (keyCode == 257 || keyCode == 335) { // ENTER
            String n = input.toString().trim();
            if (!n.isEmpty() && n.length() <= 16) {
               if (!accounts.contains(n)) {
                  accounts.add(n);
                  entryAnims.add(0f);
                  if (dataFile != null) dataFile.addAlt(n);
               }
               loginTo(n);
               input.setLength(0);
               playActionSound();
            }
            typing = false;
            return true;
         }
         if (keyCode == 259 && input.length() > 0) { // BACKSPACE
            input.deleteCharAt(input.length() - 1);
            try {
               ClientSounds.TYPING.play(0.12f, 0.65f);
            } catch (Exception ignored) {}
            return true;
         }
         if (keyCode == 86 && (modifiers & 2) != 0) { // CTRL+V
            try {
               String clip = mc.keyboard.getClipboard();
               if (clip != null) {
                  clip = clip.replaceAll("[^a-zA-Z0-9_]", "");
                  for (char c : clip.toCharArray()) {
                     if (input.length() < 16) input.append(c);
                  }
               }
            } catch (Exception ignored) {}
            return true;
         }
      }
      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      if (typing && input.length() < 16) {
         if (Character.isLetterOrDigit(chr) || chr == '_') {
            input.append(chr);
            // Optimized sound - avoid try/catch hot path
            if (soundsModule != null && soundsModule.isEnabled()) {
               ClientSounds.TYPING.play(0.12f, 0.95f + ((chr & 3) * 0.04f));
            }
            return true;
         }
      }
      return super.charTyped(chr, modifiers);
   }

   @Override
   public void close() {
      super.close();
      if (parent != null) {
         mc.setScreen(parent);
      }
   }

   @Override
   public boolean shouldPause() {
      return false;
   }

   private void playActionSound() {
      if (soundsModule != null && soundsModule.isEnabled()) {
         ClientSounds.MODULE.play(0.45f, 0.95f + (float)(Math.random() * 0.08f));
      }
   }
}