package dev.neoclient.mixin;

import dev.neoclient.CameraControl;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Redirects mouse look to the detached camera, keeping mouse sensitivity and freelook angles under client control. */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void neoclient$look(double dx, double dy, CallbackInfo ci) {
        if (CameraControl.isCamera((Entity) (Object) this) && CameraControl.look(dx, dy)) ci.cancel();
    }
}
