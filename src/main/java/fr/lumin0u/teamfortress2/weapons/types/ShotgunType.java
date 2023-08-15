package fr.lumin0u.teamfortress2.weapons.types;

import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.Gun;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.util.RayTraceResult;

public class ShotgunType extends GunType
{
	protected final int shots;
	
	public ShotgunType(boolean ultimate, Material material, String name, int maxAmmo, int reloadTicks, int actionDelay, double damage, double range, double accuracy, double knockback, int shots) {
		super(ultimate, material, name, maxAmmo, reloadTicks, actionDelay, damage, range, accuracy, knockback);
		
		this.shots = shots;
	}
	
	@Override
	public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
		for(int i = 0; i < shots; i++) {
			shoot(true, player, player.getEyeLocation(), player.getEyeLocation().getDirection(), (Gun<?>) weapon,
					l -> l.getWorld().spawnParticle(Particle.REDSTONE, l, 1, new DustOptions(Color.BLACK, 1)), GameManager.getInstance().getEntities());
		}
		
		weapon.useAmmo();
	}
}
