package fr.lumin0u.teamfortress2;

import fr.lumin0u.teamfortress2.util.ImmutableItemStack;
import fr.lumin0u.teamfortress2.util.ItemBuilder;
import fr.lumin0u.teamfortress2.weapons.types.WeaponType;
import fr.lumin0u.teamfortress2.weapons.types.Weapons;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

public enum Kit
{
	NOKIT(new WeaponType[0], Weapons.KUKRI, 20, 0.2f, 1, 1, Material.AIR, false, 0, ' '),
	//DEBUG(new AbstractWeapon[] {new MitrailletteLourde(), new Defoncator(), new Blaoups(), new SuperRocketLauncher()}, new Striker(), 20, 0.3f, 100000, 20, Material.CARPET, 0, false, 0, 'z'),
	
	SCOUT(new WeaponType[]{Weapons.CANON_SCIE, Weapons.BATTE, Weapons.DEFENSEUR}, Weapons.SCOUT_RACE, 18, 0.32f, 2, 1, Material.GRAY_CONCRETE, true, 1, '░'),
	/*SOLDIER(new AbstractWeapon[] {new RocketLauncher(), new FusilAPompe(), new GrenadeFlash()}, new Scavenger(), 20, 0.26f, 1, 1, Material.STAINED_CLAY, 7, true, 2, '▒'),
	DEMOMAN(new AbstractWeapon[] {new Dynamite(), new Fumigene(), new FusilAPompe(), new FuseeDeDetresse()}, new Striker(), 20, 0.26f, 1, 1, Material.STAINED_CLAY, 5, true, 4, '│'),
	HEAVY(new AbstractWeapon[] {new LaTornade(), new FusilAPompe(), new PoingsAmericains()}, new BeastFury(), 32, 0.21f, 1, 1, Material.STAINED_CLAY, 4, true, 5, '┤'),
	SNIPER(new AbstractWeapon[] {new Sniper(), new PistoletAutomatique(), new HealthPotion()}, new Kukri(), 18, 0.26f, 1, 1, Material.STAINED_CLAY, 1, true, 8, '╖'),
	SPY(new AbstractWeapon[] {new Poignard(), new C4(), new Revolver(), new MontreInvi()}, new Disguise(), 16, 0.3f, 2, 1, Material.STAINED_CLAY, 0, true, 9, '╕'),
	ENGINEER(new AbstractWeapon[] {new CanonMontable(), new Defenseur(), new Trampoline(), new Mine(), new CleMolette()}, new RedButton(), 20, 0.26f, 1, 1, Material.STAINED_CLAY, 3, true, 6, '╡'),
	MEDIC(new AbstractWeapon[] {new MedecinePortable(), new PistoletTranquilisant(), new ScieAmputation()}, new UberCharge(), 20, 0.26f, 1, 1, Material.STAINED_CLAY, 2, true, 7, '╢'),
	PYRO(new AbstractWeapon[] {new Barbecue(), new FusilAPompe(), new Hache()}, new CoktailMolotov(), 24, 0.28f, 1, 1, Material.STAINED_CLAY, 6, true, 3, '▓')*/;
	
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
	
	public static Kit byRepItem(ItemStack item) {
		for(Kit k : values()) {
			if(k.getRepItem().getType().equals(item.getType()) && item.hasItemMeta() && item.getItemMeta().getDisplayName().equals(k.getRepItem().getItemMeta().getDisplayName()))
				return k;
		}
		
		return null;
	}
	
	public static Kit getRealRandomKit(Random random) {
		Kit k = Kit.values()[random.nextInt(Kit.values().length)];
		
		while(!k.isReal())
			k = Kit.values()[random.nextInt(Kit.values().length)];
		
		return k;
	}
}
