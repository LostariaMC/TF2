package fr.lumin0u.teamfortress2.weapons.types;

import com.google.common.collect.ImmutableList.Builder;
import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import fr.lumin0u.teamfortress2.weapons.types.InvisWatchType.InvisWatch;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public final class KnifeType extends MeleeWeaponType
{
	public KnifeType() {
		super(false, Material.NETHERITE_SWORD, "Poignard", 1, 76, 7, 0.2);
	}
	
	@Override
	protected Builder<String> loreBuilder() {
		return super.loreBuilder().add(CUSTOM_LORE.formatted("§6Dégats dans le dos§7: §e§l" + (damage * 2)));
	}
	
	@Override
	public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
		if(info == null || !(info.getHitEntity() instanceof Player)) {
			return;
		}
		
		TFPlayer victim = TFPlayer.of(info.getHitEntity());
		
		if(player.isEnemy(victim) && victim.canBeMeleeHit()) {
			Vector kb = player.toBukkit().getEyeLocation().getDirection().multiply(knockback);
			
			boolean stab = player.toBukkit().getEyeLocation().getDirection().dot(victim.toBukkit().getEyeLocation().getDirection()) > 0;
			
			if(victim.damage(player, stab ? damage * 2 : damage, kb)) {
				victim.setLastMeleeHitDate(TF.currentTick());
				weapon.useAmmo();
				
				Location particleLoc = victim.getLocation().add(0, 1.2, 0);
				
				particleLoc.getWorld().spawnParticle(Particle.REDSTONE, particleLoc, 40, 0.3, 0.3, 0.3, 0.7, new DustOptions(Color.fromRGB(0xb20063), 0.7f), true);
				
				player.getOptWeapon(WeaponTypes.INVIS_WATCH).ifPresent(w -> ((InvisWatch)w).setInvisibilityCancelled(true));
			}
		}
	}
}
