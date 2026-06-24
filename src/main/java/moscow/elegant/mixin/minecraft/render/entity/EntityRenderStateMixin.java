package moscow.elegant.mixin.minecraft.render.entity;

import moscow.elegant.utility.mixins.EntityRenderStateAddition;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({EntityRenderState.class})
public abstract class EntityRenderStateMixin implements EntityRenderStateAddition {
   @Unique
   private Entity elegant$entity;

   @Unique
   @Override
   public void elegant$setEntity(Entity entity) {
      this.elegant$entity = entity;
   }

   @Unique
   @Override
   public Entity elegant$getEntity() {
      return this.elegant$entity;
   }
}
