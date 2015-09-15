/**
 * NoteSprite.java defines the behavior of the "note" paint
 * refill objects for the game.
 * by Anton Ridgway
 */

package entities;

import java.util.Random;
import image.ImagesLoader;

public class NoteSprite extends Sprite
{
	private int value; //how much paint is this worth?
	private int yBase;
	private DefenseField dField;

	public NoteSprite( int pW, int pH, int yB, int v, DefenseField dF, ImagesLoader imsLd)
	{
		super(new Random().nextInt(pW-24), -35, pW, pH, imsLd, "note");
		setStep(0,1);
		value = v;
		yBase = yB;
		dField = dF;

	}

	/**
	 * getValue returns the ink value for the current InkRefill object
	 * @return value how much ink the object is worth
	 */
	public int getValue()
	{
		return value;
	}
	

	/**
	 * updateSprite calls the Sprite parent class's updateSprite method,
	 * and then checks for collision with the DefenseField, or the ground. 
	 */
	public void updateSprite()
	{
		super.updateSprite();
		if (dField.hits((int)getXCenter(), (int)getYCenter()) || getYCenter() > yBase )
			finished = true;			
	}
}
