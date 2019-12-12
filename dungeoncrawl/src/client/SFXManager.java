package client;

import java.util.ArrayList;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;

/**
 * This is a static class for handling all sound effects on the client.
 * 
 * All sounds should be registered and loaded in Main before use with this
 * class.
 * 
 * Client:
 * 	 Registers sound effects in Main
 *   Adds all sounds to the library once using the addSound method
 *   Handles playing and stopping sounds through the playSound and stopSound methods
 *
 * Server:
 *   Hands the client actions from other characters so the client can handle the sounds
 *   Does not directly interact with this class
 */
public class SFXManager {
	static class SoundEffect{
		String action;
		Sound effect;
		
		public SoundEffect(String action, Sound effect){
			this.action = action;
			this.effect = effect;
		}
	}
	/**
	 * Sounds in the nowPlaying list are removed from the library list.
	 */
	private static ArrayList<SoundEffect> nowPlaying = new ArrayList<SoundEffect>();
	
	private static ArrayList<SoundEffect> library = new ArrayList<SoundEffect>();
	
	/*
	public SFXManager(){
		nowPlaying = new ArrayList<SoundEffect>();
		library = new ArrayList<SoundEffect>();
	}
	*/
	
	/**
	 * Add a sound to the sound effect library
	 * 
	 * @param action - the name to associate with this sound effect
	 * @param effect - the sound file to associate with this sound effect
	 * @throws SlickException 
	 */
	public static void addSound(String action, Sound effect) throws SlickException{
		if( effect == null ){
			throw new SlickException("Sound effect is null");
		}
		for( SoundEffect se : library ){
			if( se.action.equals(action) ){
				System.out.println("Sound with action '" + action + "' already in library.");
				return;
			}
		}
		library.add(new SoundEffect(action, effect));
	}
	
	/**
	 * Get the sound with given action from the library
	 * and play it.
	 * 
	 * @param action - the action to search
	 * @throws SlickException 
	 */
	public static void playSound(String action) throws SlickException{
		SoundEffect result = null;
		for( SoundEffect se : library ){
			if( se.action.equals(action) ){
				result = se;
				break;
			}
		}
		if( result == null ){
			System.out.println("Sound with action '" + action + "' not found.");
		}else{
			library.remove(result);
			nowPlaying.add(result);
			
			result.effect.play();
		}
	}
	
	/**
	 * Get the currently playing sound with the given action
	 * and stop it.
	 * 
	 * @param action - the action to search
	 * @throws SlickException 
	 */
	public static void stopSound(String action) throws SlickException{
		SoundEffect result = null;
		for( SoundEffect se : nowPlaying ){
			if( se.action.equals(action) ){
		
				result = se;
				break;
			}
		}
		if( result == null ){
			throw new SlickException("Sound with action '" + action + "' not found.");
		}
		
		result.effect.stop();
		
		nowPlaying.remove(result);
		library.add(result);		
	}
	
	/**
	 * Places all sounds that have stopped back into
	 * the sound effect library.
	 * 
	 * Should be called on each update loop.
	 */
	public static void replaceStoppedSounds(){
		//iterate through all sounds and move the stopped ones back into the library
		ArrayList<SoundEffect> stopped = new ArrayList<SoundEffect>();
		for( SoundEffect se : nowPlaying ){
			if( !se.effect.playing() ){
				stopped.add(se);
			}
		}
		nowPlaying.removeAll(stopped);
		library.addAll(stopped);
	}
	
	/**
	 * Print a list of available sounds.
	 */
	public static void listSounds(){
		System.out.println("Now playing:");
		for( SoundEffect se : nowPlaying ){
			System.out.println(se.action + " - " + se.effect.toString());
		}
		System.out.println("Sound effect library:");
		for( SoundEffect se : library ){
			System.out.println(se.action + " - " + se.effect.toString() );
		}
	}
}