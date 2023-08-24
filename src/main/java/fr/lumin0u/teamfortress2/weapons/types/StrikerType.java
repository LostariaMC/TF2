package fr.lumin0u.teamfortress2.weapons.types;

import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.ThrownExplosive;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import fr.lumin0u.teamfortress2.weapons.types.RocketLauncherType.Rocket;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StrikerType extends WeaponType
{
	private final List<ThrownExplosive> projectiles = new ArrayList<>();
	
	public StrikerType() {
		super(true, Material.LEATHER_HORSE_ARMOR, "Striker", 4, -1, 10);
	}
	
	@Override
	public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
		
		Vector direction = player.getEyeLocation().getDirection();
		Location rocketLoc = player.getEyeLocation().add(direction);
		
		Egg egg = (Egg) player.toBukkit().getWorld().spawnEntity(rocketLoc, EntityType.EGG);
		egg.setVelocity(direction.clone().multiply(1.7));
		
		weapon.useAmmo();
		
		projectiles.add(new ThrownExplosive(player, this, 100, true) {
			@Override
			public void tick() {}
			
			@Override
			public void explode() {
				GameManager.getInstance().explosion(player, egg.getLocation(), 14, 8, player::isEnemy, 2);
				remove();
			}
			
			@Override
			public Entity getEntity() {
				return egg;
			}
		});
		
		/*Random rand = new Random();
		
		Location eyeLoc = player.getEyeLocation();
		
		double x = eyeLoc.getDirection().getX();
		double y = eyeLoc.getDirection().getY();
		double z = eyeLoc.getDirection().getZ();
		
		for(int i = 0; i < 50; i++) {
			eyeLoc.getWorld().spawnParticle(Particle.CLOUD, eyeLoc, 1, x+rand.nextDouble()-0.5, y+rand.nextDouble()-0.5, z+rand.nextDouble()-0.5, rand.nextDouble(), null, true);
		}*/
	}
	
	public List<ThrownExplosive> getProjectiles() {
		return Collections.unmodifiableList(projectiles);
	}
	
	@Override
	public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {}
	/*
	public static class Rocket extends ThrownExplosive
	{
		private final SmallFireball fireball;
		private final Vector direction;
		private Location location;
		
		public Rocket(TFPlayer owner, SmallFireball fireball, Vector direction) {
			super(owner, WeaponTypes.ROCKET_LAUNCHER, -1);
			this.fireball = fireball;
			this.direction = direction;
			this.location = fireball.getLocation();
		}
		
		@Override
		public void tick() {
			//fireball.setDirection(direction);
			
			Location nextLoc = location.clone().add(direction.clone().multiply(ROCKET_SPEED));
			
			if(location.getWorld().rayTraceBlocks(location, nextLoc.toVector().subtract(location.toVector()), ROCKET_SPEED) != null) {
				explode();
				return;
			}
			
			fireball.teleport(nextLoc);
			location = nextLoc;
		}
		
		@Override
		public Fireball getEntity() {
			return fireball;
		}
		
		@Override
		public void explode() {
			final double radius = 6;
			final double centerKnockback = 2.5;
			
			GameManager.getInstance().explosion(owner, location, 13, radius, owner::isEnemy, centerKnockback);
			
			double distance = owner.getLocation().distance(location);
			if(distance < radius) {
				Vector direction = owner.getLocation().toVector().add(new Vector(0, 1, 0)).subtract(location.toVector());
				double m = Math.min(0.9, (radius - distance) / radius);
				owner.toBukkit().setVelocity(owner.toBukkit().getVelocity()
						//.multiply(new Vector(1 + 2*m, 1, 1 + 2*m))
						.add(direction.normalize().multiply(m * centerKnockback)));
			}
			
			remove();
		}
	}*/
}
