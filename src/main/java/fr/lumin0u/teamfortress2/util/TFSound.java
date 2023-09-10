package fr.lumin0u.teamfortress2.util;

import fr.lumin0u.teamfortress2.TF;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import org.bukkit.*;
import org.bukkit.Note.Tone;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface TFSound
{
	SoundCombination SILENCE = new SoundCombination();
	
	TFSound PLAYER_DEATH = new SimpleSound(Sound.ENTITY_EVOKER_DEATH, 0.7f, 0.9f, 1.3f, SoundCategory.PLAYERS);
	TFSound GUN_SHOT = new SimpleSound(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1.2f, SoundCategory.PLAYERS);
	TFSound SHOTGUN_SHOT = new SimpleSound(Sound.ENTITY_GENERIC_EXPLODE, 1f, 2f, SoundCategory.PLAYERS);
	TFSound EXPLOSION = new SimpleSound(Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f, SoundCategory.PLAYERS);
	TFSound ULTI_READY = new SimpleSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.7f, SoundCategory.PLAYERS);
	
	TFSound SMOKE_EXPLODE = new SimpleSound(Sound.ENTITY_LLAMA_SPIT, 2f, 0.5f, SoundCategory.MASTER);
	
	TFSound MELEE_MISS = new SimpleSound(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.7f, SoundCategory.PLAYERS);
	TFSound MELEE_HIT = new SimpleSound(Sound.ENTITY_PLAYER_ATTACK_STRONG, 1f, 1.3f, SoundCategory.PLAYERS);
	
	TFSound GET_HEALED = new SimpleSound(Sound.BLOCK_BEACON_ACTIVATE, 1f, 2f, SoundCategory.PLAYERS);
	TFSound HEAL_STOP = new SimpleSound(Sound.BLOCK_BEACON_DEACTIVATE, 1f, 2f, SoundCategory.PLAYERS);
	TFSound SNIPER_SCOPE = new SimpleSound(Sound.ITEM_SPYGLASS_USE, 1f, 1f, SoundCategory.PLAYERS);
	TFSound SNIPER_UNSCOPE = new SimpleSound(Sound.ITEM_SPYGLASS_STOP_USING, 1f, 1f, SoundCategory.PLAYERS);
	TFSound SPY_INVIS_END = new SimpleSound(Sound.BLOCK_ANVIL_LAND, 0.5f, 1f, SoundCategory.PLAYERS);
	
	TFSound SYRINGE_GUN = new SimpleSound(Sound.ENTITY_GUARDIAN_DEATH_LAND, 1f, 2f, SoundCategory.PLAYERS);
	TFSound SCAVENGER = new SimpleSound(Sound.ENTITY_GUARDIAN_HURT, 1f, 2f, SoundCategory.PLAYERS);
	TFSound BARBECUE = new SimpleSound(Sound.ENTITY_GHAST_SHOOT, 1f, 0.8f, SoundCategory.PLAYERS);
	TFSound MOLOTOV_THROW = new SimpleSound(Sound.ENTITY_SPLASH_POTION_THROW, 1f, 1f, SoundCategory.PLAYERS);
	TFSound STRIKER = new SimpleSound(Sound.ENTITY_SPLASH_POTION_THROW, 1f, 1.5f, SoundCategory.PLAYERS);
	TFSound BURP = new SimpleSound(Sound.ENTITY_PLAYER_BURP, 0.5f, 1f, SoundCategory.PLAYERS);
	TFSound DRINK = new SimpleSound(Sound.ENTITY_GENERIC_DRINK, 0.5f, 1f, SoundCategory.PLAYERS);
	TFSound ACTIVATE_INVICIBILITY = new SoundCombination(
			new SimpleSound(Sound.BLOCK_CONDUIT_ACTIVATE, 1f, 1f, SoundCategory.PLAYERS),
			new SimpleSound(Sound.BLOCK_CONDUIT_AMBIENT, 1f, 1f, SoundCategory.PLAYERS));
	TFSound DEACTIVATE_INVICIBILITY = new SimpleSound(Sound.BLOCK_CONDUIT_DEACTIVATE, 1f, 1f, SoundCategory.PLAYERS);
	
	SoundCombination ROCKET_LAUNCHER = new SoundCombination(
			new SimpleSound(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 2f, SoundCategory.PLAYERS),
			new SimpleSound(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 2f, SoundCategory.PLAYERS));
	
	SoundCombination FLARE_GUN = new SoundCombination(Map.of(
			new SimpleSound(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 0.6f, SoundCategory.PLAYERS), 0,
			new SimpleSound(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 0.6f, SoundCategory.PLAYERS), 3,
			new SimpleSound(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 0.6f, SoundCategory.PLAYERS), 6));
	
	TFSound TURRET_CONSTRUCT = new SimpleSound(Sound.BLOCK_ANVIL_USE, 0.5f, 0.5f, SoundCategory.PLAYERS);
	TFSound TURRET_CONSTRUCT_READY = new SimpleSound(Sound.BLOCK_ANVIL_PLACE, 0.5f, 1f, SoundCategory.PLAYERS);
	TFSound TURRET_DIRECTION = new SimpleSound(Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 0.5f, SoundCategory.PLAYERS);
	TFSound TURRET_READY = new SimpleSound(Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f, SoundCategory.PLAYERS);
	TFSound TURRET_SHOOT = new SoundCombination(
			new SimpleSound(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 2f, SoundCategory.PLAYERS),
			new SimpleSound(Sound.ENTITY_GENERIC_EXPLODE, 1f, 2f, SoundCategory.PLAYERS));
	
	TFSound SCOUT_DASH = new SimpleSound(Sound.ENTITY_BAT_TAKEOFF, 0.5f, 0.8f, SoundCategory.PLAYERS);
	
	TFSound MY_FLAG_CAPTURED = new SoundCombination(Map.of(
			new InstrumentNote(Instrument.PIANO, Note.natural(0, Tone.D)), 0,
			new InstrumentNote(Instrument.BASS_GUITAR, Note.natural(0, Tone.D)), 0,
			new InstrumentNote(Instrument.PIANO, Note.natural(0, Tone.D)), 4,
			new InstrumentNote(Instrument.BASS_GUITAR, Note.natural(0, Tone.D)), 4,
			new InstrumentNote(Instrument.PIANO, Note.natural(0, Tone.D)), 8,
			new InstrumentNote(Instrument.BASS_GUITAR, Note.natural(0, Tone.D)), 8));
	TFSound OTHER_FLAG_CAPTURED = new SoundCombination(Map.of(
			new InstrumentNote(Instrument.PIANO, Note.natural(1, Tone.D)), 0,
			new InstrumentNote(Instrument.BASS_GUITAR, Note.natural(1, Tone.D)), 0,
			new InstrumentNote(Instrument.PIANO, Note.natural(1, Tone.D)), 4,
			new InstrumentNote(Instrument.BASS_GUITAR, Note.natural(1, Tone.D)), 4,
			new InstrumentNote(Instrument.PIANO, Note.natural(1, Tone.D)), 8,
			new InstrumentNote(Instrument.BASS_GUITAR, Note.natural(1, Tone.D)), 8));
	
	
	public void playTo(WrappedPlayer player);
	public void play(Location location);
	public void play(Location location, List<WrappedPlayer> listeners);
	public boolean isSilence();
	public TFSound withVolume(float volume);
	
	
	public static class SimpleSound implements TFSound
	{
		private final Sound sound;
		private final float volume;
		private final float pitchMin;
		private final float pitchMax;
		private final SoundCategory category;
		
		public SimpleSound(Sound sound, float volume, float pitchMin, float pitchMax, SoundCategory category) {
			this.sound = sound;
			this.volume = volume;
			this.pitchMin = pitchMin;
			this.pitchMax = pitchMax;
			this.category = category;
		}
		
		public SimpleSound(Sound sound, float volume, float pitch, SoundCategory category) {
			this(sound, volume, pitch, pitch, category);
		}
		
		private float getPitch() {
			return pitchMin == pitchMax ? pitchMin : new Random().nextFloat(pitchMin, pitchMax);
		}
		
		@Override
		public void playTo(WrappedPlayer player) {
			if(isSilence())
				return;
			if(player.isOnline()) {
				if(category == null)
					player.toBukkit().playSound(player.toBukkit().getLocation(), sound, volume, getPitch());
				else
					player.toBukkit().playSound(player.toBukkit().getLocation(), sound, category, volume, getPitch());
			}
		}
		
		@Override
		public void play(Location location) {
			if(isSilence())
				return;
			
			if(category == null)
				location.getWorld().playSound(location, sound, volume, getPitch());
			else
				location.getWorld().playSound(location, sound, category, volume, getPitch());
		}
		
		@Override
		public void play(Location location, List<WrappedPlayer> listeners) {
			if(isSilence())
				return;
			
			listeners.stream().filter(WrappedPlayer::isOnline).map(WrappedPlayer::toBukkit).forEach(player -> {
				if(category == null)
					player.playSound(location, sound, volume, getPitch());
				else
					player.playSound(location, sound, category, volume, getPitch());
			});
		}
		
		@Override
		public boolean isSilence() {
			return sound == null;
		}
		
		public Sound sound() {return sound;}
		
		public float volume() {return volume;}
		
		public float pitchMin() {return pitchMin;}
		
		public float pitchMax() {return pitchMax;}
		
		public SoundCategory category() {return category;}
		
		@Override
		public TFSound withVolume(float volume) {
			return new SimpleSound(sound, volume, pitchMin, pitchMax, category);
		}
	}
	
	public static class InstrumentNote implements TFSound
	{
		private final Instrument instrument;
		private final Note note;
		
		public InstrumentNote(Instrument instrument, Note note) {
			this.instrument = instrument;
			this.note = note;
		}
		
		@Override
		public void playTo(WrappedPlayer player) {
			player.toBukkit().playNote(player.toBukkit().getLocation(), instrument, note);
		}
		
		@Override
		public void play(Location location) {
			location.getNearbyEntitiesByType(Player.class, 32).forEach(p -> {
				p.playNote(location, instrument, note);
			});
		}
		
		@Override
		public void play(Location location, List<WrappedPlayer> listeners) {
			location.getNearbyEntitiesByType(Player.class, 32).stream().map(WrappedPlayer::of).filter(listeners::contains).forEach(p -> {
				p.toBukkit().playNote(location, instrument, note);
			});
		}
		
		@Override
		public boolean isSilence() {
			return false;
		}
		
		@Override
		public TFSound withVolume(float volume) {
			return this;
		}
	}
	
	public static class SoundCombination implements TFSound
	{
		private final Map<TFSound, Integer> soundDelays;
		
		public SoundCombination(TFSound... sounds) {
			this(Arrays.stream(sounds).collect(Collectors.toMap(Function.identity(), s -> 0)));
		}
		
		public SoundCombination(List<TFSound> sounds) {
			this(sounds.stream().collect(Collectors.toMap(Function.identity(), s -> 0)));
		}
		
		public SoundCombination(Map<TFSound, Integer> soundDelays) {
			this.soundDelays = Collections.unmodifiableMap(soundDelays);
		}
		
		@Override
		public void playTo(WrappedPlayer player) {
			soundDelays.forEach((sound, delay) -> {
				if(delay == 0) {
					sound.playTo(player);
				}
				else {
					Bukkit.getScheduler().runTaskLater(TF.getInstance(), () -> sound.playTo(player), delay);
				}
			});
		}
		
		@Override
		public void play(Location location) {
			Location realLoc = location.clone();
			soundDelays.forEach((sound, delay) -> {
				if(delay == 0) {
					sound.play(realLoc);
				}
				else {
					Bukkit.getScheduler().runTaskLater(TF.getInstance(), () -> sound.play(realLoc), delay);
				}
			});
		}
		
		@Override
		public void play(Location location, List<WrappedPlayer> listeners) {
			Location realLoc = location.clone();
			soundDelays.forEach((sound, delay) -> {
				if(delay == 0) {
					sound.play(realLoc, listeners);
				}
				else {
					Bukkit.getScheduler().runTaskLater(TF.getInstance(), () -> sound.play(realLoc, listeners), delay);
				}
			});
		}
		
		@Override
		public boolean isSilence() {
			return soundDelays.keySet().stream().allMatch(TFSound::isSilence);
		}
		
		@Override
		public TFSound withVolume(float volume) {
			throw new UnsupportedOperationException("j'ai eu la flemme de le coder");
		}
	}
}
