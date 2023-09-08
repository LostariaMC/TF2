package fr.lumin0u.teamfortress2.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.checkerframework.checker.units.qual.C;

public class Items
{
	public static final ImmutableItemStack attackTip = new ItemBuilder(Material.RED_WOOL)
			.setDisplayName("§cATTAQUE")
			.addLore("§7Classes efficaces en attaque")
			.buildImmutable();
	public static final ImmutableItemStack defenceTip = new ItemBuilder(Material.BLUE_WOOL)
			.setDisplayName(ChatColor.BLUE + "DEFENSE")
			.addLore("§7Classes efficaces en défense")
			.buildImmutable();
	public static final ImmutableItemStack supportTip = new ItemBuilder(Material.LIME_WOOL)
			.setDisplayName("§aSUPPORT")
			.addLore("§7Classes efficaces en support")
			.buildImmutable();
	public static final ImmutableItemStack randomKitItem = new ItemBuilder(Material.AMETHYST_CLUSTER)
			.setDisplayName("§c§kmm§r §d§lClasse Aléatoire §c§kmm")
			.addLore(" ", "§7§l> §5Vous donne une classe §laléatoire", "§5à chaque réapparition")
			.buildImmutable();
	public static final ImmutableItemStack LOCKED_ULT_ITEM = new ItemBuilder(Material.BLAZE_ROD)
			.setDisplayName("§5Verrouillé...").setLore("§7Effectuez un kill pour récupérer", "§7Votre capacité spéciale")
			.buildImmutable();
	public static final ImmutableItemStack MENU_ITEM = new ItemBuilder(Material.NAME_TAG)
			.setDisplayName("§6Changer de classe").setLore("§7Changez de §lclasse §7à votre", "§7prochaine §lmort")
			.buildImmutable();
	public static final ImmutableItemStack WR_KIT_ITEM = new ItemBuilder(Material.NAME_TAG)
			.setDisplayName("§6Choix de classe")
			.buildImmutable();
}
