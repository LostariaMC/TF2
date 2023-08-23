package fr.lumin0u.teamfortress2.weapons.types;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.ItemBuilder;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;

import java.util.List;

public abstract class WeaponType
{
	public static final String DAMAGE_LORE = "§7> §6Degats §7: §e§l%d";
	public static final String RELOAD_LORE = "§7> §6Recharge §7: §e§l%.1f s";
	
	/**
	 * you shouldn't rely on material
	 * */
	protected final Material material;
	
	private List<String> lore;
	protected final String name;
	protected final int maxAmmo;
	protected final int reloadTicks;
	protected final int actionDelay;
	protected final boolean ultimate;
	
	public WeaponType(boolean ultimate, Material material, String name, int maxAmmo, int reloadTicks, int actionDelay) {
		this.material = material;
		this.name = name;
		this.maxAmmo = maxAmmo;
		this.reloadTicks = reloadTicks;
		this.actionDelay = actionDelay;
		this.ultimate = ultimate;
	}
	
	public Weapon createWeapon(TFPlayer owner, int slot) {
		return new Weapon(this, owner, slot);
	}
	
	public abstract void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info);
	
	/**
	 * if info is null : it's a simple left click (not hitting another entity) <br />
	 * if info is not null, the owner is hitting an entity <br />
	 * this can be called twice for the same left click with one info at null and one info not-null
	 * */
	public abstract void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info);
	
	public boolean isUltimate() {
		return ultimate;
	}
	
	public String getName() {
		return name;
	}
	
	public int getMaxAmmo() {
		return maxAmmo;
	}
	
	public ItemBuilder buildItem(Weapon weapon) {
		return new ItemBuilder(material)
				.setAmount(weapon.isReloading() ? 1 : weapon.getAmmo())
				.addItemFlag(ItemFlag.HIDE_ITEM_SPECIFICS)
				.setDisplayName((isUltimate() ? ChatColor.LIGHT_PURPLE : ChatColor.BLUE) + name)
				.setLore(getLore());
	}
	
	public boolean isItem(ItemStack item) {
		return item != null && item.getType().equals(material);
	}
	
	protected ImmutableList.Builder<String> loreBuilder() {
		return new Builder<String>().add(RELOAD_LORE.formatted((float) reloadTicks / 20f));
	}
	
	public List<String> getLore() {
		if(lore == null)
			lore = loreBuilder().build();
		return lore;
	}
	
	public int getActionDelay() {
		return actionDelay;
	}
	
	public int getReloadTicks() {
		return reloadTicks;
	}
}
