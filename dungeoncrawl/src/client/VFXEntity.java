package client;

import java.util.ArrayList;

import org.newdawn.slick.Animation;

import jig.Entity;

public class VFXEntity extends Entity{
	private ArrayList<Animation> animations;
	
	public VFXEntity(float x, float y){
		super(x, y);
		animations = new ArrayList<Animation>();
	}
	
	public void addVisualEffect(Animation a){
		//add an animation to this entity
		animations.add(a);
		addAnimation(a);
	}
	
	public void removeDeadAnimations(){
		//remove any animations that have stopped
		for( Animation a : animations ){
			if( a.isStopped() ){
				removeAnimation(a);
			}
		}
		
		animations.removeIf(b -> b.isStopped());
	}
	
	public boolean hasAnimations(){
		if(animations.size() > 0){
			return true;
		}
		return false;
	}
}
