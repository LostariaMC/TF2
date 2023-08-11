package fr.lumin0u.teamfortress2;

import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.game.TFTeam;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public interface TFEntity
{
	public abstract Location getLocation();
	
	public default Location getEyeLocation() {
		return getLocation().clone().add(0, 0.62, 0);
	}
	
	public default BoundingBox getBodyBox() {
		return BoundingBox.of(getLocation().clone().add(-0.3, 0, -0.3), getLocation().clone().add(0.3, 1.4, 0.3));
	}
	
	public default BoundingBox getHeadBox() {
		return BoundingBox.of(getLocation().clone().add(-0.25, 1.4, -0.25), getLocation().clone().add(0.25, 1.7, 0.25));
	}
	
	public LivingEntity getEntity();
	
	public TFTeam getTeam();
	
	public boolean isDead();
	
	public FireCause getFireCause();
	
	public void setFireCause(FireCause fireCause);
	
	public TFPlayer getPoisonSource();
	
	public void setPoisonSource(TFPlayer poisonSource);
	
	public void damage(TFPlayer damager, double amount, Vector knockback);
}
