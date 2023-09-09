package fr.lumin0u.teamfortress2.weapons.types;

import com.google.common.collect.ImmutableList.Builder;
import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.TFEntity;
import fr.lumin0u.teamfortress2.game.managers.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.PlaceableWeapon;
import fr.lumin0u.teamfortress2.weapons.PlacedBlockWeapon;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.FaceAttachable.AttachedFace;
import org.bukkit.block.data.type.Switch;
import org.bukkit.util.RayTraceResult;

public final class C4Type extends PlaceableWeaponType
{
	private final double centerDamage = 10;
	private final double radius = 5;
	
	public C4Type() {
		super(false, Material.POLISHED_BLACKSTONE_BUTTON, "C4", 1, 415);
	}
	
	@Override
	public Weapon createWeapon(TFPlayer owner, int slot) {
		return new C4(owner, slot);
	}
	
	@Override
	protected Builder<String> loreBuilder() {
		return super.loreBuilder().add(DAMAGE_LORE.formatted(centerDamage)).add(RADIUS_LORE.formatted(radius));
	}
	
	@Override
	public boolean isCorrectLocation(Block minePosition, Block downBlock, BlockFace against) {
		return (minePosition.isEmpty() || minePosition.getType() == Material.LIGHT);
	}
	
	@Override
	public PlacedBlockWeapon placeBlock(TFPlayer player, PlaceableWeapon weapon, Block block, BlockFace against) {
		C4Block C4 = new C4Block(player, weapon, block);
		C4.place(against);
		weapon.useAmmo();
		return C4;
	}
	
	public static class C4 extends PlaceableWeapon {
		public C4(TFPlayer owner, int slot) {
			super(WeaponTypes.C4, owner, slot);
		}
		
		private void onExplode() {
			pickupAmmo();
			reload(WeaponTypes.C4.getReloadTicks());
		}
	}
	
	public static class C4Block extends PlacedBlockWeapon {
		private final Igniter igniter = new Igniter(owner, weapon.getSlot());
		private BlockFace face;
		
		public C4Block(TFPlayer owner, PlaceableWeapon weapon, Block block) {
			super(owner, weapon, block);
		}
		
		@Override
		public void place(BlockFace against) {
			face = against;
			block.setType(Material.POLISHED_BLACKSTONE_BUTTON, false);
			BlockData data = block.getBlockData();
			AttachedFace attachedFace = against == BlockFace.UP ? AttachedFace.FLOOR : against == BlockFace.DOWN ? AttachedFace.CEILING : AttachedFace.WALL;
			((Switch)data).setAttachedFace(attachedFace);
			if(attachedFace == AttachedFace.WALL)
				((Switch)data).setFacing(against);
			block.setBlockData(data, false);
			Bukkit.getScheduler().runTaskLater(TF.getInstance(), () -> owner.giveWeapon(igniter), 10);
		}
		
		@Override
		public void destroy() {
			block.setType(Material.AIR, false);
		}
		
		@Override
		public void onWalkOn(TFEntity walker) {}
		
		class Igniter extends Weapon
		{
			public Igniter(TFPlayer owner, int slot) {
				super(WeaponTypes.C4_IGNITER, owner, slot);
			}
			
			@Override
			public void rightClick(RayTraceResult info) {
				Location explosionLoc = block.getLocation().toCenterLocation().add(face.getOppositeFace().getDirection().multiply(0.4));
				GameManager.getInstance().explosion(owner, explosionLoc, WeaponTypes.C4.centerDamage, WeaponTypes.C4.radius, owner::isEnemy, 0);
				weapon.removeBlock(C4Block.this);
				owner.removeWeapon(this);
				owner.toBukkit().getInventory().setItem(slot, null);
				((C4)owner.getWeapon(WeaponTypes.C4)).onExplode();
			}
		}
	}
}
