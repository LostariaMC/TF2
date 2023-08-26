package fr.lumin0u.teamfortress2.weapons.types;

import com.google.common.collect.ImmutableList.Builder;
import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.TFSound;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class MeleeWeaponType extends WeaponType
{
	protected final double damage;
	protected final double knockback; // blocks/tick
	
	public MeleeWeaponType(boolean ultimate, Material material, String name, int maxAmmo, int reloadTicks, double damage, double knockback) {
		super(ultimate, material, name, maxAmmo, reloadTicks, 0);
		this.damage = damage;
		this.knockback = knockback;
	}
	
	@Override
	public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {}
	
	@Override
	public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
		if(info == null || !(info.getHitEntity() instanceof Player)) {
			TFSound.MELEE_MISS.playTo(player);
			return;
		}
		
		TFPlayer victim = TFPlayer.of(info.getHitEntity());
		
		if(player.isEnemy(victim) && victim.canBeMeleeHit()) {
			Vector kb = player.toBukkit().getEyeLocation().getDirection().multiply(knockback);
			
			if(victim.damage(player, damage, kb)) {
				victim.setLastMeleeHitDate(TF.currentTick());
				weapon.useAmmo();
				TFSound.MELEE_HIT.playTo(player);
			}
		}
	}
	
	public double getDamage() {
		return damage;
	}
	
	public double getKnockback() {
		return knockback;
	}
	
	@Override
	protected Builder<String> loreBuilder() {
		return super.loreBuilder().add(DAMAGE_LORE.formatted(damage));
	}
}
