/**
 * DefenseField.java defines the behavior of the player's "defense field."
 * It can be drawn on with the mouse to protect the player from incoming
 * projectiles.
 * 
 * It consists of a two-dimensional integer array; 0 represents an empty
 * location, while a value of one represents a filled location.  Integers
 * are used so that varying types of "paint" can be implemented in a later
 * version.
 * 
 * Mouse coordinates are mapped to specific array entries according to a
 * constant "resolution" integer, which determines the precision of the
 * in-game field and allows its pixel size to increase if slow-down is
 * caused by too many draw operations.  A proper value must be selected that
 * will not cause an out of bounds error for the 2D-array from integer division.
 * 
 * by Anton Ridgway
 */
package entities;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import image.ImagesLoader;

public class DefenseField
{

	private int[][] defenseArray;
	private Graphics2D imageGfx;
	private Graphics2D finalGfx;
	private BufferedImage imageMask;
	private BufferedImage defImage;
	private BufferedImage finalImage;
	private ImagesLoader imsLd;
	private int pWidth;
	private int pHeight;
	private int highBound;
	private int lowBound;
	private int brushSize = 10;
	private boolean hasChanged = false;
	private final int res = 1;

	public DefenseField( int pW, int pH, ImagesLoader il )
	{
		pWidth = pW;
		pHeight = pH;
		highBound = 50;
		lowBound = pHeight - 210;
		
		//broken up for integer division
		defenseArray = new int[pWidth/res][(lowBound/res)-(highBound/res)];
		for (int i = 0; i < defenseArray.length; i++)
			Arrays.fill(defenseArray[i], 0);

		imsLd = il;

		defImage = imsLd.getImage("maplesheet");
		finalImage = new BufferedImage(pWidth, lowBound-highBound, BufferedImage.TYPE_INT_ARGB);
		imageMask = new BufferedImage(pWidth, lowBound-highBound, BufferedImage.TYPE_INT_ARGB);
		imageGfx = imageMask.createGraphics();
		finalGfx = finalImage.createGraphics();
		imageGfx.setColor(Color.red);
	}
	
	/**
	 * getBrushSize returns the current brushSize for the DefenseField.
	 * 
	 * @return brushSize the current brush size
	 */
	public int getBrushSize()
	{
		return brushSize;
	}
	
	/**
	 * getLowBound returns the y-value of the lower bound for the field.
	 * @return lowBound the lower bound
	 */
	public int getLowBound()
	{
		return lowBound;
	}
	
	/**
	 * drawBrush fills a rounded square of space with ink on the
	 * DefenseField.  It fills it by rows, and checks to make sure
	 * it is not drawing outside the given area.
	 * 
	 * @param x the x-coordinate to draw around
	 * @param y the y-coordinate to draw around
	 * @param pA the current amount of paint
	 */
	public int drawBrush( int x, int y, int pA )
	{
		int tempX = x/res;
		int tempY = y/res;
		//fill the inside rows
		for(int i = tempX - brushSize; i <= tempX + brushSize; i++)
		{
			for (int j = (tempY - (brushSize-1)); j <= (tempY + (brushSize-1)); j++)
			{
				if (i > 0 && i < pWidth/res && j < lowBound/res &&
						j >= highBound/res && defenseArray[i][j-highBound/res] < 1 && pA > 0)
				{
					pA--;
					defenseArray[i][j-highBound/res] = 1;
				}
			}
		}
		//fill the top and bottom rows separately to make the square rounded
		for (int i = tempX-brushSize+1; i <= tempX+brushSize-1; i++)
		{
			//top row
			if (i > 0 && i < pWidth/res && (tempY - brushSize) >= highBound/res && (tempY + brushSize) < lowBound/res && defenseArray[i][tempY - brushSize - highBound/res] < 1 && pA > 0)
			{
				pA--;
				defenseArray[i][tempY - brushSize - highBound/res] = 1;
			}
			//bottom row
			if (i > 0 && i < pWidth/res && (tempY + brushSize) >= highBound/res && (tempY + brushSize) < lowBound/res && defenseArray[i][tempY + brushSize - highBound/res] < 1 && pA > 0)
			{
				pA--;
				defenseArray[i][tempY + brushSize - highBound/res] = 1;
			}
		}
		imageGfx.fillRect(x-brushSize*res, y-(brushSize-1)*res-highBound, brushSize*res*2, ((brushSize-1)*res*2));
		imageGfx.fillRect(x-(brushSize-1)*res, y-brushSize*res-highBound, (brushSize-1)*res*2, res);
		imageGfx.fillRect(x-(brushSize-1)*res, y+(brushSize-1)*res-highBound, (brushSize-1)*res*2, res);
		hasChanged = true;
		return pA;
	}
	
	/**
	 * inRangeBrush determines whether the given point falls within the
	 * valid range from which drawing to the defenseField is allowed.
	 * A certain margin is accounted for if the "brush" is only partially
	 * outside the valid area.
	 * 
	 * @param x the x-coordinate to check
	 * @param y the y-coordinate to check
	 * @return true if this is a valid location, false if it is not
	 */
	public boolean inRangeBrush( int x, int y )
	{
		if ( y < (lowBound+brushSize*res) && y > (highBound-brushSize*res) )
			return true;
		return false;
	}
	
	/**
	 * hits determines whether the given x-y coordinate is filled with paint.
	 * 
	 * @param x the x-coordinate to check
	 * @param y the y-coordinate to check
	 * @return true if the point is filled; false if it is not
	 */
	public boolean hits( int x, int y )
	{
		if ( x >= 0 && x < pWidth && y < lowBound && y >= highBound && defenseArray[x/res][y/res - highBound/res] > 0)
			return true;
		return false;
	}

	/**
	 * eraseUnder is passed a RectangularShape object,
	 * and clears out the defenseArray underneath it.
	 * 
	 * @param r the shape to clear beneath
	 */
	public void eraseUnder(RectangularShape r)
	{
		int xMin = (int)r.getX();
		if (xMin<0)
			xMin = 0;
		
		int xMax = xMin + (int)r.getWidth();
		if (xMax >= pWidth)
			xMax = pWidth-1;
		
		int yMin = (int)r.getY();
		if (yMin < highBound)
			yMin = highBound;
			
		int yMax = yMin + (int)r.getHeight();
		if (yMax >= lowBound)
			yMax = lowBound-1;
		
		for( int i = xMin; i < xMax; i += res)
		{
			for (int j = yMin; j < yMax; j++)
			{
				if (r.contains(i,j))
					defenseArray[i/res][j/res - highBound/res] = 0;
			}
		}

		r.setFrame(r.getX(), r.getY()-highBound, r.getWidth(), r.getHeight());
		Composite c = imageGfx.getComposite();
		imageGfx.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
		imageGfx.fill( r );
		imageGfx.setComposite(c);
		hasChanged = true;
	}//end of eraseUnder

	
	/**
	 * clear empties the entire defenseArray.
	 */
	public void clear()
	{
		for (int i = 0; i < defenseArray.length; i++)
			Arrays.fill(defenseArray[i], 0);
		Composite c = imageGfx.getComposite();
		imageGfx.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
		imageGfx.fillRect(0, 0, imageMask.getWidth(), imageMask.getHeight());
		imageGfx.setComposite(c);
		hasChanged = true;
	}
	
	/**
	 * draw iterates through the defenseArray's 2D Array of integers,
	 * and fills a Rectangle to the screen for each space.
	 * Additionally, two lines are drawn to signify the borders of the
	 * valid drawing area for the user.
	 * 
	 * @param g the game's Graphics object, which manages game rendering
	 * @param doLines a boolean that determines whether the border lines
	 * 			should be drawn.
	 */
	public void draw(Graphics g, boolean doLines)
	{
		if (doLines)
		{
			g.setColor(Color.white);
			g.drawLine(0, highBound-1, pWidth, highBound-1);
			g.drawLine(0, lowBound, pWidth, lowBound);
		}
		
		if(hasChanged)
		{
			finalGfx.drawImage(defImage,0,0,null);
			Composite c = finalGfx.getComposite();
			finalGfx.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN, 1.0f));
			finalGfx.drawImage(imageMask, 0, 0, null);
			finalGfx.setComposite(c);
			hasChanged = false;
		}
		g.drawImage(finalImage, 0, highBound, null);
		
	}// end draw()
}
