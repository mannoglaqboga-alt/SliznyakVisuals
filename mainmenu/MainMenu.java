package ru.levin.screens.mainmenu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import ru.levin.screens.altmanager.AltManager;
import ru.levin.util.color.ColorUtil;
import ru.levin.manager.fontManager.FontUtils;
import ru.levin.util.render.RenderUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("All")
public class MainMenu extends Screen {

    private Button singleplayerButton;
    private Button multiplayerButton;
    private Button altmanagerButton;
    private CombinedButton optionsQuitButton;

    private final String title = "VortexCore";
    private final String version = "v1.0 BETA";

    private int shakeTime = 0;
    private float shakeOffsetY = 0f;

    // Анимация появления
    private long initTime = 0;
    private float fadeIn = 0f;

    // Частицы на фоне
    private List<Particle> particles = new ArrayList<>();
    private Random random = new Random();

    public MainMenu() {
        super(Text.literal("Custom Main Menu"));
    }

    @Override
    protected void init() {
        initTime = System.currentTimeMillis();
        fadeIn = 0f;

        // Инициализируем частицы
        particles.clear();
        for (int i = 0; i < 50; i++) {
            particles.add(new Particle());
        }

        int buttonWidth = 200;
        int buttonHeight = 30;

        singleplayerButton = new Button("Singleplayer", 0, 0, buttonWidth, buttonHeight);
        multiplayerButton = new Button("Multiplayer", 0, 0, buttonWidth, buttonHeight);
        altmanagerButton = new Button("AltManager", 0, 0, buttonWidth, buttonHeight);
        optionsQuitButton = new CombinedButton(0, 0, buttonWidth, buttonHeight, "Options", "Quit");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Анимация появления
        if (fadeIn < 1f) {
            fadeIn = Math.min(1f, fadeIn + 0.02f);
        }

        // Анимированный градиентный фон
        long currentTime = System.currentTimeMillis();
        float animTime = (currentTime % 10000L) / 10000f;

        float sin1 = (float) Math.sin(animTime * Math.PI * 2);
        float sin2 = (float) Math.sin(animTime * Math.PI * 2 + Math.PI / 2);
        float cos1 = (float) Math.cos(animTime * Math.PI * 2);
        float cos2 = (float) Math.cos(animTime * Math.PI * 2 + Math.PI / 2);

        int baseR1 = Math.max(0, Math.min(255, 40 + (int)(sin1 * 30)));
        int baseG1 = Math.max(0, Math.min(255, 20 + (int)(cos1 * 20)));
        int baseB1 = Math.max(0, Math.min(255, 30 + (int)(sin2 * 25)));

        int baseR2 = Math.max(0, Math.min(255, 50 + (int)(cos2 * 35)));
        int baseG2 = Math.max(0, Math.min(255, 25 + (int)(sin1 * 25)));
        int baseB2 = Math.max(0, Math.min(255, 45 + (int)(cos1 * 30)));

        int baseR3 = Math.max(0, Math.min(255, 35 + (int)(cos1 * 30)));
        int baseG3 = Math.max(0, Math.min(255, 15 + (int)(sin2 * 20)));
        int baseB3 = Math.max(0, Math.min(255, 40 + (int)(sin1 * 30)));

        int baseR4 = Math.max(0, Math.min(255, 45 + (int)(sin2 * 35)));
        int baseG4 = Math.max(0, Math.min(255, 20 + (int)(cos2 * 25)));
        int baseB4 = Math.max(0, Math.min(255, 35 + (int)(cos1 * 25)));

        int color1 = new Color(baseR1, baseG1, baseB1, 255).getRGB();
        int color2 = new Color(baseR2, baseG2, baseB2, 255).getRGB();
        int color3 = new Color(baseR3, baseG3, baseB3, 255).getRGB();
        int color4 = new Color(baseR4, baseG4, baseB4, 255).getRGB();

        RenderUtil.rectRGB(context.getMatrices(), -1, -1, this.width + 2, this.height + 2, 0, color1, color2, color3, color4);
        RenderUtil.drawRoundedRect(context.getMatrices(), -1, -1, this.width + 2, this.height + 2, 0, new Color(0, 0, 0, 180).getRGB());

        // Рисуем и обновляем частицы
        for (Particle particle : particles) {
            particle.update();
            particle.render(context);
        }

        // Анимация тряски заголовка
        if (shakeTime > 0) {
            shakeTime--;
            shakeOffsetY = (float)(Math.sin(shakeTime * 0.5) * 3);
        } else {
            shakeOffsetY = 0f;
        }

        int titleWidth = (int) FontUtils.sf_bold[54].getWidth(title);
        float titleX = (this.width - titleWidth) / 2f;
        float titleBaseY = this.height / 5f;
        float titleY = titleBaseY + shakeOffsetY;

        // Тень для заголовка
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 0);
        FontUtils.sf_bold[54].renderAnimatedGradientText(context.getMatrices(), title, titleX + 2, titleY + 2,
                new Color(0, 0, 0, 100).getRGB(), new Color(0, 0, 0, 100).getRGB(), 0);
        context.getMatrices().pop();

        // Основной заголовок с градиентом
        float time = (System.currentTimeMillis() % 4000L) / 1500f;
        int lightRed1 = new Color(255, 180, 180, 255).getRGB();
        int lightRed2 = new Color(255, 140, 140, 255).getRGB();
        FontUtils.sf_bold[54].renderAnimatedGradientText(context.getMatrices(), title, titleX, titleY, lightRed1, lightRed2, time);

        float titleHeight = 54;
        int spacing = 12;
        int buttonWidth = 200;
        int buttonHeight = 30;

        // Реальные часы
        java.time.LocalTime currentLocalTime = java.time.LocalTime.now();
        String timeString = String.format("%02d:%02d:%02d",
                currentLocalTime.getHour(),
                currentLocalTime.getMinute(),
                currentLocalTime.getSecond());

        float clockY = titleY + 40; // На 80 пикселей ниже заголовка
        int clockWidth = (int) FontUtils.sf_medium[24].getWidth(timeString);
        float clockX = (this.width - clockWidth) / 2f;

// Тень для часов
        FontUtils.sf_medium[24].centeredDraw(context.getMatrices(), timeString,
                this.width / 2f + 1, clockY + 1, new Color(0, 0, 0, 100).getRGB());
// Основные часы
        FontUtils.sf_medium[24].centeredDraw(context.getMatrices(), timeString,
                this.width / 2f, clockY, new Color(255, 200, 200, 255).getRGB());

        float buttonsStartY = titleBaseY + titleHeight + spacing * 2;
        int centerX = this.width / 2 - buttonWidth / 2;

        singleplayerButton.x = centerX;
        singleplayerButton.y = (int)buttonsStartY;

        multiplayerButton.x = centerX;
        multiplayerButton.y = (int)(buttonsStartY + buttonHeight + spacing);

        altmanagerButton.x = centerX;
        altmanagerButton.y = (int)(buttonsStartY + 2 * (buttonHeight + spacing));

        optionsQuitButton.x = centerX;
        optionsQuitButton.y = (int)(buttonsStartY + 3 * (buttonHeight + spacing));
        optionsQuitButton.width = buttonWidth;

        // Применяем fadeIn анимацию
        context.getMatrices().push();
        float alpha = fadeIn;
        context.getMatrices().translate(0, (1 - fadeIn) * 20, 0);

        singleplayerButton.render(context, mouseX, mouseY, delta, alpha);
        multiplayerButton.render(context, mouseX, mouseY, delta, alpha);
        altmanagerButton.render(context, mouseX, mouseY, delta, alpha);
        optionsQuitButton.render(context, mouseX, mouseY, delta, alpha);

        context.getMatrices().pop();

        // Версия клиента внизу справа
        String versionText = version;
        float versionWidth = FontUtils.sf_medium[16].getWidth(versionText);
        float versionX = this.width - versionWidth - 10;
        float versionY = this.height - 20;

        // Тень для версии
        FontUtils.sf_medium[16].centeredDraw(context.getMatrices(), versionText, versionX + 1, versionY + 1, new Color(0, 0, 0, 150).getRGB());
        FontUtils.sf_medium[16].centeredDraw(context.getMatrices(), versionText, versionX, versionY, new Color(180, 180, 180, 200).getRGB());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int titleWidth = (int) FontUtils.sf_bold[54].getWidth(title);
        float titleX = (this.width - titleWidth) / 2f;
        float titleY = this.height / 5f;

        if (mouseX >= titleX && mouseX <= titleX + titleWidth && mouseY >= titleY && mouseY <= titleY + 54) {
            shakeTime = 20;
            return true;
        }

        if (singleplayerButton.isHovered(mouseX, mouseY)) {
            this.client.setScreen(new SelectWorldScreen(this));
            return true;
        }
        if (multiplayerButton.isHovered(mouseX, mouseY)) {
            this.client.setScreen(new MultiplayerScreen(this));
            return true;
        }
        if (altmanagerButton.isHovered(mouseX, mouseY)) {
            this.client.setScreen(new AltManager(this));
            return true;
        }
        if (optionsQuitButton.isOptionHovered(mouseX, mouseY)) {
            this.client.setScreen(new OptionsScreen(this, client.options));
            return true;
        }
        if (optionsQuitButton.isQuitHovered(mouseX, mouseY)) {
            this.client.scheduleStop();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    // Класс частицы для фона
    private class Particle {
        float x, y;
        float vx, vy;
        float size;
        int alpha;

        Particle() {
            reset();
            y = random.nextFloat() * height;
        }

        void reset() {
            x = random.nextFloat() * width;
            y = -10;
            vx = (random.nextFloat() - 0.5f) * 0.5f;
            vy = random.nextFloat() * 0.5f + 0.3f;
            size = random.nextFloat() * 2 + 1;
            alpha = random.nextInt(100) + 50;
        }

        void update() {
            x += vx;
            y += vy;

            if (y > height + 10 || x < -10 || x > width + 10) {
                reset();
            }
        }

        void render(DrawContext context) {
            int color = new Color(255, 180, 180, alpha).getRGB();
            RenderUtil.drawRoundedRect(context.getMatrices(), (int)x, (int)y, (int)size, (int)size, size / 2, color);
        }
    }

    private class Button {
        final String name;
        int x, y, width, height;

        private float hoverAnim = 0f;
        private float scale = 1f;
        private float glowAnim = 0f;

        Button(String name, int x, int y, int width, int height) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        void render(DrawContext context, int mouseX, int mouseY, float delta, float fadeAlpha) {
            boolean hovered = isHovered(mouseX, mouseY);

            float animSpeed = 0.04f;

            if (hovered) {
                hoverAnim = Math.min(1f, hoverAnim + animSpeed);
                scale = Math.min(1.03f, scale + animSpeed * 0.75f);
                glowAnim = Math.min(1f, glowAnim + animSpeed * 2);
            } else {
                hoverAnim = Math.max(0f, hoverAnim - animSpeed);
                scale = Math.max(1f, scale - animSpeed * 0.75f);
                glowAnim = Math.max(0f, glowAnim - animSpeed * 2);
            }

            context.getMatrices().push();
            context.getMatrices().translate(x + width / 2f, y + height / 2f, 0);
            context.getMatrices().scale(scale, scale, 1);
            context.getMatrices().translate(-(x + width / 2f), -(y + height / 2f), 0);

            // Светящийся эффект при наведении
            if (glowAnim > 0) {
                int glowAlpha = (int)(glowAnim * 80 * fadeAlpha);
                RenderUtil.drawRoundedRect(context.getMatrices(),
                        x - 3, y - 3,
                        width + 6, height + 6,
                        10, new Color(255, 140, 140, glowAlpha).getRGB());
            }

            // Тень кнопки
            RenderUtil.drawRoundedRect(context.getMatrices(),
                    x + 2, y + 2,
                    width, height, 7,
                    new Color(0, 0, 0, (int)(100 * fadeAlpha)).getRGB());

            // Градиентный фон кнопки
            int baseColor = new Color(25, 25, 25, (int)(120 * fadeAlpha)).getRGB();
            int hoverColor = new Color(50, 30, 30, (int)(180 * fadeAlpha)).getRGB();
            int bgColor = ColorUtil.blendColorsInt(baseColor, hoverColor, hoverAnim);

            RenderUtil.drawRoundedRect(context.getMatrices(), x, y, width, height, 7, bgColor);

            // Тонкая обводка при наведении
            if (hoverAnim > 0) {
                int borderAlpha = (int)(hoverAnim * 150 * fadeAlpha);
                RenderUtil.drawRoundedRect(context.getMatrices(),
                        x, y, width, height, 7,
                        new Color(255, 140, 140, borderAlpha).getRGB());
            }

            float textHeight = 20;
            float textY = y + (height - textHeight) / 2f + 3;

            int textColor = new Color(255, 255, 255, (int)(255 * fadeAlpha)).getRGB();
            FontUtils.sf_medium[20].centeredDraw(context.getMatrices(), name, x + width / 2f, textY, textColor);

            context.getMatrices().pop();
        }

        boolean isHovered(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    private class CombinedButton {
        int x, y, width, height;
        final String leftName, rightName;

        private float leftHoverAnim = 0f;
        private float rightHoverAnim = 0f;
        private float leftScale = 1f;
        private float rightScale = 1f;
        private float leftGlowAnim = 0f;
        private float rightGlowAnim = 0f;

        CombinedButton(int x, int y, int width, int height, String leftName, String rightName) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.leftName = leftName;
            this.rightName = rightName;
        }

        void render(DrawContext context, int mouseX, int mouseY, float delta, float fadeAlpha) {
            int buttonGap = 2;
            int halfWidth = width / 2;
            int shrink = 4;

            boolean leftHovered = isOptionHovered(mouseX, mouseY);
            boolean rightHovered = isQuitHovered(mouseX, mouseY);

            float animSpeed = 0.04f;

            if (leftHovered) {
                leftHoverAnim = Math.min(1f, leftHoverAnim + animSpeed);
                leftScale = Math.min(1.03f, leftScale + animSpeed * 0.75f);
                leftGlowAnim = Math.min(1f, leftGlowAnim + animSpeed * 2);
            } else {
                leftHoverAnim = Math.max(0f, leftHoverAnim - animSpeed);
                leftScale = Math.max(1f, leftScale - animSpeed * 0.75f);
                leftGlowAnim = Math.max(0f, leftGlowAnim - animSpeed * 2);
            }

            if (rightHovered) {
                rightHoverAnim = Math.min(1f, rightHoverAnim + animSpeed);
                rightScale = Math.min(1.03f, rightScale + animSpeed * 0.75f);
                rightGlowAnim = Math.min(1f, rightGlowAnim + animSpeed * 2);
            } else {
                rightHoverAnim = Math.max(0f, rightHoverAnim - animSpeed);
                rightScale = Math.max(1f, rightScale - animSpeed * 0.75f);
                rightGlowAnim = Math.max(0f, rightGlowAnim - animSpeed * 2);
            }

            int baseColor = new Color(25, 25, 25, (int)(120 * fadeAlpha)).getRGB();
            int hoverColor = new Color(50, 30, 30, (int)(180 * fadeAlpha)).getRGB();

            int buttonWidth = halfWidth - shrink;

            // Левая кнопка (Options)
            int leftX = x + buttonGap;
            int leftBg = ColorUtil.blendColorsInt(baseColor, hoverColor, leftHoverAnim);

            context.getMatrices().push();
            context.getMatrices().translate(leftX + buttonWidth / 2f, y + height / 2f, 0);
            context.getMatrices().scale(leftScale, leftScale, 1);
            context.getMatrices().translate(-(leftX + buttonWidth / 2f), -(y + height / 2f), 0);

            if (leftGlowAnim > 0) {
                int glowAlpha = (int)(leftGlowAnim * 80 * fadeAlpha);
                RenderUtil.drawRoundedRect(context.getMatrices(),
                        leftX - 3, y - 3,
                        buttonWidth + 6, height + 6,
                        10, new Color(255, 140, 140, glowAlpha).getRGB());
            }

            RenderUtil.drawRoundedRect(context.getMatrices(), leftX + 2, y + 2, buttonWidth, height, 7, new Color(0, 0, 0, (int)(100 * fadeAlpha)).getRGB());
            RenderUtil.drawRoundedRect(context.getMatrices(), leftX, y, buttonWidth, height, 7, leftBg);

            if (leftHoverAnim > 0) {
                int borderAlpha = (int)(leftHoverAnim * 150 * fadeAlpha);
                RenderUtil.drawRoundedRect(context.getMatrices(), leftX, y, buttonWidth, height, 7, new Color(255, 140, 140, borderAlpha).getRGB());
            }

            int textColor = new Color(255, 255, 255, (int)(255 * fadeAlpha)).getRGB();
            FontUtils.sf_medium[20].centeredDraw(context.getMatrices(), leftName, leftX + buttonWidth / 2f, y + (height - 10) / 2.2f, textColor);
            context.getMatrices().pop();

            // Правая кнопка (Quit)
            int rightX = x + halfWidth + buttonGap;
            int rightBg = ColorUtil.blendColorsInt(baseColor, hoverColor, rightHoverAnim);

            context.getMatrices().push();
            context.getMatrices().translate(rightX + buttonWidth / 2f, y + height / 2f, 0);
            context.getMatrices().scale(rightScale, rightScale, 1);
            context.getMatrices().translate(-(rightX + buttonWidth / 2f), -(y + height / 2f), 0);

            if (rightGlowAnim > 0) {
                int glowAlpha = (int)(rightGlowAnim * 80 * fadeAlpha);
                RenderUtil.drawRoundedRect(context.getMatrices(),
                        rightX - 3, y - 3,
                        buttonWidth + 6, height + 6,
                        10, new Color(255, 140, 140, glowAlpha).getRGB());
            }

            RenderUtil.drawRoundedRect(context.getMatrices(), rightX + 2, y + 2, buttonWidth, height, 7, new Color(0, 0, 0, (int)(100 * fadeAlpha)).getRGB());
            RenderUtil.drawRoundedRect(context.getMatrices(), rightX, y, buttonWidth, height, 7, rightBg);

            if (rightHoverAnim > 0) {
                int borderAlpha = (int)(rightHoverAnim * 150 * fadeAlpha);
                RenderUtil.drawRoundedRect(context.getMatrices(), rightX, y, buttonWidth, height, 7, new Color(255, 140, 140, borderAlpha).getRGB());
            }

            FontUtils.sf_medium[20].centeredDraw(context.getMatrices(), rightName, rightX + buttonWidth / 2f, y + (height - 10) / 2.2f, textColor);
            context.getMatrices().pop();
        }

        boolean isOptionHovered(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width / 2 - 1 && mouseY >= y && mouseY <= y + height;
        }

        boolean isQuitHovered(double mouseX, double mouseY) {
            return mouseX > x + width / 2 + 1 && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}