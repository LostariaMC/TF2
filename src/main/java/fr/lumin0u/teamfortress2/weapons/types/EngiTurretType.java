package fr.lumin0u.teamfortress2.weapons.types;

import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.EngiTurret;
import fr.lumin0u.teamfortress2.weapons.Gun;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.RayTraceResult;

public class EngiTurretType extends PlaceableWeaponType
{
	public static final EngiTurretType INSTANCE = new EngiTurretType();
	public static final WeaponType MANETTE = new WeaponType(false, Material.COMPARATOR, "Manette", 1, -1, 10) {
		@Override
		public void rightClickAction(TFPlayer player, Weapon<?> weapon, RayTraceResult info) {
			Location eyeLoc = player.toBukkit().getLocation();
			((EngiTurret)player.getWeapon(INSTANCE)).updateDirection(eyeLoc.getYaw(), eyeLoc.getPitch());
		}
		
		@Override
		public void leftClickAction(TFPlayer player, Weapon<?> weapon, RayTraceResult info) {
			if(info != null)
				return;
			
			((EngiTurret)player.getWeapon(INSTANCE)).shoot();
		}
	};
	
	private EngiTurretType() {
		super(false, Material.BROWN_CARPET, "Tourelle", 1);
	}
	
	@Override
	public Weapon<?> createWeapon(TFPlayer owner, int slot) {
		return new EngiTurret(this, owner, slot);
	}
	
	@Override
	public void placeBlock(TFPlayer player, Weapon<?> weapon, Block block) {
		
		((EngiTurret)weapon).place(block);
	}
}
