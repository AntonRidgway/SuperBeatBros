/**
 * MissileSprite.java defines the behavior of the game's missiles.
 * by Anton Ridgway
 */

package entities;

import java.util.Random;
import java.util.ArrayList;

import image.ImagesLoader;

public class MissileSprite extends Sprite
{
	private int yBase;

	//variables to pass on to the generated ExplosionSprite
	private int bangRadius;
	private int baseMoveSize;
	private int pWidth;
	private int pHeight;
	private PlayerSprite player;
	private ImagesLoader imsLoader;
	private int period;
	
	private DefenseField dField;
	private ArrayList<ExplosionSprite> expList;
	
	/**
	 * The constructor for the Missile class.
	 * 
	 * @param pW the width of the screen, for reference
	 * @param pH the height of the screen, for reference
	 * @param bH the height of the status bar, for reference
	 * @param bms the base move size of the ground, to be passed on
	 * 			to the generated explosion
	 * @param bRadius the radius of the Explosion to generate
	 * @param ticksToGoal the number of steps to divide the travel distance into
	 * @param e the ArrayList of Explosions, to add one to
	 * @param dF the defenseField, to collide with
	 * @param plr the Player reference to pass on the generated explosion;
	 * 			also allows aimed missiles to be aimed 
	 * @param target a boolean that determines whether the missile should aim
	 * 			for the player
	 * @param imsLd the ImagesLoader to handle the missile's image
	 * @param prd the period of the explosion's animation in ms
	 */
	public MissileSprite( int pW, int pH, int bH, int bms, int bRadius,
			int ticksToGoal, ArrayList<ExplosionSprite> e, DefenseField dF, 
			PlayerSprite plr, boolean target, ImagesLoader imsLd, int prd )
	{
		super( new Random().nextInt(pW), -10, pW, pH, imsLd, "missile");
		Random rand = new Random();
		yBase = pH-bH;
		if (!target)
			setStep((rand.nextInt(pW) - getXPosn())/ticksToGoal, (yBase - getYPosn())/ticksToGoal);
		else
			setStep((plr.getXCenter() - getXPosn())/ticksToGoal, (yBase - getYPosn())/ticksToGoal);
		
		bangRadius = bRadius;
		baseMoveSize = bms;
		pWidth = pW;
		pHeight = pH;
		player = plr;
		imsLoader = imsLd;
		period = prd;
		
		expList = e;
		dField = dF;
	}
	
	/**
	 * updateSprite moves the Missile along its path, and checks for
	 * collisions with the defenseField or the floor.  It generates
	 * a different kind of explosion depending on where it strikes.
	 */
	public void updateSprite()
	{
		if (!finished)
		{
			super.updateSprite();
			int x = (int)getXCenter();
			int y = (int)getYCenter();
			//check each point along the path for collision (should also check for baseline hit, as well as Player hit)
			if(dField.hits(x,y))
			{
				finished = true;
				ExplosionSprite exp = new ExplosionSprite(x, y, pWidth, pHeight, bangRadius, bangRadius-5, 0, player, dField, imsLoader, "exploStay");
				exp.playImage(period, 0.5);
				expList.add(exp);
			}
			else if( y > yBase )
			{
				finished = true;
				setPosition(x,yBase);
				ExplosionSprite exp = new ExplosionSprite(x, y, pWidth, pHeight, bangRadius, 0, baseMoveSize, player, dField, imsLoader, "exploMove");
				exp.playImage(period, 0.5);
				expList.add(exp);
			}
		}
	}
}