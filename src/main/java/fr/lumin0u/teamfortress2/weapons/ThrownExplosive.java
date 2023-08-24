package fr.lumin0u.teamfortress2.weapons;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.types.WeaponType;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class ThrownExplosive
{
	private static final List<ThrownExplosive> livingInstances = new ArrayList<>();
	
	protected final TFPlayer owner;
	protected final WeaponType sourceType;
	protected final int maxTicks;
	protected final boolean explodeOnProjectileCollide;
	
	protected boolean removed;
	protected int ticksLived;
	
	public ThrownExplosive(TFPlayer owner, WeaponType sourceType, int maxTicks, boolean explodeOnProjectileCollide) {
		this.owner = owner;
		this.sourceType = sourceType;
		this.maxTicks = maxTicks;
		this.explodeOnProjectileCollide = explodeOnProjectileCollide;
		
		startLiving();
	}
	
	public static List<ThrownExplosive> getLivingInstances() {
		return Collections.unmodifiableList(livingInstances);
	}
	
	private void startLiving() {
		livingInstances.add(this);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				if(ticksLived++ >= maxTicks) {
					explode();
					cancel();
					return;
				}
				
				if(removed) {
					cancel();
					return;
				}
				
				tick();
			}
		}.runTaskTimer(TF.getInstance(), 1, 1);
	}
	
	public TFPlayer getOwner() {
		return owner;
	}
	
	public WeaponType getSourceType() {
		return sourceType;
	}
	
	public boolean isExplodeOnProjectileCollide() {
		return explodeOnProjectileCollide;
	}
	
	public abstract void tick();
	
	/**
	 * any implementation may call #remove() in #explode()
	 * */
	public abstract void explode();
	
	public abstract Entity getEntity();
	
	public boolean isRemoved() {
		return removed;
	}
	
	public void remove() {
		this.removed = true;
		livingInstances.remove(this);
		Optional.ofNullable(getEntity()).ifPresent(Entity::remove);
	}
}
