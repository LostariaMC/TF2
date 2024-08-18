package fr.lumin0u.teamfortress2;

import fr.lumin0u.teamfortress2.util.ItemBuilder;
import fr.lumin0u.teamfortress2.util.Items;
import fr.lumin0u.teamfortress2.weapons.types.WeaponType;
import fr.lumin0u.teamfortress2.weapons.types.WeaponTypes;
import fr.worsewarn.cosmox.tools.items.ImmutableItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public enum Kit {
	RANDOM(NamedTextColor.LIGHT_PURPLE, new WeaponType[0], WeaponTypes.KUKRI, 20, 0.2f, 1, 1, Material.BARRIER, false, 0, ""),
	//DEBUG(new AbstractWeapon[] {new MitrailletteLourde(), new Defoncator(), new Blaoups(), new SuperRocketLauncher()}, new Striker(), 20, 0.3f, 100000, 20, Material.CARPET, 0, false, 0, 'z'),
	SCOUT(NamedTextColor.WHITE, new WeaponType[]{WeaponTypes.CANON_SCIE, WeaponTypes.CLUB, WeaponTypes.DEFENSEUR}, WeaponTypes.SCOUT_RACE, 18, 0.32f, 2, 1, Material.FEATHER, true, 1, "░"),
	SOLDIER(TextColor.color(0x9D7154), new WeaponType[] {WeaponTypes.ROCKET_LAUNCHER, WeaponTypes.STD_SHOTGUN, WeaponTypes.FLASHBANG}, WeaponTypes.SCAVENGER, 20, 0.26f, 1, 1, Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, true, 2, "▒"),
	DEMOMAN(TextColor.color(0xB04441), new WeaponType[] {WeaponTypes.DYNAMITE, WeaponTypes.SMOKE, WeaponTypes.STD_SHOTGUN, WeaponTypes.FLARE_GUN}, WeaponTypes.STRIKER, 20, 0.26f, 1, 1, Material.TNT_MINECART, true, 4, "│"),
	HEAVY(TextColor.color(0x5D38C5), new WeaponType[] {WeaponTypes.TORNADO, WeaponTypes.STD_SHOTGUN, WeaponTypes.MACHETE}, WeaponTypes.BEAST_FURY, 32, 0.21f, 1, 1, Material.SHULKER_SHELL, true, 5, "┤"),
	SNIPER(TextColor.color(0x52C538), new WeaponType[] {WeaponTypes.SNIPER, WeaponTypes.MITRAILLETTE, WeaponTypes.HEALTH_POTION}, WeaponTypes.KUKRI, 18, 0.26f, 1, 1, Material.ENDER_EYE, true, 8, "╖"),
	SPY(TextColor.color(0xC5BF2F), new WeaponType[] {WeaponTypes.KNIFE, WeaponTypes.C4, WeaponTypes.REVOLVER, WeaponTypes.INVIS_WATCH}, WeaponTypes.DISGUISE, 16, 0.3f, 2, 1, Material.PUFFERFISH, true, 9, "╕"),
	ENGINEER(TextColor.color(0x55668F), new WeaponType[] {WeaponTypes.TURRET, WeaponTypes.DEFENSEUR, WeaponTypes.TRAMPOLINE, WeaponTypes.MINE, WeaponTypes.CLE_A_MOLETTE}, WeaponTypes.RED_BUTTON, 20, 0.26f, 1, 1, Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, true, 6, "╡"),
	MEDIC(TextColor.color(0xFF62B8), new WeaponType[] {WeaponTypes.MEDIGUN, WeaponTypes.SYRINGE_GUN, WeaponTypes.SAW}, WeaponTypes.UBER_CHARGE, 20, 0.26f, 1, 1, Material.APPLE, true, 7, "╢"),
	PYRO(TextColor.color(0xD56F29), new WeaponType[] {WeaponTypes.BARBECUE, WeaponTypes.STD_SHOTGUN, WeaponTypes.FIRE_AXE}, WeaponTypes.MOLOTOV, 24, 0.26f, 1, 1, Material.BLAZE_POWDER, true, 3, "▓"),
	;
	
	private static Inventory wrInventory;
	private static Inventory menuInventory;
	private final WeaponType[] weapons;
	private final WeaponType special;
	private final int maxHealth;
	private final float speed;
	private final Material blockOnHead;
	private final boolean realKit;
	private final int place;
	private final String symbol;
	private final int valeurCart, valeurCap;
	private final ImmutableItemStack repItem;
	private final TextColor color;
	private final ImmutableItemStack helmet;
	
	private Kit(TextColor color, WeaponType[] weapons, WeaponType special, int maxHealth, float speed, int valeurCart, int valeurCap, Material blockOnHead, boolean realKit, int place, String symbol) {
		this.color = color;
		this.weapons = weapons;
		this.special = special;
		this.maxHealth = maxHealth;
		this.speed = speed;
		this.blockOnHead = blockOnHead;
		this.realKit = realKit;
		this.place = place;
		this.symbol = symbol;
		this.valeurCart = valeurCart;
		this.valeurCap = valeurCap;
		
		this.repItem = setLoreAndName(new ItemBuilder(blockOnHead))
				.addItemFlag(ItemFlag.HIDE_ITEM_SPECIFICS)
				.buildImmutable();
		this.helmet = setLoreAndName(new ItemBuilder(Material.LEATHER_HELMET))
				.setLeatherColor(Color.fromRGB(color.red(), color.green(), color.blue()))
				.buildImmutable();
	}
	
	private ItemBuilder setLoreAndName(ItemBuilder ib) {
		return ib.setDisplayName(Component.text(getName(), Style.style(color, TextDecoration.BOLD)))
				.setLore(Arrays.stream(weapons).map(w -> "§2" + w.getName()).toList())
				.addLore("§d" + special.getName())
				.addLore(special.equals(WeaponTypes.SCOUT_RACE) ? List.of("§dDASH DISPONIBLE") : List.of())
				.addLore("", "§7Vie : §6" + maxHealth, "§7Vitesse : §6%.2f".formatted(speed * 5))
				.addLore("§7Valeur minecart : §6" + valeurCart);
	}
	
	public TextColor getColor() {
		return color;
	}
	
	public static Kit byRepItem(ItemStack item) {
		if(item == null)
			return null;
		
		return Arrays.stream(values()).filter(k -> k.repItem.isSimilar(item)).findAny().orElse(null);
	}
	
	public static Kit getRealRandomKit(Random random) {
		Kit k = Kit.values()[random.nextInt(Kit.values().length)];
		
		while(!k.isReal())
			k = Kit.values()[random.nextInt(Kit.values().length)];
		
		return k;
	}
	
	public static Inventory getKitMenuInventory() {
		if(menuInventory == null) {
			menuInventory = Bukkit.createInventory(null, 4 * 9, "Choisissez une classe");
			
			ItemStack grayGlass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName(" ").build();
			ItemStack pinkGlass = new ItemBuilder(Material.PINK_STAINED_GLASS_PANE).setDisplayName(" ").build();
			ItemStack redGlass = new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName(" ").build();
			ItemStack blueGlass = new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build();
			ItemStack greenGlass = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).setDisplayName(" ").build();
			for(int i = 18; i < 4*9; i++) {
				menuInventory.setItem(i, grayGlass);
			}
			menuInventory.setItem(18 + 3, pinkGlass);
			menuInventory.setItem(18 + 4, pinkGlass);
			menuInventory.setItem(18 + 5, pinkGlass);
			menuInventory.setItem(27 + 3, pinkGlass);
			menuInventory.setItem(27 + 5, pinkGlass);
			
			for(int i = 0; i < values().length; i++) {
				if(!values()[i].isReal()) {
					continue;
				}
				Kit k = values()[i];
				menuInventory.setItem(8 + k.placeInInventory(), k.getRepItem());
			}
			
			menuInventory.setItem(27 + 4, Items.randomKitItem);
			
			menuInventory.setItem(0, redGlass);
			menuInventory.setItem(1, Items.attackTip);
			menuInventory.setItem(2, redGlass);
			menuInventory.setItem(3, blueGlass);
			menuInventory.setItem(4, Items.defenceTip);
			menuInventory.setItem(5, blueGlass);
			menuInventory.setItem(6, greenGlass);
			menuInventory.setItem(7, Items.supportTip);
			menuInventory.setItem(8, greenGlass);
		}
		
		return menuInventory;
	}
	
	public WeaponType[] getWeapons() {
		return weapons;
	}
	
	public int getDefaultSlot(WeaponType weapon) {
		for(int i = 0; i < weapons.length; i++) {
			if(weapons[i].equals(weapon))
				return i;
		}
		return -1;
	}
	
	public WeaponType getSpecial() {
		return special;
	}
	
	public int getMaxHealth() {
		return maxHealth;
	}
	
	public float getSpeed() {
		return speed;
	}
	
	public ItemStack getBlockOnHead() {
		return new ItemStack(blockOnHead);
	}
	
	public boolean isReal() {
		return realKit;
	}
	
	public int placeInInventory() {
		return place;
	}
	
	public String getName() {
		return name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	public int getValeurCart() {
		return valeurCart;
	}
	
	public int getValeurCap() {
		return valeurCap;
	}
	
	public ImmutableItemStack getRepItem() {
		return repItem;
	}
	
	public ImmutableItemStack getHelmet() {
		return helmet;
	}
}
