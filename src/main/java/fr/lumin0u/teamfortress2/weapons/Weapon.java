package fr.lumin0u.teamfortress2.weapons;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.TFSound;
import fr.lumin0u.teamfortress2.weapons.types.WeaponType;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.List;

public class Weapon
{
	public static final int ULTIMATE_RELOAD_TICKS = 25 * 20;
	public static final int ULTIMATE_RELOAD_KILL_SPEEDUP_TICKS = 6 * 20;
	public static final int ULTIMATE_WEAPON_SLOT = 7;
	
	protected List<BukkitTask> bukkitTasks = new ArrayList<>();
	
	protected final WeaponType type;
	protected final int slot;
	
	protected int ammo;
	protected boolean reloading;
	protected TFPlayer owner;
	protected long lastActionDate;
	
	/**
	 * for ultimate weapons
	 * */
	protected boolean unlocked;
	protected int ultiReloadTicksRem;
	
	public Weapon(WeaponType type, TFPlayer owner, int slot) {
		this.type = type;
		this.ammo = type.getMaxAmmo();
		this.owner = owner;
		this.slot = slot;
		this.unlocked = false;
	}
	
	public WeaponType getType() {
		return type;
	}
	
	public final void giveItem() {
		ItemStack item = type.buildItem(this).build();
		if(item.getAmount() > 0)
			owner.toBukkit().getInventory().setItem(slot, item);
		else if(type.isItem(owner.toBukkit().getInventory().getItem(slot)))
			owner.toBukkit().getInventory().setItem(slot, null);
	}
	
	public void updateItem() {
		if(owner.isOnline()) {
			giveItem();
		}
	}
	
	public void rightClick(RayTraceResult info) {
		if(!reloading && ammo > 0 && lastActionDate + getType().getActionDelay() < TF.currentTick()) {
			type.rightClickAction(owner, this, info);
			lastActionDate = TF.currentTick();
		}
	}
	
	public void leftClick(RayTraceResult info) {
		if(!reloading && ammo > 0)
			type.leftClickAction(owner, this, info);
	}
	
	/**
	 * should be called when main action is triggered (for most weapons)
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
				reload(type.getReloadTicks());
			}
		}
	}
	
	public void reload(int ticks) {
		if(getType().isUltimate())
			return;
		
		if(ticks <= 0) {
			ammo = type.getMaxAmmo();
			updateItem();
			return;
		}
		
		reloading = true;
		
		updateItem();
		owner.toBukkit().setCooldown(owner.toBukkit().getInventory().getItem(slot).getType(), ticks);
		
		bukkitTasks.add(Bukkit.getScheduler().runTaskLater(TF.getInstance(), () -> {
			if(!owner.hasWeapon(Weapon.this)) {
				return;
			}
			
			ammo = type.getMaxAmmo();
			reloading = false;
			updateItem();
		}, ticks));
	}
	
	protected void ultimateReload() {
		owner.toBukkit().setCooldown(owner.toBukkit().getInventory().getItem(slot).getType(), ULTIMATE_RELOAD_TICKS);
		reloading = true;
		
		ultiReloadTicksRem = ULTIMATE_RELOAD_TICKS;
		
		bukkitTasks.add(new BukkitRunnable() {
			@Override
			public void run() {
				if(!owner.hasWeapon(Weapon.this)) {
					cancel();
					return;
				}
				
				ultiReloadTicksRem--;
				
				if(ultiReloadTicksRem <= 0) {
					ammo = type.getMaxAmmo();
					reloading = false;
					updateItem();
					TFSound.ULTI_READY.playTo(owner);
					
					cancel();
				}
			}
		}.runTaskTimer(TF.getInstance(), 1, 1));
	}
	
	/**
	 * should only be called if this is the ultimate weapon of the owner
	 * */
	public void onOwnerDoKill() {
		if(type.isUltimate()) {
			if(!unlocked) {
				unlocked = true;
				giveItem();
				ultimateReload();
				ultiReloadTicksRem -= ULTIMATE_RELOAD_KILL_SPEEDUP_TICKS;
			}
			else if(reloading) {
				ultiReloadTicksRem -= ULTIMATE_RELOAD_KILL_SPEEDUP_TICKS;
			}
			owner.toBukkit().setCooldown(owner.toBukkit().getInventory().getItem(slot).getType(), Math.max(0, ultiReloadTicksRem));
		}
	}
	
	public void fullyUnlockUltimate() {
		if(type.isUltimate()) {
			unlocked = true;
			giveItem();
			ultimateReload();
			ultiReloadTicksRem = 1;
			owner.toBukkit().setCooldown(owner.toBukkit().getInventory().getItem(slot).getType(), 0);
		}
	}
	
	public int getAmmo() {
		return ammo;
	}
	
	public boolean isReloading() {
		return reloading;
	}
	
	public int getSlot() {
		return slot;
	}
	
	public void remove() {
		bukkitTasks.forEach(BukkitTask::cancel);
	}
	
	public void addBukkitTask(BukkitTask task) {
		bukkitTasks.add(task);
	}
	
	public boolean isUnlocked() {
		return unlocked;
	}
}
