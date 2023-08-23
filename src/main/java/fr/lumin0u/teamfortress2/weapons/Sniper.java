package fr.lumin0u.teamfortress2.weapons;

import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.types.GunType;
import fr.lumin0u.teamfortress2.weapons.types.WeaponTypes;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class Sniper extends Gun {
	
	private boolean scoping;
	
	public Sniper(TFPlayer owner, int slot) {
		super(WeaponTypes.SNIPER, owner, slot);
	}
	
	public void setScoping(boolean scoping) {
		if(this.scoping == scoping) {
			return;
		}
		this.scoping = scoping;
		
		if(scoping) {
			owner.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, PotionEffect.INFINITE_DURATION, 10, false, false, false));
		}
		else {
			owner.toBukkit().removePotionEffect(PotionEffectType.SLOW);
		}
	}
	
	public void invertScope() {
		setScoping(!scoping);
	}
	
	@Override
	public void remove() {
		super.remove();
		setScoping(false);
	}
	
	@Override
	public void leftClick(RayTraceResult info) {
		type.leftClickAction(owner, this, info);
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
			
			Vector kb = hit.direction().clone().setY(0).multiply(hit.weapon().getType().getKnockback());
			
			hit.hitEntity().damage(hit.player(), hit.weapon().getType().getDamage() * (hit.headshot() ? 1.5 : scoping ? 2 : 1), kb);
		}
		
		@Override
		public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
			super.leftClickAction(player, weapon, info);
			((Sniper)weapon).invertScope();
		}
	}
}
