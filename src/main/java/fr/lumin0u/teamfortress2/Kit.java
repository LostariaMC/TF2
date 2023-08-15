package fr.lumin0u.teamfortress2;

import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.util.ImmutableItemStack;
import fr.lumin0u.teamfortress2.util.ItemBuilder;
import fr.lumin0u.teamfortress2.weapons.types.WeaponType;
import fr.lumin0u.teamfortress2.weapons.types.Weapons;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

public enum Kit {
	RANDOM(new WeaponType[0], Weapons.KUKRI, 20, 0.2f, 1, 1, Material.AIR, false, 0, ' '),
	//DEBUG(new AbstractWeapon[] {new MitrailletteLourde(), new Defoncator(), new Blaoups(), new SuperRocketLauncher()}, new Striker(), 20, 0.3f, 100000, 20, Material.CARPET, 0, false, 0, 'z'),
	SCOUT(new WeaponType[]{Weapons.CANON_SCIE, Weapons.BATTE, Weapons.DEFENSEUR}, Weapons.SCOUT_RACE, 18, 0.32f, 2, 1, Material.YELLOW_CONCRETE, true, 1, '░'),
	//SOLDIER(new AbstractWeapon[] {new RocketLauncher(), new FusilAPompe(), new GrenadeFlash()}, new Scavenger(), 20, 0.26f, 1, 1, Material.BROWN_CONCRETE, 7, true, 2, '▒'),
	//DEMOMAN(new AbstractWeapon[] {new Dynamite(), new Fumigene(), new FusilAPompe(), new FuseeDeDetresse()}, new Striker(), 20, 0.26f, 1, 1, Material.STAINED_CLAY, 5, true, 4, '│'),
	//HEAVY(new AbstractWeapon[] {new LaTornade(), new FusilAPompe(), new PoingsAmericains()}, new BeastFury(), 32, 0.21f, 1, 1, Material.STAINED_CLAY, 4, true, 5, '┤'),
	SNIPER(new AbstractWeapon[] {new Sniper(), new PistoletAutomatique(), new HealthPotion()}, new Kukri(), 18, 0.26f, 1, 1, Material.STAINED_CLAY, 1, true, 8, '╖'),
	//SPY(new AbstractWeapon[] {new Poignard(), new C4(), new Revolver(), new MontreInvi()}, new Disguise(), 16, 0.3f, 2, 1, Material.STAINED_CLAY, 0, true, 9, '╕'),
	ENGINEER(new WeaponType[] {Weapons.TURRET, Weapons.DEFENSEUR, Weapons.TRAMPOLINE, Weapons.MINE, Weapons.CLE_A_MOLETTE}, Weapons.RED_BUTTON, 20, 0.26f, 1, 1, Material.GRAY_CONCRETE, true, 6, '╡'),
	//MEDIC(new AbstractWeapon[] {new MedecinePortable(), new PistoletTranquilisant(), new ScieAmputation()}, new UberCharge(), 20, 0.26f, 1, 1, Material.STAINED_CLAY, 2, true, 7, '╢'),
	//PYRO(new AbstractWeapon[] {new Barbecue(), new FusilAPompe(), new Hache()}, new CoktailMolotov(), 24, 0.28f, 1, 1, Material.STAINED_CLAY, 6, true, 3, '▓'),
	;
	
	public static final ImmutableItemStack attackTip = new ItemBuilder(Material.RED_DYE)
			.setDisplayName("§cATTAQUE")
			.addLore("§7Classes efficaces en attaque")
			.buildImmutable();
	
	public static final ImmutableItemStack defenceTip = new ItemBuilder(Material.BLUE_DYE)
			.setDisplayName("§1DEFENSE")
			.addLore("§7Classes efficaces en défense")
			.buildImmutable();
	
	public static final ImmutableItemStack supportTip = new ItemBuilder(Material.LIME_DYE)
			.setDisplayName("§aSUPPORT")
			.addLore("§7Classes efficaces en support")
			.buildImmutable();
	
	public static final ImmutableItemStack randomKitItem = new ItemBuilder(Material.PUFFERFISH)
			.setDisplayName("§c§kmm§r §fClasse Aléatoire §c§kmm")
			.addLore("§bBien plus fun comme ca !", "§7C'est peut-être une mauvaise idée...")
			.buildImmutable();
	
	private static Inventory wrInventory;
	private static Inventory menuInventory;
	private final WeaponType[] weapons;
	private final WeaponType special;
	private final int maxHealth;
	private final float speed;
	private final Material blockOnHead;
	private final boolean realKit;
	private final int place;
	private final char symbole;
	private final int valeurCart, valeurCap;
	private final ImmutableItemStack repItem;
	
	private Kit(WeaponType[] weapons, WeaponType special, int maxHealth, float speed, int valeurCart, int valeurCap, Material blockOnHead, boolean realKit, int place, char symbole) {
		this.weapons = weapons;
		this.special = special;
		this.maxHealth = maxHealth;
		this.speed = speed;
		this.blockOnHead = blockOnHead;
		this.realKit = realKit;
		this.place = place;
		this.symbole = symbole;
		this.valeurCart = valeurCart;
		this.valeurCap = valeurCap;
		
		this.repItem = !isReal() ? new ItemBuilder(blockOnHead)/*.setDisplayName(getName()).addLore("Cette classe n'existe pas vraiment...")*/.buildImmutable()
				: new ItemBuilder(blockOnHead)
				.setDisplayName("§6" + getName())
				.addLore(Arrays.stream(weapons).map(WeaponType::getName).collect(Collectors.toList()))
				.addLore("", "§7Vie : §6" + maxHealth, "§7Vitesse : §6%.2f".formatted(speed))
				.addLore("§7Valeur minecart : §6" + valeurCart, "§7Valeur de capture : §6" + valeurCap)
				.buildImmutable();
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
	
	public static Inventory getWRInventory() {
		if(wrInventory == null) {
			wrInventory = Bukkit.createInventory(null, 4 * 9, "Choisissez une classe");
			
			Arrays.stream(values()).filter(Kit::isReal).forEach(kit -> wrInventory.setItem(kit.place - 1, kit.repItem));
			
			wrInventory.setItem(18 + 4, randomKitItem);
		}
		
		return wrInventory;
	}
	
	public static Inventory getKitMenuInventory() {
		if(menuInventory == null) {
			Inventory inv = Bukkit.createInventory(null, 6 * 9, "Choisissez une classe");
			
			for(int i = 0; i < values().length; i++) {
				if(!values()[i].isReal()) {
					continue;
				}
				Kit k = values()[i];
				inv.setItem(8 + k.placeInInventory(), k.getRepItem());
			}
			
			inv.setItem(27 + 4, randomKitItem);
			inv.setItem(1, attackTip);
			inv.setItem(4, defenceTip);
			inv.setItem(7, supportTip);
		}
		
		return menuInventory;
	}
	
	public WeaponType[] getWeapons() {
		return weapons;
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
		return name().toCharArray()[0] + name().replaceFirst(String.valueOf(name().toCharArray()[0]), "").toLowerCase();
	}
	
	public char getSymbol() {
		return symbole;
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
}
