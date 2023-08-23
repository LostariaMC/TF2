package fr.lumin0u.teamfortress2.weapons;

import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.types.PlaceableWeaponType;
import fr.lumin0u.teamfortress2.weapons.types.WeaponType;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlaceableWeapon extends Weapon {
	
	protected List<PlacedBlockWeapon> blocks = new ArrayList<>();
	
	public PlaceableWeapon(PlaceableWeaponType type, TFPlayer owner, int slot) {
		super(type, owner, slot);
	}
	
	@Override
	public final void useAmmo() {
		ammo--;
		updateItem();
	}
	
	public void pickupAmmo() {
		ammo++;
		updateItem();
	}
	
	public PlaceableWeaponType getType() {
		return (PlaceableWeaponType) type;
	}
	
	@Override
	public void remove() {
		super.remove();
		removeBlocks();
	}
	
	public void removeBlocks() {
		new ArrayList<>(blocks).forEach(this::removeBlock);
	}
	
	public void removeBlock(PlacedBlockWeapon block) {
		blocks.remove(block);
		block.destroy();
	}
	
	public Collection<PlacedBlockWeapon> getBlocks() {
		return blocks;
	}
}
