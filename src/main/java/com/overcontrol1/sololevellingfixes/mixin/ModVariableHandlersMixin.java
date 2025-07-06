package com.overcontrol1.sololevellingfixes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraftforge.common.util.LazyOptional;
import net.solocraft.network.SololevelingModVariables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SololevelingModVariables.EventBusVariableHandlers.class, remap = false)
public class ModVariableHandlersMixin {
    @Redirect(method = "clonePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/util/LazyOptional;orElse(Ljava/lang/Object;)Ljava/lang/Object;"))
    private static Object switchToSupplier(LazyOptional<SololevelingModVariables.PlayerVariables> instance, Object other) {
        return instance.orElseGet(SololevelingModVariables.PlayerVariables::new);
    }

    @WrapOperation(method = "clonePlayer", at = @At(value = "NEW", target = "()Lnet/solocraft/network/SololevelingModVariables$PlayerVariables;"))
    private static SololevelingModVariables.PlayerVariables avoidCopy(Operation<SololevelingModVariables.PlayerVariables> original) {
        return null;
    }
}
