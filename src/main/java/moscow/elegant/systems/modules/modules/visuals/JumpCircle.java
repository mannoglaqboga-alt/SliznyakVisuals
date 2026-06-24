package moscow.elegant.systems.modules.modules.visuals;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import moscow.elegant.elegant;
import moscow.elegant.systems.event.EventListener;
import moscow.elegant.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.elegant.systems.event.impl.render.Render3DEvent;
import moscow.elegant.systems.modules.api.ModuleCategory;
import moscow.elegant.systems.modules.api.ModuleInfo;
import moscow.elegant.systems.modules.impl.BaseModule;
import moscow.elegant.systems.setting.settings.ModeSetting;
import moscow.elegant.systems.setting.settings.SliderSetting;
import moscow.elegant.utility.colors.ColorRGBA;
import moscow.elegant.utility.render.DrawUtility;
import moscow.elegant.utility.time.Timer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(
   name = "Jump Circle",
   category = ModuleCategory.VISUALS,
   desc = "modules.descriptions.jump_circle"
)
public class JumpCircle extends BaseModule {
   private final List<Circle> circles = new ArrayList<>();
   private final Identifier circleTexture = elegant.id("textures/circle.png");
   private boolean wasOnGround = true;

   private final ModeSetting style = new ModeSetting(this, "modules.settings.jump_circle.style");
   private final ModeSetting.Value defaultStyle = new ModeSetting.Value(this.style, "modules.settings.jump_circle.style.default").select();

   private final SliderSetting speed = new SliderSetting(this, "modules.settings.jump_circle.speed")
      .min(0.1F).max(5.0F).step(0.1F).currentValue(1.0F);
   private final SliderSetting size = new SliderSetting(this, "modules.settings.jump_circle.size")
      .min(0.5F).max(10.0F).step(0.1F).currentValue(2.0F);

   private final EventListener<ClientPlayerTickEvent> onTick = event -> {
      if (mc.player == null) return;

      boolean isOnGround = mc.player.isOnGround();

      if (wasOnGround && !isOnGround) {
         Vec3d pos = new Vec3d(
            mc.player.getX(),
            Math.floor(mc.player.getY()) + 0.001,
            mc.player.getZ()
         );
         float currentSpeed = speed.getCurrentValue();
         float currentMaxSize = size.getCurrentValue();
         float lifeMs = 3000f / currentSpeed;
         circles.add(new Circle(pos, new Timer(), lifeMs, currentMaxSize));
      }

      wasOnGround = isOnGround;

      circles.removeIf(c -> c.timer.finished((long) c.maxTime()));
   };

   private final EventListener<Render3DEvent> onRender = event -> {
      renderCircles(event);
   };

   @Override
   public void onEnable() {
      super.onEnable();
      circles.clear();
      wasOnGround = true;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      circles.clear();
   }

   private void renderCircles(Render3DEvent event) {
      if (circles.isEmpty()) return;

      MatrixStack ms = event.getMatrices();
      Camera camera = event.getCamera();
      Vec3d cameraPos = camera.getPos();

      boolean hasAny = false;
      for (Circle c : circles) {
         if (c.timer.getElapsedTime() < c.maxTime()) {
            hasAny = true;
            break;
         }
      }
      if (!hasAny) return;

      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      RenderSystem.setShaderTexture(0, circleTexture);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.disableDepthTest();
      RenderSystem.disableCull();
      RenderSystem.depthMask(false);

      BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

      for (Circle circle : circles) {
         long lifeTime = circle.timer.getElapsedTime();
         float maxTime = circle.maxTime();
         float maxScale = circle.maxScale();
         float progress = lifeTime / maxTime;

         if (progress >= 1f) continue;

         // Плавное расползание (ease-out)
         float eased = 1f - (1f - progress) * (1f - progress);
         float scale = eased * maxScale;
         float alpha = 1f - (progress * progress);

         float hue = (progress * 360f) % 360f;
         ColorRGBA base = ColorRGBA.fromHSB(hue, 0.7f, 1.0f);
         ColorRGBA color = base.withAlpha(alpha * 255f);

         ms.push();
         double dx = circle.pos().x - cameraPos.x;
         double dy = circle.pos().y - cameraPos.y;
         double dz = circle.pos().z - cameraPos.z;
         ms.translate(dx, dy, dz);

         ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90f));

         float half = scale / 2.0f;
         DrawUtility.drawImage(ms, buffer, -half, -half, 0.0, scale, scale, color);
         ms.pop();
      }

      BufferRenderer.drawWithGlobalProgram(buffer.end());

      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
   }

   private record Circle(Vec3d pos, Timer timer, float maxTime, float maxScale) {}
}
