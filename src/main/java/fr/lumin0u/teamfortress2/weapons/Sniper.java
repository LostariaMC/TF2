package fr.lumin0u.teamfortress2.weapons;

import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.types.GunType;
import fr.lumin0u.teamfortress2.weapons.types.WeaponTypes;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class Sniper extends Scopeable {
	
	public Sniper(TFPlayer owner, int slot) {
		super(WeaponTypes.SNIPER, owner, slot);
	}
	
	@Override
	public void leftClick(RayTraceResult info) {
		type.leftClickAction(owner, this, info);
	}
	
	@Override
	public void scopeEffect() {
		owner.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, PotionEffect.INFINITE_DURATION, 10, false, false, false));
	}
	
	@Override
	public void unscopeEffect() {
		owner.toBukkit().removePotionEffect(PotionEffectType.SLOW);
	}
	
	public static class SniperType extends GunType {
		
		public SniperType() {
			super(false, Material.DIAMOND_HOE, "Sniper", 1, 76, -1, 10, 100, 0, 0.02);
		}
		
		@Override
		public Weapon createWeapon(TFPlayer owner, int slot) {
			return new Sniper(owner, slot);
		}
		
		@Override
		public void onEntityHit(Hit hit) {
			
			boolean scoping = ((Sniper)hit.weapon()).scoping;
			
			Vector kb = hit.direction().clone().setY(0).multiply(hit.gunType().getKnockback());
			
			hit.hitEntity().damage(hit.player(), hit.gunType().getDamage() * (hit.headshot() ? 1.5 : scoping ? 2 : 1), kb);
		}
	}
}
