package client;

import java.util.ArrayList;

import org.newdawn.slick.Animation;

import jig.Entity;

public class VFXEntity extends Entity{
	
	private class NamedAnimation{
		String effect;
		Animation a;
		
		public NamedAnimation(String effect, Animation a){
			this.effect = effect;
			this.a = a;
		}
	}
	private ArrayList<NamedAnimation> animations;
	
	public VFXEntity(float x, float y){
		super(x, y);
		animations = new ArrayList<NamedAnimation>();
	}
	
	public void addVisualEffect(Animation a, String effect){
		//add an animation to this entity
		for( NamedAnimation na : animations ){
			if( na.effect.toLowerCase().equals(effect.toLowerCase()) ){
				return;
			}
		}
		animations.add(new NamedAnimation(effect, a));
		addAnimation(a);
	}
	
	public void removeDeadAnimations(){
		//remove any animations that have stopped
		for( NamedAnimation a : animations ){
			if( a.a.isStopped() ){
				removeAnimation(a.a);
			}
		}
		
		animations.removeIf(b -> b.a.isStopped());
	}
	
	public boolean hasAnimations(){
		if(animations.size() > 0){
			return true;
		}
		return false;
	}
}
