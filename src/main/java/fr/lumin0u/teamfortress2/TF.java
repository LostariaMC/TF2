package fr.lumin0u.teamfortress2;

import fr.lumin0u.teamfortress2.events.CosmoxListener;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.GameType;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.ImmutableItemStack;
import fr.lumin0u.teamfortress2.util.ItemBuilder;
import fr.worsewarn.cosmox.API;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.api.players.WrappedPlayer.PlayerWrapper;
import fr.worsewarn.cosmox.api.statistics.Statistic;
import fr.worsewarn.cosmox.game.Game;
import fr.worsewarn.cosmox.game.GameVariables;
import fr.worsewarn.cosmox.game.teams.Team;
import fr.worsewarn.cosmox.tools.items.DefaultItemSlot;
import fr.worsewarn.cosmox.tools.map.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class TF extends JavaPlugin {

	private static TF instance;
	private GameManager gameManager;
	
	private final Map<UUID, TFPlayer> players = new HashMap<>();
	private Game cosmoxGame;
	
	public static final ImmutableItemStack LOCKED_ULT_ITEM = new ItemBuilder(Material.BLAZE_ROD)
			.setDisplayName("§5VEROUILLE").setLore("§7Effectuez un kill pour récupérer", "§7Votre capacité spéciale")
			.buildImmutable();

	public static final ImmutableItemStack MENU_ITEM = new ItemBuilder(Material.NAME_TAG)
			.setDisplayName("§6Changer de classe").setLore("§7Changez de §lclasse §7à votre", "§7prochaine §lmort")
			.buildImmutable();

	public static final ImmutableItemStack WR_KIT_ITEM = new ItemBuilder(Material.NAME_TAG)
			.setDisplayName("§6Choix de classe")
			.buildImmutable();
	
	public TF() {
		super();
		
		WrappedPlayer.registerType(new PlayerWrapper<>(TFPlayer.class) {
			@Override
			public TFPlayer unWrap(java.util.UUID uuid) {
				if(!players.containsKey(uuid))
					players.put(uuid, new TFPlayer(uuid));
				return players.get(uuid);
			}
	
			@Override
			public java.util.UUID wrap(TFPlayer tfPlayer) {
				return null;
			}
		});
	}
	
	public void registerListener(Listener listener) {
		Bukkit.getPluginManager().registerEvents(listener, this);
	}
	
	@Override
	public void onEnable() {
		instance = this;
	
		cosmoxGame = new Game("teamfortress2", "TeamFortress2", ChatColor.GOLD, Material.GUNPOWDER, List.of(Team.RED, Team.BLUE), 4, false, false,
				List.of(
						new Statistic("Temps de jeu", GameVariables.TIME_PLAYED, true, "s"),
						new Statistic("Parties jouées", GameVariables.GAMES_PLAYED),
						new Statistic("Victoires", GameVariables.WIN)),
				List.of(),
				List.of("§7Il y a un spy dans le tas ..."),
				List.of(new MapTemplate(MapType.TWO, List.of(
						new MapLocation("name", MapLocationType.STRING),
						new MapLocation("authors", MapLocationType.STRING),
						new MapLocation("redSpawn", MapLocationType.LOCATION),
						new MapLocation("blueSpawn", MapLocationType.LOCATION),
					
						new MapLocation("redSafeZone", MapLocationType.CUBOID),
						new MapLocation("blueSafeZone", MapLocationType.CUBOID)
				))))
				.addForbiddenSlot(IntStream.range(9, 36).boxed().toList())
				.setDefaultFriendlyFire(false)
				.setPreparationTime(5)
				.setShowScoreTablist(true)
				.setGameAuthor("environ lumin0u")
				.addGameTip("§c§lJouez soldier§c, c'est la seule option");
	
		registerListener(new CosmoxListener());
	
		API.instance().registerNewGame(cosmoxGame);

		cosmoxGame.addDefaultItemWaitingRoom(new DefaultItemSlot("kitMenu", WR_KIT_ITEM), 6);
	}
	
	public static TF getInstance() {
		return instance;
	}
	
	public GameManager createGameManager(GameType type, GameMap map) {
		gameManager = type.createManager(map);
		return gameManager;
	}
	
	public GameManager getGameManager() {
		return gameManager;
	}
	
	public TFPlayer loadTFPlayer(WrappedPlayer p) {
		return TFPlayer.of(p);
	}
	
	public Collection<TFPlayer> getPlayers() {
		return new ArrayList<>(players.values());
	}
	
	public Collection<TFPlayer> getNonSpecPlayers() {
		return players.values().stream().filter(Predicate.not(TFPlayer::isSpectator)).collect(Collectors.toSet());
	}
	
	public Game getCosmoxGame() {
		return cosmoxGame;
	}
	
	public void reset() {
		gameManager = null;
		players.clear();
	}
	
	public Kit getKitFromRedis(WrappedPlayer player) {
		return Kit.valueOf((String) API.instance().getRedisAccess().getBucketValue("game.tf2.kits." + player.getUniqueId().toString(), "RANDOM"));
	}
	
	public void setKitInRedis(WrappedPlayer player, Kit kit) {
		API.instance().getRedisAccess().setBucketValue("game.tf2.kits." + player.getUniqueId().toString(), kit.name());
	}
}
