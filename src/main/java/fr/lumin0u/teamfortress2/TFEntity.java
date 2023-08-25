package fr.lumin0u.teamfortress2;

import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.game.TFTeam;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public interface TFEntity
{
	public abstract Location getLocation();
	
	public default Location getEyeLocation() {
		return getLocation().clone().add(0, 1.62, 0);
	}
	
	public default BoundingBox getBodyBox() {
		return BoundingBox.of(getLocation().clone().add(-0.35, 0, -0.35), getLocation().clone().add(0.35, 1.4, 0.35));
	}
	
	public default BoundingBox getHeadBox() {
		return BoundingBox.of(getLocation().clone().add(-0.3, 1.4, -0.3), getLocation().clone().add(0.3, 1.9, 0.3));
	}
	
	public LivingEntity getEntity();
	
	public TFTeam getTeam();
	
	public boolean isDead();
	
	@NotNull
	public FireDamageCause getFireCause();
	
	public void setFireCause(FireDamageCause fireCause);
	
	public TFPlayer getPoisonSource();
	
	public void setPoisonSource(TFPlayer poisonSource);
	
	public boolean damage(TFPlayer damager, double amount, Vector knockback);
}
