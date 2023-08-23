package fr.lumin0u.teamfortress2.weapons;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.types.WeaponType;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class ThrownExplosive
{
	protected final TFPlayer owner;
	protected final WeaponType sourceType;
	protected final int maxTicks;
	
	protected boolean removed;
	protected int ticksLived;
	
	public ThrownExplosive(TFPlayer owner, WeaponType sourceType, int maxTicks) {
		this.owner = owner;
		this.sourceType = sourceType;
		this.maxTicks = maxTicks;
		
		startLiving();
	}
	
	private void startLiving() {
		new BukkitRunnable() {
			@Override
			public void run() {
				if(maxTicks != -1 && ticksLived++ >= maxTicks) {
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
		getEntity().remove();
	}
}
