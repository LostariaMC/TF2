package fr.lumin0u.teamfortress2.util;

import fr.lumin0u.teamfortress2.TF;
import fr.worsewarn.cosmox.api.languages.Language;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class I18nItemStack
{
	private Map<Language, ItemStack> items;
	private Function<Language, ItemStack> builder;
	
	public I18nItemStack(Function<Language, ItemStack> itemF) {
		this.builder = itemF;
	}
	
	private void checkTranslationsLoaded() {
		if(items == null) {
			if(TF.getInstance().isEnabled()) {
				items = Stream.of(Language.values()).collect(Collectors.toMap(Function.identity(), builder));
				if(items.containsValue(null))
					throw new NullPointerException("Items cannot be null in an I18nItemStack");
			}
			else {
				throw new IllegalStateException("Languages have not been loaded yet, please get this item later.");
			}
		}
	}
	
	public ItemStack get(WrappedPlayer player) {
		return get(player.toCosmox().getRedisPlayer().getLanguage());
	}
	
	public ItemStack get(Language language) {
		checkTranslationsLoaded();
		
		return items.get(language);
	}
	
	public boolean isSimilar(ItemStack other) {
		checkTranslationsLoaded();
		
		if(other == null)
			return false;
		return items.values().stream().anyMatch(item ->
				other.getType().equals(item.getType()) && other.hasItemMeta() == item.hasItemMeta() && (!other.hasItemMeta() || other.getItemMeta().getDisplayName().equals(item.getItemMeta().getDisplayName()))
		);
	}
	
	/*public static I18nItemStack translateItem(ItemStack item) {
		return new I18nItemStack(language ->
		{
			ItemBuilder builder = new ItemBuilder(item);
			if(item.hasItemMeta()) {
				if(item.getItemMeta().hasDisplayName()) {
					builder.setDisplayName(LanguageManager.getInstance().interpret(item.getItemMeta().getDisplayName(), )))
				}
			}
			return builder.build();
		});
	}*/
}
