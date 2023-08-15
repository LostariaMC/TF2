package fr.lumin0u.teamfortress2.weapons;

import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.types.GunType;
import fr.lumin0u.teamfortress2.weapons.types.WeaponType;

public class Gun extends Weapon
{
	public Gun(GunType type, TFPlayer owner, int slot) {
		super(type, owner, slot);
	}

	@Override
	public GunType getType() {
		return (GunType) super.getType();
	}
}
