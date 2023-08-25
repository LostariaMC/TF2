package fr.lumin0u.teamfortress2.weapons.types;

import com.google.common.collect.ImmutableList.Builder;
import fr.lumin0u.teamfortress2.FireDamageCause;
import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.TFEntity;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import fr.lumin0u.teamfortress2.weapons.types.GunType.Hit;
import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class FlareGunType extends WeaponType
{
	private final double range = 10; // blocks
	private final double flareSpeed = 0.7; // blocks/ticks
	private final double fireDmg = 2;
	private final int fireDuration = 80;
	
	public FlareGunType() {
		super(false, Material.REDSTONE_TORCH, "Flare gun", 1, 78, -1);
	}
	
	public double getRange() {
		return range;
	}
	
	@Override
	protected Builder<String> loreBuilder() {
		return super.loreBuilder()
				.add(RANGE_LORE.formatted(range))
				.add(CUSTOM_LORE.formatted("Enflamme vos ennemis"))
				.add(DURATION_LORE.formatted((float) ((float) fireDuration / 20f)))
				.add(FIRE_DMG_LORE.formatted(fireDmg));
	}
	
	@Override
	public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
		
		weapon.useAmmo();
		
		Collection<TFEntity> entities = GameManager.getInstance().getLivingEntities();
		Collection<TFEntity> gotLit = new ArrayList<>();
		
		final Location source = player.getEyeLocation();
		final Vector direction = player.getEyeLocation().getDirection();
		
		Location point = source.clone().add(direction.clone().multiply(0.5));
		
		Location wantedEndPoint = source.clone().add(direction.clone().multiply(range));
		
		World world = source.getWorld();
		
		RayTraceResult collisionResult = world.rayTraceBlocks(source, direction, range, FluidCollisionMode.ALWAYS, true);
		Location endPoint = collisionResult == null ? wantedEndPoint : collisionResult.getHitPosition().toLocation(world);
		double bulletTravelLength = endPoint.distance(source);
		
		final double particlePerBlock = 6; // blocks^-1
		
		new BukkitRunnable() {
			int tick = 0;
			
			@Override
			public void run() {
				while(point.distance(source) < flareSpeed * tick) {
					if(point.distanceSquared(source) > bulletTravelLength*bulletTravelLength) {
						cancel();
						return;
					}
					
					// particle {
					final int nb = 20;
					final double radius = 0.4;
					
					final double angleBtwnP = Math.PI * 2d / (double) nb;
					Vector normalizedDirection = direction.clone();
					for(int j = 0; j < nb; j++) {
						Vector x1 = new Vector(-normalizedDirection.getZ(), 0d, normalizedDirection.getX()).normalize();
						Vector x2 = normalizedDirection.clone().crossProduct(x1).normalize();
						
						Location l = point.clone().add(x1.clone().multiply(radius * Math.sin(j * angleBtwnP))).add(x2.clone().multiply(radius * Math.cos(j * angleBtwnP)));
						l.getWorld().spawnParticle(Particle.REDSTONE, l, 1, 0, 0, 0, 0, new DustOptions(Color.RED, 0.5f), true);
					}
					// }
					
					BoundingBox flareBB = BoundingBox.of(point.clone().add(-radius, -radius, -radius), point.clone().add(radius, radius, radius));
					for(TFEntity ent : entities) {
						if(!player.isEnemy(ent) || gotLit.contains(ent))
							continue;
						
						if(ent.getBodyBox().overlaps(flareBB) || ent.getHeadBox().overlaps(flareBB)) {
							ent.setFireCause(new FireDamageCause(false, player, fireDmg));
							ent.getEntity().setFireTicks(fireDuration);
							gotLit.add(ent);
						}
					}
					
					point.add(direction.clone().multiply(1 / particlePerBlock));
				}
				tick++;
			}
		}.runTaskTimer(TF.getInstance(), 1, 1);
	}
	
	@Override
	public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
	}
}
