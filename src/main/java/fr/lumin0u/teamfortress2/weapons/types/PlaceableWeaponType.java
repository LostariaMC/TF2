package fr.lumin0u.teamfortress2.weapons.types;

import com.destroystokyo.paper.NamespacedTag;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.ItemBuilder;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.RayTraceResult;

public abstract class PlaceableWeaponType extends WeaponType
{
	public PlaceableWeaponType(boolean ultimate, Material material, String name, int maxAmmo) {
		super(ultimate, material, name, maxAmmo, 0, 0);
	}
	
	@Override
	public void rightClickAction(TFPlayer player, Weapon<?> weapon, RayTraceResult info) {
		if(info.getHitBlock() == null)
			return;
		
		Block minePosition = info.getHitBlock().getRelative(info.getHitBlockFace());
		if(minePosition.isEmpty() && minePosition.getRelative(BlockFace.UP).isEmpty()) {
			placeBlock(player, weapon, minePosition);
			weapon.useAmmo();
		}
	}
	
	@Override
	public void leftClickAction(TFPlayer player, Weapon<?> weapon, RayTraceResult info) {
	
	}
	
	@Override
	public ItemBuilder buildItem(Weapon<?> weapon) {
		return super.buildItem(weapon).addPlaceableKeys(NamespacedTag.minecraft(""));
	}
	
	public abstract void placeBlock(TFPlayer player, Weapon<?> weapon, Block block);
}
