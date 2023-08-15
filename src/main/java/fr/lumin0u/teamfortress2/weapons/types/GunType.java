package fr.lumin0u.teamfortress2.weapons.types;

import fr.lumin0u.teamfortress2.TFEntity;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.Gun;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Random;
import java.util.function.Consumer;

public class GunType extends WeaponType
{
	protected final double damage;
	protected final double range; // blocks
	protected final double inaccuracy; // std variation in radians
	protected final double knockback; // blocks/tick
	
	public GunType(boolean ultimate, Material material, String name, int maxAmmo, int reloadTicks, int actionDelay, double damage, double range, double inaccuracy, double knockback) {
		super(ultimate, material, name, maxAmmo, reloadTicks, actionDelay);
		this.damage = damage;
		this.range = range;
		this.inaccuracy = inaccuracy;
		this.knockback = knockback;
	}
	
	@Override
	public Weapon createWeapon(TFPlayer owner, int slot) {
		return new Gun(this, owner, slot);
	}
	
	@Override
	public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
		shoot(true, player, player.getEyeLocation(), player.getEyeLocation().getDirection(), (Gun) weapon,
				l -> l.getWorld().spawnParticle(Particle.REDSTONE, l, 1, new DustOptions(Color.BLACK, 1)), GameManager.getInstance().getEntities());
		
		weapon.useAmmo();
	}
	
	@Override
	public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
	
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
		Vector kb = hit.direction.clone().setY(0).multiply(hit.weapon.getType().knockback);
		
		hit.hitEntity.damage(hit.player, hit.weapon.getType().damage, kb);
	}
	
	public static void shoot(boolean flame, TFPlayer player, Location source, Vector direction, Gun weapon, Consumer<Location> particle, Collection<? extends TFEntity> entities)
	{
		/*if(this instanceof LaTornade)
		{
			p.setVelocity(p.getVelocity().subtract(p.getEyeLocation().getDirection().multiply(0.05)));
			
			double j = tfp.getHeavyBulletNb();
			double multi = 0.15;
			
			Vector x1 = new Vector(-p.getLocation().getDirection().normalize().getZ(), 0d, p.getLocation().getDirection().normalize().getX()).normalize();
			Vector x2 = p.getLocation().getDirection().normalize().crossProduct(x1).normalize();
			source.add(x1.clone().multiply(multi * Math.sin(j / 10 * Math.PI * 2d))).add(x2.clone().multiply(multi * Math.cos(j / 10 * Math.PI * 2d)));
			
			tfp.setHeavyBulletNb(tfp.getHeavyBulletNb() + 1);
			
			
			range * (player.isEnergized() ? 20 : 10)
		}*/
		
		direction.normalize();
		direction = addSpread(direction, weapon.getType().inaccuracy);
		
		double range = weapon.getType().range;
		
		Location wantedEndPoint = source.clone().add(direction.clone().multiply(range));
		
		World world = source.getWorld();
		
		RayTraceResult collisionResult = world.rayTraceBlocks(source, direction, range);
		Location endPoint = collisionResult == null ? wantedEndPoint : collisionResult.getHitPosition().toLocation(world);
		
		TFEntity hitEntity = null;
		Vector hitPosition = null;
		boolean headshot = false;
		
		for(TFEntity ent : entities)
		{
			if(!player.canDamage(ent))
				continue;
			
			BoundingBox bodyBox = ent.getBodyBox();
			BoundingBox headBox = ent.getHeadBox();
			
			RayTraceResult bodyCollision = bodyBox.rayTrace(source.toVector(), direction, range);
			RayTraceResult headCollision = headBox.rayTrace(source.toVector(), direction, range);
			
			Vector headHitPoint = headCollision != null ? headCollision.getHitPosition() : null;
			Vector bodyHitPoint = bodyCollision != null ? bodyCollision.getHitPosition() : null;
			
			Vector hitPositionHere;
			boolean headshotHere;
			
			if(bodyHitPoint == null) {
				hitPositionHere = headHitPoint;
				headshotHere = true;
			}
			else if(headHitPoint == null) {
				hitPositionHere = bodyHitPoint;
				headshotHere = false;
			}
			else if(bodyHitPoint.distance(source.toVector()) < headHitPoint.distance(source.toVector())) {
				hitPositionHere = bodyHitPoint;
				headshotHere = false;
			}
			else {
				hitPositionHere = headHitPoint;
				headshotHere = true;
			}
			
			if(hitPositionHere != null && (hitPosition == null || hitPositionHere.distanceSquared(source.toVector()) < hitPosition.distanceSquared(source.toVector()))) {
				hitEntity = ent;
				hitPosition = hitPositionHere;
				headshot = headshotHere;
			}
		}
		
		if(hitEntity != null)
		{
			endPoint = hitPosition.toLocation(world);
			
			weapon.getType().onEntityHit(new Hit(player, weapon, hitEntity, endPoint, headshot, direction));
			
			for(int i = 0; i < 10; i++)
				world.spawnParticle(Particle.BLOCK_CRACK, endPoint, 1, Material.REDSTONE_BLOCK.createBlockData());
			
			player.toBukkit().playSound(player.toBukkit().getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 1.0f);
			
		/*	boolean damageDone = false;
			
			if(this instanceof Revolver && headshot)// headshot revolver
			{
				damageDone = TF.getInstance().damageTF(ent, tfp, damage + 2, kb);
			}
			
			else if(this instanceof Sniper && headshot)// headshot sniper
			{
				damageDone = TF.getInstance().damageTF(ent, tfp, damage * 1.5, kb);
			}
			
			else if((tfp.isLooking() || p.getName().equals("lylyssou1")) && this instanceof Sniper)// looking sniper
			{
				damageDone = TF.getInstance().damageTF(ent, tfp, damage * 2, kb);
			}
			
			else
			{
				damageDone = TF.getInstance().damageTF(ent, tfp, damage, kb);
			}
			
			if(damageDone)
			{
				
				
				break;
			}*/
		}
		
		for(int counter = 0; counter < source.distance(endPoint) * 5; counter++)
		{
			Location point = source.clone().add(direction.clone().multiply(counter / 5));
			
			if(counter == 5 && flame)
				world.spawnParticle(Particle.FLAME, point, 1);
			
			else if(counter > 5)
			{
				particle.accept(point);
			}
		}
		
		if(collisionResult != null && hitEntity == null)
		{
			Block collided = collisionResult.getHitBlock();
			
			if(collided != null && !collided.isLiquid())
				for(int i = 0; i < 20; i++)
					world.spawnParticle(Particle.BLOCK_CRACK, endPoint, 1, collided.getBlockData());
		}
		
		/*for(TFEntity ent : nearestPoint.keySet())
			if(!alreadyHit.contains(ent) && !ent.equals(tfp) && ent instanceof TFPlayer)
				Utils.playSound(((TFPlayer) ent).getPlayer(), nearestPoint.get(ent), "guns.FIEEW", 5, (float) Math.random() * 0.2F + 0.9F);*/
	}
	
	public static record Hit(TFPlayer player, Gun weapon, TFEntity hitEntity, Location hitPoint, boolean headshot, Vector direction) {}
}
