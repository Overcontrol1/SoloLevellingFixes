package com.overcontrol1.sololevellingfixes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import net.minecraft.nbt.Tag;
import net.minecraftforge.network.NetworkEvent;
import net.solocraft.network.SololevelingModVariables;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Debug(export = true)
@Mixin(value = SololevelingModVariables.PlayerVariablesSyncMessage.class, remap = false)
public class ModVariableSyncMessageMixin {
    @Unique
    private Tag solo_levelling_fixes$cached_nbt;

    @WrapOperation(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "NEW", target = "Lnet/solocraft/network/SololevelingModVariables$PlayerVariables;"))
    private SololevelingModVariables.PlayerVariables bypassVariablesCreation(Operation<SololevelingModVariables.PlayerVariables> original) {
        return null;
    }

    @WrapOperation(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/solocraft/network/SololevelingModVariables$PlayerVariables;readNBT(Lnet/minecraft/nbt/Tag;)V"))
    private void cacheNbt(SololevelingModVariables.PlayerVariables instance, Tag tag, Operation<Void> original) {
        solo_levelling_fixes$cached_nbt = tag;
    }

    @SuppressWarnings({"InvalidInjectorMethodSignature", "MixinAnnotationTarget"})
//    @ModifyExpressionValue(method = "lambda$handler$0", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/util/LazyOptional;orElseGet(Lnet/minecraftforge/common/util/NonNullSupplier;)Ljava/lang/Object;"))
    private static Object bypassCopy(Object variables, NetworkEvent.Context context,
                                                                               SololevelingModVariables.PlayerVariablesSyncMessage message,
                                                                               @Cancellable CallbackInfo ci) {
        var messageExt = ((ModVariableSyncMessageMixin)(Object) message);
        assert messageExt != null;
        ((SololevelingModVariables.PlayerVariables)variables).readNBT(messageExt.solo_levelling_fixes$cached_nbt);
        messageExt.solo_levelling_fixes$cached_nbt = null;

        ci.cancel();

        return variables;
    }
}
