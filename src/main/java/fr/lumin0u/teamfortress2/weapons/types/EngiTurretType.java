package fr.lumin0u.teamfortress2.weapons.types;

import com.google.common.collect.ImmutableList.Builder;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.EngiTurret;
import fr.lumin0u.teamfortress2.weapons.PlaceableWeapon;
import fr.lumin0u.teamfortress2.weapons.PlacedBlockWeapon;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;

public final class EngiTurretType extends PlaceableWeaponType
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
	protected Builder<String> loreBuilder() {
		return super.loreBuilder().add(CUSTOM_LORE.formatted("Placez une tourelle qui tire des obus"));
	}
	
	@Override
	public PlacedBlockWeapon placeBlock(TFPlayer player, PlaceableWeapon weapon, Block block) {
		EngiTurret turret = new EngiTurret(player, (PlaceableWeapon) weapon, block);
		turret.place();
		player.toBukkit().setGameMode(GameMode.ADVENTURE);
		return turret;
	}
	
	@Override
	public void wrenchPickup(TFPlayer player, PlaceableWeapon weapon, PlacedBlockWeapon clicked) {
		if(!((EngiTurret)clicked).isReloading())
			super.wrenchPickup(player, weapon, clicked);
		else
			player.toBukkit().sendActionBar("§cLa tourelle recharge !");
	}
}
