package fr.lumin0u.teamfortress2.weapons;

import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.types.PlaceableWeaponType;
import fr.lumin0u.teamfortress2.weapons.types.WeaponType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class PlaceableWeapon extends Weapon {
	
	protected List<PlacedBlockWeapon> blocks = new ArrayList<>();
	
	public PlaceableWeapon(PlaceableWeaponType type, TFPlayer owner, int slot) {
		super(type, owner, slot);
	}
	
	@Override
	public final void useAmmo() {
		ammo--;
		updateItem();
	}
	
	public final void pickupAmmo() {
		ammo++;
		updateItem();
	}
	
	@Override
	public void destroy() {
		super.destroy();
		blocks.forEach(PlacedBlockWeapon::destroy);
	}
	
	public Collection<PlacedBlockWeapon> getBlocks() {
		return blocks;
	}
}
