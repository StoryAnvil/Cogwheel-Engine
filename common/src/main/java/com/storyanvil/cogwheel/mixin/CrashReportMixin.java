/*
 *
 * StoryAnvil Cogwheel Engine
 * Copyright (C) 2025 StoryAnvil
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.storyanvil.cogwheel.mixin;

import net.minecraft.util.crash.ReportType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ReportType.class)
public class CrashReportMixin {
    @Inject(at = @At("TAIL"), method = "addHeaderAndNugget")
    public void addHeaderAndNuggetMixin(StringBuilder reportBuilder, List<String> extraInfo, CallbackInfo ci) {
        reportBuilder.append("// Cogwheel Engine is installed!\n");
        reportBuilder.append("// Its scripts are written by modpack developer\n");
        reportBuilder.append("// and this scripts can cause crashes.\n");
        reportBuilder.append("// This is just a warning. It does not indicate\n");
        reportBuilder.append("// that crash was caused by Cogwheel Engine's script.\n\n");
    }
}
