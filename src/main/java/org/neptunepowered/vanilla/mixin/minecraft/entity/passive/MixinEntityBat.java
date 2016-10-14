/*
 * This file is part of NeptuneVanilla, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015-2016, Jamie Mansfield <https://github.com/jamierocks>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.neptunepowered.vanilla.mixin.minecraft.entity.passive;

import net.canarymod.api.entity.EntityType;
import net.canarymod.api.entity.living.animal.Bat;
import net.canarymod.api.inventory.Item;
import net.minecraft.entity.passive.EntityBat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityBat.class)
public abstract class MixinEntityBat extends MixinEntityAmbientCreature implements Bat {

    @Shadow public abstract boolean getIsBatHanging();
    @Shadow public abstract void setIsBatHanging(boolean isHanging);

    @Override
    public boolean isAnimal() {
        return true;
    }

    @Override
    public boolean isBreedingItem(Item item) {
        return false;
    }

    @Override
    public boolean isHanging() {
        return this.getIsBatHanging();
    }

    @Override
    public void setHanging(boolean b) {
        this.setIsBatHanging(b);
    }

    @Override
    public String getFqName() {
        return "Bat";
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.BAT;
    }

}
