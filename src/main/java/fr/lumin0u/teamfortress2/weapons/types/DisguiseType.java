package fr.lumin0u.teamfortress2.weapons.types;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.common.collect.ImmutableList.Builder;
import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.NMSUtils;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import fr.worsewarn.cosmox.API;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Predicate;

public final class DisguiseType extends WeaponType
{
	private final int duration = 200;
	
	public DisguiseType() {
		super(true, Material.PUFFERFISH, "Kit de Déguisement", 1, -1, -1);
	}
	
	@Override
	public Weapon createWeapon(TFPlayer owner, int slot) {
		return new Disguise(owner, slot);
	}
	
	@Override
	protected Builder<String> loreBuilder() {
		return super.loreBuilder().add(RIGHT_CLICK_LORE.formatted("se déguiser temporairement en")).add("§evotre dernière victime").add(DURATION_LORE.formatted((float) ((float) duration / 20f)));
	}
	
	@Override
	public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
		activateDisguise(player, weapon, info);
		if(player.isSpyInvisible()) {
		}
	}
	
	public void activateDisguise(TFPlayer player, Weapon weapon, RayTraceResult info) {
		final Predicate<TFPlayer> shouldBeTricked = p -> player.isEnemy(p) && !p.isSpectator();
		
		TFPlayer disguise = TFPlayer.of(player.getNextDisguise());
		player.setDisguise(disguise);
		weapon.useAmmo();
		
		int id = new Random().nextInt(10000000) + 100000;
		
		Disguise disguiseWeapon = (Disguise) weapon;
		disguiseWeapon.setDisguiseCancelled(false);
		
		EntityPlayer evilCloneHandle;
		Player evilClone;
		Constructor<?> playerInfoUpdatePacketConstructor;
		ClientboundPlayerInfoUpdatePacket packetPlayerInfoAddCloneHandle;
		
		try {
			Constructor<?> humanConstructor = MinecraftReflection.getEntityPlayerClass().getDeclaredConstructor(
					MinecraftReflection.getMinecraftServerClass(),
					MinecraftReflection.getWorldServerClass(),
					MinecraftReflection.getGameProfileClass());
			
			WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(disguise.toBukkit());
			WrappedGameProfile cloneGameProfile = new WrappedGameProfile(UUID.randomUUID(), gameProfile.getName());
			cloneGameProfile.getProperties().putAll(gameProfile.getProperties());
			
			evilCloneHandle = ((EntityPlayer) humanConstructor.newInstance(
					NMSUtils.getMinecraftServer(),
					NMSUtils.getHandle(disguise.getLocation().getWorld()),
					cloneGameProfile.getHandle()));
			evilClone = evilCloneHandle.getBukkitEntity();
			
			
			playerInfoUpdatePacketConstructor = ClientboundPlayerInfoUpdatePacket.class.getDeclaredConstructor(
					EnumSet.class,
					Collection.class);
			evilClone.setDisplayName(disguise.toBukkit().getDisplayName());
			
			packetPlayerInfoAddCloneHandle = (ClientboundPlayerInfoUpdatePacket) playerInfoUpdatePacketConstructor.newInstance(
					EnumSet.of((Enum) EnumWrappers.getPlayerInfoActionConverter().getGeneric(PlayerInfoAction.ADD_PLAYER)/*, (Enum) EnumWrappers.getPlayerInfoActionConverter().getGeneric(PlayerInfoAction.UPDATE_LISTED)*/, (Enum) EnumWrappers.getPlayerInfoActionConverter().getGeneric(PlayerInfoAction.UPDATE_DISPLAY_NAME)),
					List.of(evilCloneHandle));
			
		}catch(ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
		
		//PacketContainer packetDisappear = new PacketContainer(Server.ENTITY_DESTROY, new PacketPlayOutEntityDestroy(player.toBukkit().getEntityId()));
		
		PacketContainer packetPlayerInfoAddClone = new PacketContainer(Server.PLAYER_INFO, packetPlayerInfoAddCloneHandle);
		//PacketContainer packetSpawnClone = new PacketContainer(Server.NAMED_ENTITY_SPAWN, new PacketPlayOutNamedEntitySpawn(evilCloneHandle));
		//packetSpawnClone.getIntegers().write(0, id);
		
		PacketContainer packetDestroyClone = new PacketContainer(Server.ENTITY_DESTROY, new PacketPlayOutEntityDestroy(id));
		
		Bukkit.getOnlinePlayers().stream()
				.map(TFPlayer::of)
				.filter(shouldBeTricked)
				.forEach(p -> {
					
					p.toBukkit().hidePlayer(TF.getInstance(), player.toBukkit());
					p.sendPacket(packetPlayerInfoAddClone);
					
					Bukkit.getScheduler().runTaskLater(TF.getInstance(), () -> {
						if(!player.isSpyInvisible()) {
							p.toBukkit().showPlayer(TF.getInstance(), player.toBukkit());
						}
					}, 2);
				});
		
		player.giveArmor();
		
		if(disguiseWeapon.packetListener != null && API.instance().getProtocolManager().getPacketListeners().contains(disguiseWeapon.packetListener)) {
			API.instance().getProtocolManager().removePacketListener(disguiseWeapon.packetListener);
		}
		
		disguiseWeapon.packetListener = new PacketAdapter(TF.getInstance(), Server.ENTITY_EQUIPMENT, Server.ENTITY_DESTROY, Server.ANIMATION, Server.NAMED_ENTITY_SPAWN, Server.ENTITY_HEAD_ROTATION, Server.REL_ENTITY_MOVE_LOOK, Server.REL_ENTITY_MOVE, Server.ENTITY_LOOK, Server.ENTITY_TELEPORT, Server.HURT_ANIMATION, Server.ENTITY_METADATA) {
			@Override
			public void onPacketSending(PacketEvent event) {
				if(!shouldBeTricked.test(TFPlayer.of(event.getPlayer())))
					return;
				
				if(event.getPacket().getType().equals(Server.ENTITY_DESTROY)) {
					PacketContainer newPacket = event.getPacket().deepClone();
					newPacket.getIntLists().write(0, newPacket.getIntLists().read(0).stream().map(i -> i == player.toBukkit().getEntityId() ? id : i).toList());
					event.setPacket(newPacket);
				}
				else if(event.getPacket().getIntegers().read(0) == player.toBukkit().getEntityId()) {
					PacketContainer newPacket = event.getPacket().deepClone();
					newPacket.getIntegers().write(0, id);
					
					if(event.getPacketType().equals(Server.NAMED_ENTITY_SPAWN)) {
						newPacket.getUUIDs().write(0, evilClone.getUniqueId());
					}
					
					event.setPacket(newPacket);
				}
			}
		};
		
		Bukkit.getScheduler().runTaskLater(TF.getInstance(), () -> {
			API.instance().getProtocolManager().addPacketListener(disguiseWeapon.packetListener);
		}, 1);
		
		new BukkitRunnable() {
			int tick = 0;
			@Override
			public void run() {
				if(tick == duration || disguiseWeapon.disguiseCancelled()) {
					API.instance().getProtocolManager().removePacketListener(disguiseWeapon.packetListener);
					
					player.setDisguise(null);
					player.giveArmor();
					
					Bukkit.getOnlinePlayers().stream()
							.map(TFPlayer::of)
							.forEach(p -> {
								p.sendPacket(packetDestroyClone);
								if(!disguiseWeapon.disguiseCancelled()) {
									p.toBukkit().hidePlayer(TF.getInstance(), player.toBukkit());
									p.toBukkit().showPlayer(TF.getInstance(), player.toBukkit());
								}
							});
					
					disguiseWeapon.setDisguiseCancelled(false);
					
					cancel();
					return;
				}
				
				tick++;
			}
		}.runTaskTimer(TF.getInstance(), 1, 1);
	}
	
	@Override
	public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {}
	
	
	public static final class Disguise extends Weapon {
		private boolean disguiseCancelled;
		private PacketListener packetListener;
		
		public Disguise(TFPlayer owner, int slot) {
			super(WeaponTypes.DISGUISE, owner, slot);
		}
		
		public void setDisguiseCancelled(boolean disguiseCancelled) {
			this.disguiseCancelled = disguiseCancelled;
			if(packetListener != null)
				API.instance().getProtocolManager().removePacketListener(packetListener);
		}
		
		public boolean disguiseCancelled() {
			return disguiseCancelled;
		}
		
		@Override
		public void remove() {
			super.remove();
			setDisguiseCancelled(true);
		}
	}
}
