package fr.lumin0u.teamfortress2.weapons;

import com.google.common.collect.ImmutableList.Builder;
import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.TFEntity;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.ItemBuilder;
import fr.lumin0u.teamfortress2.util.TFSound;
import fr.lumin0u.teamfortress2.weapons.types.WeaponType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Medigun extends Weapon
{
	private final Map<TFPlayer, Long> healStartDate = new HashMap<>();
	private final int healDuration;
	
	public Medigun(WeaponType type, TFPlayer owner, int slot, int healDuration) {
		super(type, owner, slot);
		this.healDuration = healDuration;
		
		bukkitTasks.add(new BukkitRunnable()
		{
			int tick = 0;
			
			@Override
			public void run() {
				new HashMap<>(healStartDate).forEach((ally, startDate) -> {
					if(ally.isDead()) {
						healStartDate.remove(ally);
						TFSound.HEAL_STOP.playTo(ally);
					}
				});
				
				new HashMap<>(healStartDate).forEach((ally, startDate) -> {
					if(TF.currentTick() - startDate < healDuration) {
						if(ally.toBukkit().getHealth() == ally.toBukkit().getMaxHealth()) {
							
							double currentHealth = ally.toBukkit().getHealth();
							int currentAmplifier = Optional.ofNullable(ally.toBukkit().getPotionEffect(PotionEffectType.HEALTH_BOOST)).map(PotionEffect::getAmplifier).orElse(-1);
							ally.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, healDuration * 3, Math.min(4, currentAmplifier + 1)));
							ally.toBukkit().setHealth(currentHealth);
						}
						
						if(tick % 10 == 0 && ally.isNot(owner)) {
							healParticleSphere(ally.getLocation().add(0, 1, 0));
							
							Vector direction = ally.getLocation().clone().subtract(owner.getLocation()).toVector().normalize();
							final double particlePerBlock = 4;
							for(Location point = owner.getLocation(); point.distance(ally.getLocation()) > 2 / particlePerBlock; point.add(direction.clone().multiply(1 / particlePerBlock))) {
								point.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, point.clone().add(0, 1, 0), 1, 0, 0, 0, 0, null, true);
							}
						}
					}
					else {
						ally.toBukkit().getInventory().setArmorContents(Arrays.stream(ally.toBukkit().getInventory().getArmorContents())
								.map(item -> item == null ? null : ItemBuilder.modify(item).setGlow(false).build())
								.toArray(ItemStack[]::new));
						
						healStartDate.remove(ally);
						TFSound.HEAL_STOP.playTo(ally);
					}
				});
				
				tick++;
			}
		}.runTaskTimer(TF.getInstance(), 1, 1));
	}
	
	public void startHealing(TFPlayer target, int amplifier) {
		healStartDate.put(target, TF.currentTick());
		target.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, healDuration, amplifier, true, true, true));
		
		TFSound.GET_HEALED.playTo(target);
		
		target.toBukkit().getInventory().setArmorContents(Arrays.stream(target.toBukkit().getInventory().getArmorContents())
				.map(item -> item == null ? null : ItemBuilder.modify(item).setGlow(true).build())
				.toArray(ItemStack[]::new));
		
		double currentHealth = target.toBukkit().getHealth();
		int currentAmplifier = Optional.ofNullable(target.toBukkit().getPotionEffect(PotionEffectType.HEALTH_BOOST)).map(PotionEffect::getAmplifier).orElse(-1);
		if(currentAmplifier > -1) {
			target.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, healDuration * 2, Math.min(4, currentAmplifier)));
			target.toBukkit().setHealth(currentHealth);
		}
	}
	
	public static class MedigunType extends WeaponType
	{
		private final double range = 30;
		private static final int duration = 140;
		
		public MedigunType() {
			super(false, Material.PURPLE_DYE, "Kit medical", 1, 82, -1);
		}
		
		@Override
		protected Builder<String> loreBuilder() {
			return super.loreBuilder()
					.add(RIGHT_CLICK_LORE.formatted("heal le joueur visé (regen II)"))
					.add(RANGE_LORE.formatted(range))
					.add(LEFT_CLICK_LORE.formatted("vous heal (regen I)"))
					.add(DURATION_LORE.formatted((float) ((float) duration / 20f)));
		}
		
		@Override
		public Weapon createWeapon(TFPlayer owner, int slot) {
			return new Medigun(this, owner, slot, duration);
		}
		
		@Override
		public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
			Location source = player.toBukkit().getEyeLocation();
			Vector direction = source.getDirection();
			
			boolean healedSomeone = false;
			
			for(TFEntity ent : GameManager.getInstance().getLivingEntities()) {
				double distance = ent.getLocation().distance(player.getLocation());
				
				if(player.is(ent) || player.isEnemy(ent) || distance > range || !(ent instanceof TFPlayer target))
					continue;
				
				double m = distance / 10 + 0.3;
				BoundingBox bodyBox = target.getBodyBox().clone().expand(m);
				BoundingBox headBox = target.getHeadBox().clone().expand(m);
				
				RayTraceResult bodyCollision = bodyBox.rayTrace(source.toVector(), direction, range);
				RayTraceResult headCollision = headBox.rayTrace(source.toVector(), direction, range);
				
				if(bodyCollision != null || headCollision != null) {
					((Medigun) weapon).startHealing(target, 1);
					healedSomeone = true;
					break;
				}
			}
			
			if(healedSomeone)
				weapon.useAmmo();
		}
		
		@Override
		public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
			((Medigun) weapon).startHealing(player, 0);
			
			weapon.useAmmo();
		}
	}
	
	public static void healParticleSphere(Location loc) {
		//loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc.clone().add(0, 0, 0), 30, 0.5, 0.8, 0.5, 0, null, true);
	}
	
	public static class UberCharge extends Medigun {
		
		public UberCharge(WeaponType type, TFPlayer owner, int slot, int healDuration) {
			super(type, owner, slot, healDuration);
		}
	}
	
	public static class UberChargeType extends WeaponType
	{
		private final double range = 20;
		private static final int duration = 180;
		
		public UberChargeType() {
			super(true, Material.POPPED_CHORUS_FRUIT, "Uber Charge", 1, -1, -1);
		}
		
		@Override
		protected Builder<String> loreBuilder() {
			return super.loreBuilder()
					.add(RIGHT_CLICK_LORE.formatted("heal les joueurs proches (regen IV)"))
					.add(RADIUS_LORE.formatted(range))
					.add(DURATION_LORE.formatted((float) ((float) duration / 20f)));
		}
		
		@Override
		public Weapon createWeapon(TFPlayer owner, int slot) {
			return new UberCharge(this, owner, slot, duration);
		}
		
		@Override
		public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
			for(TFEntity ent : GameManager.getInstance().getLivingEntities()) {
				
				double distance = ent.getLocation().distance(player.getLocation());
				if(player.isEnemy(ent) || distance > range || !(ent instanceof TFPlayer target))
					continue;
				
				((Medigun) weapon).startHealing(target, 3);
			}
			
			weapon.useAmmo();
		}
		
		@Override
		public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {}
	}
}
