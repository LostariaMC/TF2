package fr.lumin0u.teamfortress2.weapons;

import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.types.WeaponType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;

public abstract class Scopeable extends Weapon
{
	protected boolean scoping;
	
	public Scopeable(WeaponType type, TFPlayer owner, int slot) {
		super(type, owner, slot);
	}
	
	public void setScoping(boolean scoping) {
		if(this.scoping == scoping) {
			return;
		}
		this.scoping = scoping;
		
		if(scoping) {
			scopeEffect();
		}
		else {
			unscopeEffect();
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
	
	public boolean isScoping() {
		return scoping;
	}
	
	@Override
	public void leftClick(RayTraceResult info) {
		super.leftClick(info);
		invertScope();
	}
	
	public abstract void scopeEffect();
	
	public abstract void unscopeEffect();
}
