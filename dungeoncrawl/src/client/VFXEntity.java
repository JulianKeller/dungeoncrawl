package client;

import java.util.ArrayList;

import org.newdawn.slick.Animation;

import jig.Entity;

public class VFXEntity extends Entity{
	
	private class NamedAnimation{
		String effect;
		Animation a;
		int timer;
		
		public NamedAnimation(String effect, int timer, Animation a){
			this.effect = effect;
			this.a = a;
			this.timer = timer;
		}
	}
	private ArrayList<NamedAnimation> animations;
	
	public VFXEntity(float x, float y){
		super(x, y);
		animations = new ArrayList<NamedAnimation>();
	}
	
	public void addVisualEffect(String effect, int timer, Animation a){
		//add an animation to this entity
		for( NamedAnimation na : animations ){
			if( na.effect.toLowerCase().equals(effect.toLowerCase()) ){
				return;
			}
		}
		animations.add(new NamedAnimation(effect, timer, a));
		addAnimation(a);
	}
	
	public void updateVisualEffectTimer(String effect, int time){
		//set the timer of a visual effect to the given value
		//and remove the effect if its timer has expired
		for( NamedAnimation na : animations ){
			
			if( na.effect.equals(effect) ){
				na.timer = time;
				
				if( na.timer <= 0 ){
					na.a.stop();
					removeAnimation(na.a);
				}
			}
		}
		
		animations.removeIf(b -> b.timer <= 0);
	}
	
	public boolean hasAnimations(){
		if(animations.size() > 0){
			return true;
		}
		return false;
	}
}
