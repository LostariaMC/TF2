package fr.lumin0u.teamfortress2.weapons.types;

import com.google.common.collect.ImmutableList.Builder;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Random;

public class ShotgunType extends GunType
{
	public static final boolean RANDOM_SPRAY = true;
	
	protected final int shots;
	
	public ShotgunType(boolean ultimate, Material material, String name, int maxAmmo, int reloadTicks, int actionDelay, double damage, double range, double accuracy, double knockback, int shots) {
		super(ultimate, material, name, maxAmmo, reloadTicks, actionDelay, damage, range, accuracy, knockback);
		
		this.shots = shots;
	}
	
	@Override
	public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
		
		if(RANDOM_SPRAY) {
			for(int i = 0; i < shots; i++) {
				shoot(player, player.getEyeLocation(), player.getEyeLocation().getDirection(), weapon, inaccuracy, this::particle, GameManager.getInstance().getLivingEntities());
			}
		}
		else {
			// TODO
		}
		
		weapon.useAmmo();
	}
	
	@Override
	protected Builder<String> loreBuilder() {
		return super.loreBuilder().add(NBSHOTS_LORE.formatted(shots));
	}
}
