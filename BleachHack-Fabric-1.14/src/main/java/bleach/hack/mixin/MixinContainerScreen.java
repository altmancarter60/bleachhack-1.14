/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
 * Copyright (c) 2019 Bleach.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.gui.screen.ingame.ContainerProvider;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.container.Container;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.packet.PlayerInteractEntityC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventDrawContainer;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.MountBypass;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinContainerScreen<T extends Container> extends Screen implements ContainerProvider<T> {

	@Shadow
	public int left;
	
	@Shadow
	public int top;
	
	public MixinContainerScreen(Container container_1, PlayerInventory playerInventory_1, Text text_1) {
		super(text_1);
	}
	
	@Inject(at = @At("RETURN"), method = "init()V")
	protected void init(CallbackInfo info) {
		if (!(MinecraftClient.getInstance().player.getVehicle() instanceof AbstractDonkeyEntity)) {
			return;
		}
		
		AbstractDonkeyEntity entity = (AbstractDonkeyEntity) MinecraftClient.getInstance().player.getVehicle();
	    
		addButton(new ButtonWidget(left + 130, top + 4, 39, 12, "Dupe", button -> {
			((MountBypass) ModuleManager.getModule(MountBypass.class)).dontCancel = true;
			
			MinecraftClient.getInstance().player.networkHandler.sendPacket(
					new PlayerInteractEntityC2SPacket(
							entity, Hand.MAIN_HAND, entity.getPos().add(entity.getWidth() / 2, entity.getHeight() / 2, entity.getWidth() / 2)));
			
			((MountBypass) ModuleManager.getModule(MountBypass.class)).dontCancel = false;
		}));
	}

	@Inject(at = @At("RETURN"), method = "render(IIF)V")
	public void render(int int_1, int int_2, float float_1, CallbackInfo info) {
		EventDrawContainer event = new EventDrawContainer(
				(AbstractContainerScreen<?>) MinecraftClient.getInstance().currentScreen, int_1, int_2); // hmm
		BleachHack.eventBus.post(event);
		if (event.isCancelled()) info.cancel();
	}
}