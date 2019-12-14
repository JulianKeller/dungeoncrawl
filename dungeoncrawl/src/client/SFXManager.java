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
	
	private static ArrayList<SoundEffect> library = new ArrayList<SoundEffect>();
	
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
	 */
	public static void playSound(String action){
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
			if( result.effect.playing() ){
				//restart the sound effect
				result.effect.stop();
			}
			result.effect.play();
		}
	}
	
	/**
	 * Get the currently playing sound with the given action
	 * and stop it.
	 * 
	 * @param action - the action to search
	 */
	public static void stopSound(String action){
		SoundEffect result = null;
		for( SoundEffect se : library ){
			if( se.action.equals(action) ){
				result = se;
				break;
			}
		}
		if( result == null ){
			System.out.println("Sound with action '" + action + "' not found.");
			return;
		}else if( !result.effect.playing() ){
			System.out.println("Sound with action '" + action + "' is not currently playing.");
			return;
		}
		
		result.effect.stop();	
	}
	
	/**
	 * Print a list of available sounds.
	 */
	public static void listSounds(){
		System.out.println("Available sound effects:");
		for( SoundEffect se : library ){
			System.out.print(se.action + " - " + se.effect.toString() );
			if( se.effect.playing() ){
				System.out.print( " <playing>" );
			}
			System.out.println();
		}
	}
}
