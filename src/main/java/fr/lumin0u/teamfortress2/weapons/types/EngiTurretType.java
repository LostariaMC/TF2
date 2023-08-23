package fr.lumin0u.teamfortress2.weapons.types;

import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.EngiTurret;
import fr.lumin0u.teamfortress2.weapons.PlaceableWeapon;
import fr.lumin0u.teamfortress2.weapons.PlacedBlockWeapon;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class EngiTurretType extends PlaceableWeaponType
{
	public EngiTurretType() {
		super(false, Material.BROWN_CARPET, "Tourelle", 1);
	}
	
	@Override
	public Weapon createWeapon(TFPlayer owner, int slot) {
		return new PlaceableWeapon(this, owner, slot) {
			@Override
			public void pickupAmmo() {
				// should be useless
				if(ammo == 0)
					super.pickupAmmo();
			}
		};
	}
	
	@Override
	public PlacedBlockWeapon placeBlock(TFPlayer player, PlaceableWeapon weapon, Block block) {
		EngiTurret turret = new EngiTurret(player, (PlaceableWeapon) weapon, block);
		turret.place();
		return turret;
	}
}
