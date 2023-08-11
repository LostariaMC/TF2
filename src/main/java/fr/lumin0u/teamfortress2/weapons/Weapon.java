package fr.lumin0u.teamfortress2.weapons;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.types.WeaponType;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class Weapon<T extends WeaponType>
{
	public static final int ULTIMATE_RELOAD_TICKS = 25 * 20;
	public static final int ULTIMATE_RELOAD_KILL_SPEEDUP_TICKS = 5 * 20;
	
	protected final T type;
	protected final int slot;
	
	protected int ammo;
	protected boolean reloading;
	protected TFPlayer owner;
	protected int lastActionDate;
	
	/**
	 * for ultimate weapons
	 * */
	protected boolean unlocked;
	protected int ultiReloadTicksRem;
	
	public Weapon(T type, TFPlayer owner, int slot) {
		this.type = type;
		this.ammo = type.getMaxAmmo();
		this.owner = owner;
		this.slot = slot;
		this.unlocked = false;
	}
	
	public T getType() {
		return type;
	}
	
	public final void giveItem() {
		owner.toBukkit().getInventory().setItem(slot, type.buildItem(this).build());
	}
	
	public void updateItem() {
		if(owner.isOnline()) {
			giveItem();
		}
	}
	
	/**
	 * should be called when main action is triggered, except for melee weapon (except for kukri)
	 * */
	public void useAmmo() {
		
		ammo--;
		
		if(ammo > 0) {
			updateItem();
		}
		else {
			if(type.isUltimate()) {
				ultimateReload();
			}
			else {
				reload();
			}
		}
	}
	
	protected void reload() {
		if(type.getReloadTicks() <= 0) {
			ammo = type.getMaxAmmo();
			updateItem();
			return;
		}
		
		owner.toBukkit().setCooldown(owner.toBukkit().getInventory().getItem(slot).getType(), type.getReloadTicks());
		reloading = true;
		
		Bukkit.getScheduler().runTaskLater(TF.getInstance(), () -> {
			ammo = type.getMaxAmmo();
			reloading = false;
			updateItem();
		}, type.getReloadTicks());
	}
	
	protected void ultimateReload() {
		owner.toBukkit().setCooldown(owner.toBukkit().getInventory().getItem(slot).getType(), ULTIMATE_RELOAD_TICKS);
		reloading = true;
		
		ultiReloadTicksRem = ULTIMATE_RELOAD_TICKS;
		
		new BukkitRunnable() {
			@Override
			public void run() {
				ultiReloadTicksRem--;
				
				if(ultiReloadTicksRem == 0) {
					ammo = type.getMaxAmmo();
					reloading = false;
					updateItem();
					
					cancel();
				}
			}
		}.runTaskTimer(TF.getInstance(), 1, 1);
	}
	
	/**
	 * should only be called if this is the ultimate weapon of the owner
	 * */
	public void onOwnerDoKill() {
		if(type.isUltimate()) {
			if(!unlocked) {
				giveItem();
				ultimateReload();
			}
			else if(!reloading) {
				ultimateReload();
			}
			else {
				ultiReloadTicksRem -= ULTIMATE_RELOAD_KILL_SPEEDUP_TICKS;
				owner.toBukkit().setCooldown(owner.toBukkit().getInventory().getItem(slot).getType(), ultiReloadTicksRem);
			}
		}
	}
	
	public int getAmmo() {
		return ammo;
	}
	
	public boolean isReloading() {
		return reloading;
	}
}
