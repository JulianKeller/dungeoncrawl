package client;

import jig.Entity;
import jig.Vector;

public class StationaryObject extends Entity{
	/**
	 * Represents an object which remains in the same position on the level.
	 */
	private Vector worldCoordinates; //the object's coordinates on the world (not screen coordinates)
	private boolean locked;
	
	public StationaryObject(Vector worldCoordinates, boolean locked){
		super(worldCoordinates);
		this.worldCoordinates = worldCoordinates;
		this.locked = locked;
	}
	
	public Vector getWorldCoordinates(){
		return worldCoordinates;
	}
	
	public void setWorldCoordinates(Vector wc){
		worldCoordinates = wc;
	}
	
	public boolean isLocked(){
		return locked;
	}
	
	public void lock(){
		locked = true;
	}
	public void unlock(){
		locked = false;
	}
}
