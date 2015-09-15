/**
 * ExplosionSprite.java defines the behavior of the in-game explosions.
 * by Anton Ridgway
 */

package entities;

import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import image.ImagesLoader;


public class ExplosionSprite extends Sprite
{
	private boolean hitPlayer = false;

	public ExplosionSprite ( int x, int y, int pW, int pH, int radius, int expOffset, int xStep, PlayerSprite player, DefenseField dF, ImagesLoader imsLd, String name )
	{
		super(x-50, y-100+expOffset, pW, pH, imsLd, name);
		setStep(-xStep,0);

		dF.eraseUnder(new Ellipse2D.Double(x-radius, y-radius, radius*2, radius*2));
		int cRadius = radius*2/3;
		if (new Rectangle(x-cRadius, y-cRadius, cRadius, cRadius).intersects(player.getMyRectangle()))
			hitPlayer = true;
	}
	
	/**
	 * hitPlayer returns the boolean of the same name,
	 * which is turned on if the explosion has hit the
	 * Player.
	 * @return hitPlayer the boolean that indicates if this Explosion
	 * 			has hit the Player.
	 */
	public boolean hitPlayer()
	{ return hitPlayer; }
}
