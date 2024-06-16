package fr.lumin0u.teamfortress2.weapons.types;

import com.google.common.collect.ImmutableList.Builder;
import fr.lumin0u.teamfortress2.game.managers.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.TFSound;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import fr.lumin0u.teamfortress2.weapons.ThrownExplosive;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

public final class RocketLauncherType extends WeaponType
{
	private final List<Rocket> rockets = new ArrayList<>();
	private static final double ROCKET_SPEED = 1;
	
	
	private final double centerDamage = 17;
	private final double radius = 6;
	
	public RocketLauncherType() {
		super(false, Material.CROSSBOW, "Lance Roquette", 1, 144, -1);
	}
	
	@Override
	protected Builder<String> loreBuilder() {
		return super.loreBuilder().add(CUSTOM_LORE.formatted("Tire une roquette explosive")).add(RANGE_LORE.formatted(ROCKET_SPEED * 100)).add(DAMAGE_LORE.formatted(centerDamage)).add(RADIUS_LORE.formatted(radius));
	}
	
	@Override
	public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
		
		Vector direction = player.getEyeLocation().getDirection();
		Location rocketLoc = player.getEyeLocation().add(direction);
		
		TFSound.ROCKET_LAUNCHER.play(rocketLoc);
		
		SmallFireball fireball = (SmallFireball) player.toBukkit().getWorld().spawnEntity(rocketLoc, EntityType.SMALL_FIREBALL);
		fireball.setDirection(direction.clone().multiply(ROCKET_SPEED));
		fireball.setYield(0);
		fireball.setIsIncendiary(false);
		
		weapon.useAmmo();
		
		rockets.add(new Rocket(player, fireball, direction));
		
		/*Random rand = new Random();
		
		Location eyeLoc = player.getEyeLocation();
		
		double x = eyeLoc.getDirection().getX();
		double y = eyeLoc.getDirection().getY();
		double z = eyeLoc.getDirection().getZ();
		
		for(int i = 0; i < 50; i++) {
			eyeLoc.getWorld().spawnParticle(Particle.CLOUD, eyeLoc, 1, x+rand.nextDouble()-0.5, y+rand.nextDouble()-0.5, z+rand.nextDouble()-0.5, rand.nextDouble(), null, true);
		}*/
	}
	
	public List<Rocket> getRockets() {
		return Collections.unmodifiableList(rockets);
	}
	
	@Override
	public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {}
	
	public class Rocket extends ThrownExplosive
	{
		private final SmallFireball fireball;
		private final Vector direction;
		private Location location;
		
		public Rocket(TFPlayer owner, SmallFireball fireball, Vector direction) {
			super(owner, WeaponTypes.ROCKET_LAUNCHER, 100, false);
			this.fireball = fireball;
			this.direction = direction;
			this.location = fireball.getLocation();
		}
		
		@Override
		public void tick() {
			//fireball.setDirection(direction);
			
			Location nextLoc = location.clone().add(direction.clone().multiply(ROCKET_SPEED));
			BoundingBox bb = getEntity().getBoundingBox().expand(0.4);
			
			Optional<Entity> optHit = Stream.concat(
							nextLoc.getWorld().getNearbyEntitiesByType(Fireball.class, nextLoc, 5, 5, 5).stream(),
							nextLoc.getWorld().getNearbyEntitiesByType(Player.class, nextLoc, 5, 5, 5).stream())
					.filter(owner::isNot)
					.filter(not(fireball::equals))
					.filter(ent -> bb.overlaps(ent.getBoundingBox()))
					.findAny();
			
			if(location.getWorld().rayTraceBlocks(location, nextLoc.toVector().subtract(location.toVector()), ROCKET_SPEED, FluidCollisionMode.NEVER, true) != null
				|| optHit.isPresent()) {
				if(optHit.isPresent() && optHit.get() instanceof Fireball other) {
					rockets.stream()
							.filter(rocket -> rocket.getEntity().equals(other))
							.forEach(Rocket::explode);
				}
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
			final double centerKnockback = 2.5;
			
			GameManager.getInstance().explosion(owner, location, centerDamage, radius, owner::isEnemy, centerKnockback);
			
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
	}
}
