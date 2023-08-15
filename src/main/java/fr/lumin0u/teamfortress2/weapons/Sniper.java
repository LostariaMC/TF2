package fr.lumin0u.teamfortress2.weapons;

import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.types.GunType;
import org.bukkit.Material;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class Sniper {
	
	public static class SniperType extends GunType {
		
		public boolean scoping;
		
		public SniperType(boolean ultimate, Material material, String name, int maxAmmo, int reloadTicks, int actionDelay, double damage, double range, double inaccuracy, double knockback) {
			super(false, Material.STICK, "Sniper", 1, 1000, -1, 1000, 100, 0, 0.02);
		}
		
		@Override
		public void onEntityHit(Hit hit) {
			Vector kb = hit.direction().clone().setY(0).multiply(hit.weapon().getType().knockback);
			
			hit.hitEntity().damage(hit.player(), hit.weapon().getType().damage, kb);
		}
		
		@Override
		public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
			super.leftClickAction(player, weapon, info);
			scoping = !scoping;
			// TODO SLOWNESS
		}
		
		public void unscope() {
			scoping = false;
	}
}
}
