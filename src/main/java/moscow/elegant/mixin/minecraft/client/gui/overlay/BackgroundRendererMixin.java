package moscow.elegant.mixin.minecraft.client.gui.overlay;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import moscow.elegant.elegant;
import moscow.elegant.systems.modules.modules.visuals.CustomFog;
import moscow.elegant.utility.colors.ColorRGBA;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.BackgroundRenderer.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({BackgroundRenderer.class})
public class BackgroundRendererMixin {
   @ModifyReturnValue(
      method = {"applyFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/BackgroundRenderer$FogType;Lorg/joml/Vector4f;FZF)Lnet/minecraft/client/render/Fog;"},
      at = {@At("RETURN")}
   )
   private static Fog modifyFogProperties(
      Fog original,
      @Local(argsOnly = true) Camera camera,
      @Local(argsOnly = true) FogType fogType,
      @Local(argsOnly = true,ordinal = 0) float viewDistance
   ) {
      CustomFog customFogModule = elegant.getInstance().getModuleManager().getModule(CustomFog.class);
      if (customFogModule.shouldModifyFog(camera) && fogType == FogType.FOG_TERRAIN) {
         float density = customFogModule.getFogDensity();
         float start = MathHelper.clamp(customFogModule.getDistance().getFirstValue() / density, -8.0F, viewDistance);
         float end = MathHelper.clamp(customFogModule.getDistance().getSecondValue() / density, 0.0F, viewDistance);
         ColorRGBA color = customFogModule.getFogColorValue();
         FogShape shape = FogShape.SPHERE;
         float r = color.getRed() / 255.0F;
         float g = color.getGreen() / 255.0F;
         float b = color.getBlue() / 255.0F;
         float a = 1.0F;
         return new Fog(start, end, shape, r, g, b, a);
      } else {
         return original;
      }
   }
}
