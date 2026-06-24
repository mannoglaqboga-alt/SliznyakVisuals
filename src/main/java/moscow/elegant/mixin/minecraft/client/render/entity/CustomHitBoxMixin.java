package moscow.elegant.mixin.minecraft.client.render.entity;

import moscow.elegant.elegant;
import moscow.elegant.systems.modules.modules.visuals.CustomHitBox;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityRenderer.class)
public abstract class CustomHitBoxMixin<T extends Entity, S extends EntityRenderState> {
   // No longer cancelling or modifying vanilla hitbox; CustomHitBox now draws its own filled boxes
}