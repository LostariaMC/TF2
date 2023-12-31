package fr.lumin0u.teamfortress2;

import fr.lumin0u.teamfortress2.events.CosmoxListener;
import fr.lumin0u.teamfortress2.game.GameType;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.game.managers.GameManager;
import fr.lumin0u.teamfortress2.util.I18n;
import fr.lumin0u.teamfortress2.util.ItemBuilder;
import fr.lumin0u.teamfortress2.util.Items;
import fr.lumin0u.teamfortress2.util.Utils;
import fr.worsewarn.cosmox.API;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.api.players.WrappedPlayer.PlayerWrapper;
import fr.worsewarn.cosmox.api.statistics.Statistic;
import fr.worsewarn.cosmox.game.Game;
import fr.worsewarn.cosmox.game.GameVariables;
import fr.worsewarn.cosmox.game.configuration.Parameter;
import fr.worsewarn.cosmox.game.teams.Team;
import fr.worsewarn.cosmox.tools.items.DefaultItemSlot;
import fr.worsewarn.cosmox.tools.map.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class TF extends JavaPlugin
{
	
	private static final String WR_SCOREBOARD_KIT = "§7❘ §fClasse: %s";
	public static final String GAME_IDENTIFIER = "teamfortress2";
	
	private static TF instance;
	private GameManager gameManager;
	
	private final Map<UUID, TFPlayer> players = new HashMap<>();
	private Game cosmoxGame;
	
	private static long currentTick = 0;
	
	public TF() {
		super();
		
		WrappedPlayer.registerType(new PlayerWrapper<>(TFPlayer.class)
		{
			@Override
			public TFPlayer unWrap(java.util.UUID uuid) {
				if(!players.containsKey(uuid))
					players.put(uuid, new TFPlayer(uuid));
				return players.get(uuid);
			}
			
			@Override
			public java.util.UUID wrap(TFPlayer tfPlayer) {
				return tfPlayer.getUniqueId();
			}
		});
	}
	
	public void registerListener(Listener listener) {
		Bukkit.getPluginManager().registerEvents(listener, this);
	}
	
	@Override
	public void onEnable() {
		instance = this;
		
		getCommand("getulti").setExecutor(this);
		
		Bukkit.getScheduler().runTaskTimer(this, () -> currentTick++, 1, 1);
		
		cosmoxGame = new Game(GAME_IDENTIFIER, "TeamFortress2", ChatColor.GOLD, Material.TNT_MINECART, List.of(Team.BLUE, Team.RED), 2, false, false,
				List.of(
						new Statistic(I18n.interpretable("main", "statistics_time_played"), GameVariables.TIME_PLAYED, true, "s"),
						new Statistic(I18n.interpretable("main", "statistics_games_played"), GameVariables.GAMES_PLAYED),
						new Statistic(I18n.interpretable("main", "statistics_win"), GameVariables.WIN),
						new Statistic(I18n.interpretable("statistics_kills"), GameVariables.KILLS, "", true, true),
						new Statistic(I18n.interpretable("statistics_damage"), GameVariables.DAMAGES, "", true, true),
						new Statistic(I18n.interpretable("statistics_deaths"), GameVariables.DEATHS, "", true, true),
						new Statistic(I18n.interpretable("statistics_flag_captures"), "flagCaptureCount", "", true, true),
						new Statistic(I18n.interpretable("statistics_push_time"), "pushTime", true, true, true),
						new Statistic(I18n.interpretable("statistics_capture_time"), "captureTime", true, true, true)
				),
				List.of(),
				List.of(" ", I18n.interpretable("game_description")),
				List.of(new MapTemplate(MapType.TWO, List.of(
						new MapLocation("name", MapLocationType.STRING),
						new MapLocation("authors", MapLocationType.STRING),
						new MapLocation("timeOfDay", MapLocationType.STRING, "default"),
						new MapLocation("gamemode", MapLocationType.STRING),
						new MapLocation("redSpawn", MapLocationType.LOCATION),
						new MapLocation("blueSpawn", MapLocationType.LOCATION),
						
						new MapLocation("redSafeZone", MapLocationType.CUBOID),
						new MapLocation("blueSafeZone", MapLocationType.CUBOID),
						
						new MapLocation("redRailsStart", MapLocationType.LOCATION),
						new MapLocation("blueRailsStart", MapLocationType.LOCATION),
						new MapLocation("redRailsEnd", MapLocationType.LOCATION),
						new MapLocation("blueRailsEnd", MapLocationType.LOCATION),
						new MapLocation("redFlag", MapLocationType.LOCATION),
						new MapLocation("blueFlag", MapLocationType.LOCATION),
						
						new MapLocation("kothMiddle", MapLocationType.CUBOID)
				))))
				.setDefaultFriendlyFire(false)
				.setPreparationTime(5)
				.setShowScoreTablist(true)
				.setGameAuthor("lumin0u")
				.addExtraScoreboard(WR_SCOREBOARD_KIT.formatted("§7?"))
				.addExtraScoreboard(" §9§9§8")
				.addParameter(new Parameter("Discord", "", 0, 0, 1,
						new ItemBuilder(fr.worsewarn.cosmox.tools.items.Items.DISCORD.item.clone())
							.setLore(Arrays.asList(" ", "§7Ceci est le déplacement automatique des", "§7équipes sur Discord. En gros c'est", "§7pour séparer les équipes par canaux vocaux", "§7Si vous êtes trop ou que c'est le bordel, tu", "§7sais quoi faire !", " ", " §fServeur par défaut actuel §a%ds", " ", " §7Actuellement %b"))
							.build(),
						List.of(1F), true, false))
				.activeJoinInGame();
		
		registerListener(new CosmoxListener());
		
		API.instance().registerNewGame(cosmoxGame);
		
		cosmoxGame.addDefaultItemWaitingRoom(new DefaultItemSlot("kitMenu", Items.WR_KIT_ITEM.getType(), I18n.interpretable("item_choose_kit"), 7), 7);
	}
	
	public void updatePlayerKitWRScoreboard(WrappedPlayer player, Kit kit) {
		player.toCosmox().getScoreboard().updateLine("Classe", WR_SCOREBOARD_KIT.formatted(Utils.bungeeColor(kit.getColor()) + kit.getName() + " " + kit.getSymbol()));
	}
	
	public static long currentTick() {
		return currentTick;
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
	
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if(sender instanceof Player && sender.getName().equals("lumin0u")) {
			TFPlayer player = TFPlayer.of(sender);
			if(player.getUltimate() != null) {
				player.getUltimate().fullyUnlockUltimate();
			}
		}
		return true;
	}
}
