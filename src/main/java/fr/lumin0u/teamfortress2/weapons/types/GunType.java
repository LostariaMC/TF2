package fr.lumin0u.teamfortress2.weapons.types;

import com.google.common.collect.ImmutableList.Builder;
import fr.lumin0u.teamfortress2.TFEntity;
import fr.lumin0u.teamfortress2.game.managers.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.TFSound;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import fr.lumin0u.teamfortress2.weapons.types.InvisWatchType.InvisWatch;
import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

public class GunType extends WeaponType
{
	protected final double damage;
	protected final double range; // blocks
	protected final double inaccuracy; // std variation in radians
	protected final double knockback; // blocks/tick
	protected final boolean perforant; // does the "bullet" goes through entities ?
	protected final TFSound shotSound;
	
	public GunType(boolean ultimate, Material material, String name, int maxAmmo, int reloadTicks, int actionDelay, double damage, double range, double inaccuracy, double knockback) {
		this(ultimate, material, name, maxAmmo, reloadTicks, actionDelay, damage, range, inaccuracy, knockback, TFSound.GUN_SHOT, false);
	}
	
	public GunType(boolean ultimate, Material material, String name, int maxAmmo, int reloadTicks, int actionDelay, double damage, double range, double inaccuracy, double knockback, TFSound sound, boolean perforant) {
		super(ultimate, material, name, maxAmmo, reloadTicks, actionDelay);
		this.damage = damage;
		this.range = range;
		this.inaccuracy = inaccuracy;
		this.knockback = knockback;
		this.perforant = perforant;
		this.shotSound = sound;
	}
	
	public double getDamage() {
		return damage;
	}
	
	public double getRange() {
		return range;
	}
	
	public double getInaccuracy() {
		return inaccuracy;
	}
	
	public double getKnockback() {
		return knockback;
	}
	
	public boolean isPerforant() {
		return perforant;
	}
	
	@Override
	public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
		shotSound.play(player.getLocation());
		
		shoot(player, player.getEyeLocation(), player.getEyeLocation().getDirection(), weapon, inaccuracy, this::particle, GameManager.getInstance().getLivingEntities());
		weapon.useAmmo();
		
		player.getOptWeapon(WeaponTypes.INVIS_WATCH).ifPresent(w -> ((InvisWatch)w).setInvisibilityCancelled(true));
	}
	
	@Override
	public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
	}
	
	@Override
	protected Builder<String> loreBuilder() {
		return super.loreBuilder().add(DAMAGE_LORE.formatted(damage)).add(RANGE_LORE.formatted(range));
	}
	
	private static Vector addSpread(Vector directionNormalized, double accuracy) {
		double a = directionNormalized.getX(), b = directionNormalized.getY(), c = directionNormalized.getZ();
		Vector perpendicular;
		if(c == 1)
			perpendicular = new Vector(1, 0, 0);
		else
			perpendicular = new Vector(-b, a, 0).normalize();
		
		double alpha = Math.random() * 2 * Math.PI;
		double beta = new Random().nextGaussian(0, accuracy);
		
		Vector spreaded = directionNormalized.clone().rotateAroundAxis(perpendicular, beta);
		
		return spreaded.rotateAroundAxis(directionNormalized, alpha);
	}
	
	public void onEntityHit(Hit hit) {
		Vector kb = hit.direction.clone().setY(0).multiply(hit.gunType().knockback);
		
		hit.hitEntity.damage(hit.player, hit.gunType().damage, kb);
	}
	
	public void particle(Location l, int i) {
		if(i == 0)
			l.getWorld().spawnParticle(Particle.SMALL_FLAME, l, 1, 0, 0.01, 0, 0.000001D, null, true);
		else
			l.getWorld().spawnParticle(Particle.REDSTONE, l, 1, 0, 0, 0, 0, new DustOptions(Color.BLACK, 0.5f), true);
	}
	
	public static void shoot(TFPlayer player, Location source, Vector direction, Weapon weapon, double inaccuracy, BiConsumer<Location, Integer> particle, Collection<? extends TFEntity> entities) {
		
		GunType type = (GunType) weapon.getType();
		
		direction.normalize();
		direction = addSpread(direction, inaccuracy);
		
		double range = type.range * (player.isEnergized() ? 1.5 : 1);
		
		Location wantedEndPoint = source.clone().add(direction.clone().multiply(range));
		
		World world = source.getWorld();
		
		RayTraceResult collisionResult = world.rayTraceBlocks(source, direction, range, FluidCollisionMode.NEVER, true);
		Location endPoint = collisionResult == null ? wantedEndPoint : collisionResult.getHitPosition().toLocation(world);
		double bulletTravelLength = endPoint.distance(source);
		
		//perforant
		List<Hit> entityHits = new ArrayList<>();
		
		for(TFEntity ent : entities) {
			if(!player.isEnemy(ent))
				continue;
			
			BoundingBox bodyBox = ent.getBodyBox().expand(0.2);
			BoundingBox headBox = ent.getHeadBox().expand(0.2);
			
			RayTraceResult bodyCollision = bodyBox.rayTrace(source.toVector(), direction, bulletTravelLength);
			RayTraceResult headCollision = headBox.rayTrace(source.toVector(), direction, bulletTravelLength);
			
			Vector headHitPoint = headCollision != null ? headCollision.getHitPosition() : null;
			Vector bodyHitPoint = bodyCollision != null ? bodyCollision.getHitPosition() : null;
			
			Vector hitPosition;
			boolean headshot;
			
			if(bodyHitPoint == null) {
				hitPosition = headHitPoint;
				headshot = true;
			}
			else if(headHitPoint == null) {
				hitPosition = bodyHitPoint;
				headshot = false;
			}
			else if(bodyHitPoint.distance(source.toVector()) < headHitPoint.distance(source.toVector())) {
				hitPosition = bodyHitPoint;
				headshot = false;
			}
			else {
				hitPosition = headHitPoint;
				headshot = true;
			}
			
			if(hitPosition != null) {
				if(type.perforant || entityHits.isEmpty() || entityHits.get(0).hitPoint().distanceSquared(source) < hitPosition.distanceSquared(source.toVector())) {
					entityHits.add(new Hit(player, weapon, type, ent, hitPosition.toLocation(world), headshot, direction));
				}
			}
		}
		
		for(Hit hit : entityHits) {
			if(!type.perforant)
				endPoint = hit.hitPoint.toLocation(world);
			
			type.onEntityHit(hit);
			
			for(int i = 0; i < 10; i++)
				world.spawnParticle(Particle.BLOCK_CRACK, endPoint, 1, 0, 0, 0, 0, Material.REDSTONE_BLOCK.createBlockData(), true);
			
			player.toBukkit().playSound(player.toBukkit().getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 1.0f);
		}
		
		if(collisionResult != null && (entityHits.isEmpty() || type.perforant)) {
			Block collided = collisionResult.getHitBlock();
			
			if(collided != null)
				//for(int i = 0; i < 10; i++)
				world.spawnParticle(Particle.BLOCK_CRACK, endPoint, 10, 0, 0, 0, 0, collided.getBlockData(), true);
		}
		
		final double particlePerBlock = 10;
		int i = 0;
		for(int counter = 0; counter < source.distance(endPoint) * particlePerBlock; counter++) {
			Location point = source.clone().add(direction.clone().multiply(counter / particlePerBlock));
			
			if(counter > particlePerBlock) {
				particle.accept(point, i++);
			}
		}
		
		/*for(TFEntity ent : nearestPoint.keySet())
			if(!alreadyHit.contains(ent) && !ent.equals(tfp) && ent instanceof TFPlayer)
				Utils.playSound(((TFPlayer) ent).getPlayer(), nearestPoint.get(ent), "guns.FIEEW", 5, (float) Math.random() * 0.2F + 0.9F);*/
	}
	
	public static record Hit(TFPlayer player, Weapon weapon, GunType gunType, TFEntity hitEntity, Location hitPoint, boolean headshot, Vector direction) {}
}
