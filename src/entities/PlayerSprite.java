/**
 * Player.java defines the player's character object.
 * by Anton Ridgway
 */

package entities;

import java.util.ArrayList;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import image.ImagesLoader;


public class PlayerSprite extends Sprite
{
	private int yBase;
	
	private final int hPlayerWidth = 60; //half of the player's width
	private final int playerHeight = 60; //the total of the player's height
	
	private boolean leftPressed;
	private boolean rightPressed;
	private boolean downPressed;  //This determines whether the down key is held
	private boolean isCrouching; //This determines whether the Player character is crouching
	private boolean upPressed;
	private boolean isJumping;
	
	private double crouchFactor = 0.7;
	
	// Need to only apply jumping gravity on alternate frames,
	// so the player won't fall too quickly.
	private boolean doGravity = false;
	
	/**
	 * The constructor for the Player
	 * @param pW the screen width, for reference
	 * @param pH the screen height, for reference
	 * @param bH the height of the status bar, for reference
	 * @param imsLd the images loader, to load player sprite
	 * 			images
	 */
	public PlayerSprite( int pW, int pH, int bH, ImagesLoader imsLd )
	{
		super(pW/2-60,pH-bH-60,pW,pH, imsLd, "blues");
		
		yBase = pH - bH;
		releaseControls();
		isCrouching = false;
		isJumping = false;
	}
	
	/**
	 * keyInput manages the input from the keyboard; specific
	 * keyboard events are mapped to particular numbers.
	 * 
	 * @param num the number for the current event
	 */
	public void keyInput( int num )
	{
		switch(num)
		{
			case 0:
				rightPressed = true;
				break;
			case 1:
				rightPressed = false;
				break;
			case 2:
				leftPressed = true;
				break;
			case 3:
				leftPressed = false;
				break;
			case 4:
				downPressed = true;
				break;
			case 5:
				downPressed = false;
				break;
			case 6:
				upPressed = true;
				break;
			case 7:
				upPressed = false;
				break;
		}
	}
	
	/**
	 * releaseControls releases the control variables for the
	 * PlayerSprite.  Used with a game reset, as well as menu-
	 * opening.
	 */
	public void releaseControls()
	{
		rightPressed = false;
		leftPressed = false;
		downPressed = false;
		upPressed = false;
	}
	
	/**
	 * isHit determines whether the PlayerSprite has collided with a 
	 * dangerous object, and returns true or false accordingly.
	 * 
	 * @param A an ArrayList of ExplosionSprites to check
	 * @param w a Wyrm to collision-check against the Player
	 * @return true if the Player is touching something; false otherwise
	 */
	public boolean isHit( ArrayList<ExplosionSprite> A, Wyrm w )
	{
		Rectangle pRect = getMyRectangle();
		for ( int i = 0; i < A.size(); i++ )
		{
			if ( A.get(i).hitPlayer() )
				return true;
		}
		if( w != null && pRect.contains(w.getHead()))
			return true;
		return false;
	}

	/**
	 * reset resets the PlayerSprite to its initial state; used for
	 * game restart.
	 */
	public void reset()
	{
		releaseControls();
		isCrouching = false;
		isJumping = false;
		setStep(0,0);

		setPosition(getPWidth()/2-hPlayerWidth, yBase-playerHeight);
	}
	
	/**
	 * move moves the Player, according to variables set by key presses.
	 * It also manages jumping and gravity.  Gravity is only applied on
	 * alternate frames to make the jump take longer.
	 */
	public void move()
	{
		// Take care of crouching separately from key presses, so that
		// you cannot crouch while the game is paused.
		if (downPressed && !isCrouching)
		{
			//Apply affine transform to squish
			isCrouching = true;
		}
		else if (!downPressed && isCrouching)
		{
			//Apply affine transform to squish
			isCrouching = false;
		}
		
		//The same is done for jumping, but without an action for a released key
		if (upPressed && !isJumping)
		{
			setStep(0,-8);
			isJumping = true;
		}
			
		
		if (rightPressed)
			translate( 5,0 );
		if (leftPressed)
			translate( -5, 0 );
		if (getXPosn()+getWidth() > getPWidth())
			setPosition( getPWidth()-getWidth(), getYPosn() );
		if (getXPosn() < 0)
			setPosition( 0, getYPosn() );
		if (getYPosn()+dy > yBase-getHeight())
		{
			setPosition( getXPosn(), yBase-getHeight() );
			setStep(0,0);
			isJumping = false;
		}
		super.updateSprite();
		
		if (isJumping && doGravity)
			setStep(0,getYStep()+1);
		doGravity = !doGravity;
	}
	
	/**
	 * drawSprite overrides the Sprite's drawSprite method,
	 * and allows for crouching.
	 */
	public void drawSprite(Graphics g)
	{
		if (!isCrouching)
			super.drawSprite(g);
		else
		{
			BufferedImage image = getImage();
		    if (isActive()) {
		        if (image == null) {   // the sprite has no image
		          g.setColor(Color.red);   // draw a red circle
		          g.fillRect((int)locx, (int)locy+ (int)(playerHeight*(1-crouchFactor)), 2*hPlayerWidth, (int)(playerHeight*crouchFactor));
		          g.setColor(Color.black);
		        }
		        else {
		          if (isLooping())
		            image = getIPlayer().getCurrentImage();
		          	((Graphics2D)g).drawImage(image, (int)locx, (int)(locy+playerHeight*(1-crouchFactor)), getWidth(), (int)(getHeight()*crouchFactor), null);
		        }
		      }
		}
	}
}
