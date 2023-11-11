package fr.lumin0u.teamfortress2.weapons;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.TFSound;
import fr.lumin0u.teamfortress2.weapons.types.WeaponTypes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;

public class Sniper extends Scopeable
{
	private final int CHARGE_TIME = 40;
	private final String CHARGE_S = "§5Charge §d%d%%";
	private final String CHARGED = "§eCharge §6100%";
	
	private int scopeTime;
	private BukkitTask scopeTask;
	private final BossBar bossBar = Bukkit.createBossBar(CHARGE_S.formatted(50), BarColor.PURPLE, BarStyle.SEGMENTED_12);
	private Location lastOwnerLocation;
	
	public Sniper(TFPlayer owner, int slot) {
		super(WeaponTypes.SNIPER, owner, slot);
	}
	
	@Override
	public void onScope() {
		TFSound.SNIPER_SCOPE.playTo(owner);
		owner.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, PotionEffect.INFINITE_DURATION, 10, false, false, false));
		
		bossBar.addPlayer(owner.toBukkit());
		
		scopeTask = new BukkitRunnable() {
			@Override
			public void run() {
				scopeTime++;
				
				if(lastOwnerLocation.distanceSquared(owner.getLocation()) > 0.1) {
					resetCharge();
					lastOwnerLocation = owner.getLocation();
				}
				
				if(scopeTime < CHARGE_TIME) {
					double charge = (double) scopeTime / (double) CHARGE_TIME * 0.5 + 0.5;
					bossBar.setProgress(charge);
					bossBar.setTitle(CHARGE_S.formatted((int) (charge * 100)));
				}
				else if(scopeTime == CHARGE_TIME) {
					bossBar.setTitle(CHARGED);
					bossBar.setProgress(1);
					bossBar.setStyle(BarStyle.SOLID);
					
					TFSound.SNIPER_CHARGE_FULL.playTo(owner);
				}
			}
		}.runTaskTimer(TF.getInstance(), 1, 1);
	}
	
	@Override
	public void onUnscope() {
		TFSound.SNIPER_UNSCOPE.playTo(owner);
		owner.toBukkit().removePotionEffect(PotionEffectType.SLOW);
		scopeTask.cancel();
		bossBar.removePlayer(owner.toBukkit());
		
		resetCharge();
	}
	
	public void resetCharge() {
		scopeTime = 0;
		
		bossBar.setProgress(0.5);
		bossBar.setTitle(CHARGE_S.formatted(50));
		bossBar.setStyle(BarStyle.SEGMENTED_12);
	}
	
	@Override
	public void remove() {
		super.remove();
		if(scopeTask != null)
			scopeTask.cancel();
	}
	
	/*public double chargeMultiplier() {
		return Math.min(2, (double) scopeTime / (double) CHARGE_TIME + 1);
	}*/
	
	public double getDamage(boolean headshot, double bulletTravel) {
		double charge = (double) scopeTime / (double) CHARGE_TIME + 1;
		return headshot ? 20 : scoping ? Math.min(20, charge * 10 + (bulletTravel / 30)) : 10;
	}
}
