package fr.lumin0u.teamfortress2;

import fr.lumin0u.teamfortress2.game.managers.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class RClickingPlayerTask extends BukkitRunnable
{
	private final TFPlayer player;
	private long lastClickDate;
	
	public RClickingPlayerTask(TFPlayer player) {
		this.player = player;
	}
	
	public void start() {
		runTaskTimer(TF.getInstance(), 1, 1);
	}
	
	public void triggerRClick() {
		lastClickDate = TF.currentTick();
	}
	
	public void resetClick() {
		lastClickDate = 0;
	}
	
	@Override
	public void run() {
		
		if(TF.currentTick() - lastClickDate <= 4) {
			if(GameManager.getInstance().getPhase().isInGame() && !player.isInSafeZone() && !player.isDead()) {
				RayTraceResult rayTraceResult = new RayTraceResult(new Vector());
				
				player.getWeaponInHand().ifPresent(weapon -> weapon.rightClick(rayTraceResult));
			}
		}
	}
}
