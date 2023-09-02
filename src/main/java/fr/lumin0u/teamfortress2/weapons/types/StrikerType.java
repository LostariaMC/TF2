package fr.lumin0u.teamfortress2.weapons.types;

import com.google.common.collect.ImmutableList.Builder;
import fr.lumin0u.teamfortress2.game.managers.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.TFSound;
import fr.lumin0u.teamfortress2.weapons.ThrownExplosive;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public final class StrikerType extends WeaponType
{
	private final double centerDamage = 14;
	private final double radius = 8;
	
	public StrikerType() {
		super(true, Material.IRON_HORSE_ARMOR, "Striker", 4, -1, 10);
	}
	
	@Override
	protected Builder<String> loreBuilder() {
		return super.loreBuilder().add(CUSTOM_LORE.formatted("Tire un projectile explosif")).add(DAMAGE_LORE.formatted(centerDamage)).add(RADIUS_LORE.formatted(radius));
	}
	
	@Override
	public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
		
		Vector direction = player.getEyeLocation().getDirection();
		Location rocketLoc = player.getEyeLocation().add(direction);
		
		TFSound.STRIKER.play(rocketLoc);
		
		Egg egg = (Egg) player.toBukkit().getWorld().spawnEntity(rocketLoc, EntityType.EGG);
		egg.setVelocity(direction.clone().multiply(1.7));
		
		// for PlayerEggThrowEvent
		egg.setShooter(player.toBukkit());
		
		weapon.useAmmo();
		
		new ThrownExplosive(player, this, 100, true) {
			@Override
			public void tick() {}
			
			@Override
			public void explode() {
				GameManager.getInstance().explosion(player, egg.getLocation(), centerDamage, radius, player::isEnemy, 2);
				remove();
			}
			
			@Override
			public Entity getEntity() {
				return egg;
			}
		};
	}
	
	@Override
	public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {}
}
