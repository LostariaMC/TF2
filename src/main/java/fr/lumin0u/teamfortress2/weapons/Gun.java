package fr.lumin0u.teamfortress2.weapons;

import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.types.GunType;

public class Gun<T extends GunType> extends Weapon<T>
{
	public Gun(T type, TFPlayer owner, int slot) {
		super(type, owner, slot);
	}
}
