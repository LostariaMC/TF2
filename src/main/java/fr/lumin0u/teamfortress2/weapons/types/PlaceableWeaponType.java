package fr.lumin0u.teamfortress2.weapons.types;

import com.destroystokyo.paper.NamespacedTag;
import com.google.common.collect.ImmutableList.Builder;
import fr.lumin0u.teamfortress2.TFEntity;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.ItemBuilder;
import fr.lumin0u.teamfortress2.weapons.PlaceableWeapon;
import fr.lumin0u.teamfortress2.weapons.PlacedBlockWeapon;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.function.Supplier;

public abstract class PlaceableWeaponType extends WeaponType
{
	public PlaceableWeaponType(boolean ultimate, Material material, String name, int maxAmmo) {
		super(ultimate, material, name, maxAmmo, -1, 2);
	}
	
	@Override
	protected Builder<String> loreBuilder() {
		return super.loreBuilder().add(RIGHT_CLICK_LORE.formatted("placer le block"));
	}
	
	@Override
	public Weapon createWeapon(TFPlayer owner, int slot) {
		return new PlaceableWeapon(this, owner, slot);
	}
	
	@Override
	public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
		if(info.getHitBlock() == null)
			return;
		
		Block minePosition = info.getHitBlock().getRelative(info.getHitBlockFace());
		Block downBlock = minePosition.getRelative(BlockFace.DOWN);
		if(minePosition.isEmpty() && minePosition.getRelative(BlockFace.UP).isEmpty()
				&& downBlock.isBuildable() && BoundingBox.of(downBlock).equals(downBlock.getBoundingBox())) {
			((PlaceableWeapon)weapon).getBlocks().add(placeBlock(player, (PlaceableWeapon) weapon, minePosition));
			weapon.useAmmo();
		}
	}
	
	@Override
	public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
	}
	
	public void wrenchPickup(TFPlayer player, PlaceableWeapon weapon, PlacedBlockWeapon clicked) {
		weapon.removeBlock(clicked);
		weapon.pickupAmmo();
	}
	
	public abstract PlacedBlockWeapon placeBlock(TFPlayer player, PlaceableWeapon weapon, Block block);
}
