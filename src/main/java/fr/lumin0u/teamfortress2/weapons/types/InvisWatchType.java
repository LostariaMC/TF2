package fr.lumin0u.teamfortress2.weapons.types;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.collect.ImmutableList.Builder;
import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.NMSUtils;
import fr.lumin0u.teamfortress2.util.TFSound;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import fr.lumin0u.teamfortress2.weapons.types.DisguiseType.Disguise;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.world.entity.player.EntityHuman;
import org.bukkit.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;

import static java.util.function.Predicate.not;

public final class InvisWatchType extends WeaponType
{
	public final int duration = 120;
	
	public InvisWatchType() {
		super(false, Material.CLOCK, "Montre d'invisibilité", 1, 318, -1);
	}
	
	@Override
	public Weapon createWeapon(TFPlayer owner, int slot) {
		return new InvisWatch(owner, slot);
	}
	
	@Override
	protected Builder<String> loreBuilder() {
		return super.loreBuilder()
				.add(RIGHT_CLICK_LORE.formatted("rend complètement invisible"))
				.add("§eaux yeux de vos ennemis")
				.add(DURATION_LORE.formatted((float) ((float) duration / 20f)));
	}
	
	@Override
	public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
		player.setSpyInvisible(true);
		((InvisWatch)weapon).setInvisibilityCancelled(false);
		
		//player.getOptWeapon(WeaponTypes.DISGUISE).ifPresent(w -> ((Disguise)w).setDisguiseCancelled(true));
		
		player.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 1, false, false, false));
		player.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, duration, 0, false, false, true));
		
		PacketContainer packetDisappear = new PacketContainer(Server.ENTITY_DESTROY, new PacketPlayOutEntityDestroy(player.toBukkit().getEntityId()));
		
		Bukkit.getOnlinePlayers().stream()
				.map(TFPlayer::of)
				.filter(player::isEnemy)
				.filter(not(TFPlayer::isSpectator))
				.forEach(p -> p.sendPacket(packetDisappear));
		
		//PacketContainer packetAppear = new PacketContainer(Server.NAMED_ENTITY_SPAWN, new PacketPlayOutNamedEntitySpawn((EntityHuman) NMSUtils.getHandle(player.toBukkit())));
		
		new BukkitRunnable() {
			int tick = 0;
			
			@Override
			public void run() {
				if(!player.isOnline()) {
					cancel();
					return;
				}
				
				if(tick == duration || ((InvisWatch)weapon).invisibilityCancelled()) {
					
					TFSound.SPY_INVIS_END.play(player.toBukkit().getLocation());
					
					((InvisWatch)weapon).setInvisibilityCancelled(false);
					player.setSpyInvisible(false);
					Bukkit.getOnlinePlayers().stream()
							.map(TFPlayer::of)
							.filter(player::isEnemy)
							.filter(not(TFPlayer::isSpectator))
							.forEach(p -> {
								p.toBukkit().hidePlayer(TF.getInstance(), player.toBukkit());
								p.toBukkit().showPlayer(TF.getInstance(), player.toBukkit());
							});
					
					player.toBukkit().removePotionEffect(PotionEffectType.SPEED);
					player.toBukkit().removePotionEffect(PotionEffectType.INVISIBILITY);
					cancel();
					return;
				}
				
				if(player.toBukkit().getFireTicks() > 0 && tick % 5 == 0) {
					Location l = player.toBukkit().getLocation().add(0, 1.2, 0);
					l.getWorld().spawnParticle(Particle.SMALL_FLAME, l, 1, 0.2, 0.5, 0.2, 0.000001D, null, true);
				}
				
				tick++;
			}
		}.runTaskTimer(TF.getInstance(), 1, 1);
		
		weapon.useAmmo();
	}
	
	@Override
	public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {}
	
	public static final class InvisWatch extends Weapon {
		private boolean invisibilityCancelled;
		
		public InvisWatch(TFPlayer owner, int slot) {
			super(WeaponTypes.INVIS_WATCH, owner, slot);
		}
		
		public void setInvisibilityCancelled(boolean invisibilityCancelled) {
			this.invisibilityCancelled = invisibilityCancelled;
		}
		
		public boolean invisibilityCancelled() {
			return invisibilityCancelled;
		}
		
		@Override
		public void rightClick(RayTraceResult info) {
			if(owner.isSpyInvisible() && TF.currentTick() - lastActionDate > 20) {
				setInvisibilityCancelled(true);
			}
			else {
				super.rightClick(info);
			}
		}
		
		@Override
		public void remove() {
			super.remove();
			setInvisibilityCancelled(true);
		}
	}
}
