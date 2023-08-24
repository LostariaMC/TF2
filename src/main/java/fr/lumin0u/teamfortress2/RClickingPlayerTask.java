package fr.lumin0u.teamfortress2;

import fr.lumin0u.teamfortress2.game.GameManager;
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
		//System.out.println("CLICK :pog: " + (TF.currentTick() - lastClickDate));
		lastClickDate = TF.currentTick();
	}
	
	@Override
	public void run() {
		
		//System.out.println(TF.currentTick() - lastClickDate);
		
		if(TF.currentTick() - lastClickDate <= 4) {
			if(GameManager.getInstance().getPhase().isInGame() && !player.isInSafeZone() && !player.isDead()) {
				//System.out.println("SEND CLICK");
				RayTraceResult rayTraceResult = new RayTraceResult(new Vector());
				
				player.getWeaponInHand().ifPresent(weapon -> weapon.rightClick(rayTraceResult));
			}
		}
	}
}
