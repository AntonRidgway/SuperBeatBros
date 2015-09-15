/**
 * Wyrm.java, adapted from Worm.java
 * Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th
 * Modified by Anton Ridgway, February 2012
 *
 * Contains the worm's internal data structure (a circular buffer)
 * and code for deciding on the position and compass direction
 * of the next worm move.  Additionally, the worm will "eat" a
 * given defenseField, putting holes in it as he collides with it.
 * 
 * [Much of the sophistication of the original has been removed to
 * make the worm more likely to head straight.]
 * 
 * Note that in this version, each Point2D represents the center of
 * a portion of the worm, rather than the upper-left corner.
 */
package entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import image.ImagesLoader;


public class Wyrm
{
	  // size and number of dots in the wyrm
	  private static final int DOTSIZE = 20;
	  private static final int RADIUS = DOTSIZE/2;
	  private static final int MAXPOINTS = 12;
	
	  // compass direction/bearing constants
	  private static final int NUM_DIRS = 8;
	  private static final int N = 0;  // north, etc going clockwise
	  private static final int NE = 1;
	  private static final int E = 2;
	  private static final int SE = 3;
	  private static final int S = 4;
	  private static final int SW = 5;
	  private static final int W = 6;
	  private static final int NW = 7;
	
	  private int currCompass;  // stores the current compass dir/bearing
	  
	  // Stores the increments in each of the compass dirs.
	  // An increment is added to the old head position to get the
	  // new position.
	  Point2D.Double incrs[];
	
	  // Probability info for selecting a compass dir.
	  private static final int NUM_PROBS = 10;
	  private int probsForOffset[];
	
	  // cells[] stores the dots making up the wyrm
	  // it is treated like a circular buffer
	  private Point2D cells[];
	  private int nPoints;
	  private int tailPosn, headPosn;   // the tail and head of the buffer
	
	  private int pWidth, pHeight;   // panel dimensions
	  private int yBase;			// y-value of status bar
	  private DefenseField field;	// defense field to eat
	
	  //Slow down the Wyrm a bit by moving only every few frames
	  private int mtInit = 1;
	  private int moveTimer = mtInit;
	  
	  private BufferedImage wormHead;
	  private BufferedImage wormBody;
	  private BufferedImage wormTail;
	  
	 /**
	  * The constructor for the Wyrm class.
	  * 
	  * @param pW the screen width, for reference
	  * @param pH the screen height, for reference
	  * @param yB the y-coordinate of the status bar, for reference
	  * @param df the defenseField, for the Wyrm to "eat"
	  */
	  public Wyrm(int pW, int pH, int yB, DefenseField df, ImagesLoader imsLd)
	  {
	    pWidth = pW; pHeight = pH; yBase = yB;
	    field = df;
	    cells = new Point2D[MAXPOINTS];   // initialise buffer
	    
		nPoints = 0;
		headPosn = -1;  tailPosn = -1; //first cell at array location 0
		
		// increments for each compass dir
		incrs = new Point2D.Double[NUM_DIRS];
		incrs[N] = new Point2D.Double(0.0, -1.0);
		incrs[NE] = new Point2D.Double(0.7, -0.7);
		incrs[E] = new Point2D.Double(1.0, 0.0);
		incrs[SE] = new Point2D.Double(0.7, 0.7);
		incrs[S] = new Point2D.Double(0.0, 1.0);
		incrs[SW] = new Point2D.Double(-0.7, 0.7);
		incrs[W] = new Point2D.Double(-1.0, 0.0);
		incrs[NW] = new Point2D.Double(-0.7, -0.7);
		
		// probability info for selecting a compass dir.
		//    Makes use of predefined compass directions.
		/* The array means that usually the wyrm continues in
		   the same direction but may bear slightly to the left
		   or right. */
		probsForOffset = new int[NUM_PROBS];
		probsForOffset[0] = 0;  probsForOffset[1] = 0;
		probsForOffset[2] = 1;  probsForOffset[3] = 1;
		probsForOffset[4] = 1;  probsForOffset[5] = -1;
		probsForOffset[6] = -1;  probsForOffset[7] = -1;
		probsForOffset[8] = 2;  probsForOffset[9] = -2;
		
		wormHead = imsLd.getImage("wormhead");
		wormBody = imsLd.getImage("wormbody");
		wormTail = imsLd.getImage("wormtail");

	  } // end of Wyrm()
	
	  /**
	   * getHead returns the Point2D coordinates of the Wyrm's head; it is called
	   * in Player to determine if the Player has touched the Wyrm's head.
	   * 
	   * @return cells[headPosn] if there is a valid head created; otherwise,
	   * 			a default "junk" value of -1,-1 is returned.
	   */
	  public Point2D getHead()
	  {
		  if (headPosn >= 0)
			  return cells[headPosn];
		  return new Point2D.Double(-1,-1);
	  }
	  
	  /**
	   * nearHead determines whether the given coordinates are within
	   * a box surrounding the Wyrm's head
	   * 
	   * @param x the x-coordinate to check
	   * @param y the y-coordinate to check
	   * @return whether there coordinates are within the bounding box
	   */
	  public boolean nearHead(int x, int y)
	  // is (x,y) near the wyrm's head?
	  {
		  if (nPoints > 0)
		  {
			  if( (Math.abs( cells[headPosn].getX() - x) <= DOTSIZE) &&
			       (Math.abs( cells[headPosn].getY() - y) <= DOTSIZE) )
				  return true;
	      }
	    return false;
	  } // end of nearHead()
	
	  /**
	   * touchedAt determines whether the given coordinates touch any
	   * part of the Wyrm's body, using square bounding boxes.
	   * 
	   * @param x the x-coordinate to check
	   * @param y the y-coordinate to check
	   * @return whether the coordinates do touch
	   */
	  public boolean touchedAt(int x, int y)
	  // is (x,y) near any part of the wyrm's body?
	  {
		  int i = tailPosn;
		  		while (i != headPosn)
		  		{
		  			if( (Math.abs( cells[i].getX() - x) <= RADIUS) &&
		  				(Math.abs( cells[i].getY() - y) <= RADIUS) )
		  			return true;
		  			i = (i+1) % MAXPOINTS;
		  		}
		  		return false;
	  }  // end of touchedAt()
	
	
	  /**
	   * A move causes the addition of a new dot to the front of
	   * the wyrm, which becomes its new head. A dot has a position
	   * and compass direction, which is derived from the
	   * position and bearing of the old head.
	   *
	   * In this application, move() must deal with three cases:
	   *   * when the wyrm is first created
	   *   * when the wyrm is growing
	   *   * when the wyrm is MAXPOINTS long (then the addition
	   *     of a new head must be balanced by the removal of a
	   *     tail dot)
	   */
	  public void move()
	  {
		  if(moveTimer > 0)
			  --moveTimer;
		  else
		  {
			    int prevPosn = headPosn;  // save old head posn while creating new one
		    headPosn = (headPosn + 1) % MAXPOINTS; //first time, starts at 0
		
		    if (nPoints == 0) //if empty, make the first point near the top of the screen
		    {   // empty array at start
		    	tailPosn = headPosn;
		    	cells[headPosn] = new Point2D.Double( (Math.random()*(pWidth-DOTSIZE))+RADIUS, RADIUS );
		    	nPoints++;
		    }
		    else if (nPoints == MAXPOINTS)     // array is full
		    {
		    	tailPosn = (tailPosn + 1) % MAXPOINTS;    // forget last tail
		    	newHead(prevPosn);
		    }
		    else	     // still room in cells[]
			    {
			    	newHead(prevPosn);
			    	nPoints++;
			    }
			    moveTimer = mtInit;
		  }
	
	  }  // end of move()
	
	  /**
	   * newHead creates a new head position and compass direction.
	   *
	   * This has two main parts. Initially we try to generate
	   * a head by varying the old position/bearing. But if
	   * the new head hits an obstacle, then we shift
	   * to a second phase. 
	   *
	   * In the second phase we try a head which is 90 degrees
	   * clockwise, 90 degress clockwise, or 180 degrees reversed
	   * so that the obstacle is avoided. These bearings are 
	   * stored in fixedOffs[].
	   * 
	   * @param prevPosn the previous head location, to move away from
	   */
	  private void newHead(int prevPosn)
	  {
	    Point2D newPt;
	    int fixedOffs[] = {2, -2, 3, -3, -4};  // offsets to avoid an obstacle
		boolean success = true;
		
		int newBearing = varyBearing();
		newPt = nextPoint(prevPosn, newBearing);
		  // Get a new position based on a semi-random
		  // variation of the current position.
		
		if (field.hits((int)newPt.getX(), (int)newPt.getY()) || newPt.getY() > yBase)
		{
			field.eraseUnder(new Ellipse2D.Double((int)newPt.getX()-RADIUS-1, (int)newPt.getY()-RADIUS-1, DOTSIZE+2, DOTSIZE+2));
			//new Ellipse2D.Double(cells[prevPosn].getX() + incrs[newBearing].getX()*.75,
			//cells[prevPosn].getY() + incrs[newBearing].getY()*.75,DOTSIZE,DOTSIZE)
			
			for (int i=0; i < fixedOffs.length; i++)
			{
				//don't repeat the same direction already tried
				if (fixedOffs[i] != newBearing)
				{
					newPt = nextPoint(prevPosn, calcBearing(fixedOffs[i]));
					if (!field.hits((int)newPt.getX(), (int)newPt.getY()) && newPt.getY() <= yBase)
						break;     // one of the fixed offsets will work
					
					//If the Wyrm bumps into the field, it will chew it up.
					field.eraseUnder(new Ellipse2D.Double((int)newPt.getX()-RADIUS-1, (int)newPt.getY()-RADIUS-1, DOTSIZE+2, DOTSIZE+2));
				}
			}
			success = false;
		}
		if (success)
			cells[headPosn] = newPt;     // new head position
		else
			cells[headPosn] = cells[prevPosn];     // if trapped, stay put
	  }  // end of newHead()
	
	  /**
	   * varyBearing varies the compass bearing semi-randomly 
	   *
	   * @return
	   */
	  private int varyBearing()
	  {
		int newOffset;
		newOffset = probsForOffset[(int)(Math.random()*NUM_PROBS)];
	    return calcBearing( newOffset );
	  }  // end varyBearing()
	
	  /**
	   *  Use the offset to calculate a new compass bearing based
	   *  on the current compass direction and an offset.
	   *  
	   * @param offset the compass offset to use
	   * @return turn the compass direction
	   */
	  private int calcBearing(int offset)
	  {
	    int turn = currCompass + offset;
	    // ensure that turn is between N to NW (0 to 7)
	    if (turn >= NUM_DIRS)
	      turn = turn - NUM_DIRS;
	    else if (turn < 0)
	      turn = NUM_DIRS + turn;
	    return turn;
	  }  // end of calcBearing()
	  
	  /**
	   * nextPoint returns the next coordinate based on the previous
	   * position and a compass direction.
	   * 
	   * Convert the direction into predetermined increments (stored
	   * in incrs[]). Add the increments multiplied by the DOTSIZE
	   * to the old head position.  Collision with the sides of the
	   * screen is also accounted for.
	   */
	  private Point2D nextPoint(int prevPosn, int bearing)
	  { 
			// get the increments for the compass bearing
		Point2D.Double incr = incrs[bearing];
		
		int newX = (int)cells[prevPosn].getX() + (int)(DOTSIZE * incr.getX());
		int newY = (int)cells[prevPosn].getY() + (int)(DOTSIZE * incr.getY());
		
		// modify newX/newY if they lead over the edge the screen; make the wyrm bounce
			if (newX < 0)
			{
				currCompass = calcBearing(-3); //turn around
				newX = RADIUS;
			}
			else if (newX > pWidth)
			{
				currCompass = calcBearing(3);
				newX = pWidth-RADIUS;
			}

			if ( newY < 0 )
			{
				currCompass = calcBearing(4);
				newY = RADIUS;
			}
			else if ( newY > yBase )
			{
				currCompass = calcBearing(-3);
				newY = pHeight - RADIUS;
			}
			
			return new Point2D.Double(newX,newY);
	  }  // end of nextPoint()
	
	/**
	 * draw draws the Wyrm, with the given Graphics object g
	 * 
	 * @param g the Graphics object to use.
	 */
	public void draw(Graphics g)
	{
		if (nPoints > 0)
		{
			g.setColor(new Color(70,90,60));
			int i = tailPosn;

			if (i != headPosn)
			{
				if (wormTail != null)
					g.drawImage(rotateImage(wormTail), (int) cells[i].getX()-wormTail.getWidth()/2, (int) cells[i].getY()-wormTail.getHeight()/2, null);
				else
					g.fillOval((int)cells[i].getX()-RADIUS, (int)cells[i].getY()-RADIUS, DOTSIZE, DOTSIZE);
				i = (i+1) % MAXPOINTS;
			}
			
			while (i != headPosn)
			{
				if (wormBody != null)
					g.drawImage(wormBody, (int) cells[i].getX()-wormBody.getWidth()/2, (int) cells[i].getY()-wormBody.getHeight()/2, null);
				else
					g.fillOval((int)cells[i].getX()-RADIUS, (int)cells[i].getY()-RADIUS, DOTSIZE, DOTSIZE);
				i = (i+1) % MAXPOINTS;
			}
			
			if (wormHead != null)
				g.drawImage(rotateImage(wormHead), (int) cells[headPosn].getX()-wormHead.getWidth()/2, (int) cells[headPosn].getY()-wormHead.getHeight()/2, null);
			else
			{
				g.setColor(new Color(120,180,110));
				g.fillOval( (int) cells[headPosn].getX()-RADIUS, (int) cells[headPosn].getY()-RADIUS, DOTSIZE, DOTSIZE);
			}
			
		}
	}  // end of draw()
	
	
	/**
	 * rotateImage rotates a given image, with an AffineTransform, and returns it.
	 *  
	 * @param src the source image
	 * @return dest the result image
	 */
	public BufferedImage rotateImage( BufferedImage src )
	{
		int angle = currCompass*45;
		int transparency = src.getColorModel().getTransparency();
		BufferedImage dest = new BufferedImage(src.getWidth(), src
				.getHeight(), transparency);
		Graphics2D g2d = dest.createGraphics();

		AffineTransform origAT = g2d.getTransform(); // save original transform

		// rotate the coord. system of the dest. image around its center
		AffineTransform rot = new AffineTransform();
		rot.rotate(Math.toRadians(angle), src.getWidth() / 2,
				src.getHeight() / 2);
		g2d.transform(rot);

		g2d.drawImage(src, 0, 0, null); // copy in the image

		g2d.setTransform(origAT); // restore original transform
		
		return dest;
	}

}  // end of Wyrm class
