package framework;

// RibbonsManager.java
// Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th
// Adapted by Anton Ridgway, March 2012

/* RibbonsManager manages many ribbons (wraparound images 
   used for the game's background). 

   Ribbons 'further back' move slower than ones nearer the
   foreground of the game, creating a parallax distance effect.

   When a sprite is instructed to move left or right, the 
   sprite doesn't actually move, instead the ribbons move in
   the _opposite_direction (right or left).

*/

import image.ImagesLoader;

import java.awt.*;
import java.awt.image.BufferedImage;

public class RibbonsManager
{
  /* Background ribbons images, and their movement factors.
     Ribbons 'further back' are specified first in ribImages[], 
     and have smaller move factors so they will move slower.
  */
	
  private String ribImageSets[][] = {{"sky", "mountains", "trees", "signs"},
		  							 {"clearsky", "desertfar", "desertnear", "signs"},
		  							 {"clearsky", "radiotower", "trees", "signs"}};
  private int baseHeights[] = {60, 60, 60, 60};  // applied to moveSize
  private double moveFactors[] = {0.0, 0.01, 0.05, 1.0};  // applied to moveSize
     // a move factor of 0 would make a ribbon stationary

  private Ribbon[] ribbons;
  private int numRibbons;
  private int moveSize;
  // standard distance for a ribbon to 'move' each tick
  
  private Ribbon[] switchRibbons;
  private BufferedImage switchOverlay;
  private Graphics2D overlayGfx;
  int switchingx;
  int switchingv;
  int pWidth;
  int pHeight;


  public RibbonsManager(int set, int w, int h, int baseMvSz, ImagesLoader imsLd)
  {
	pWidth = w;
	pHeight = h;
    moveSize = baseMvSz;

    numRibbons = ribImageSets[set].length;
    ribbons = new Ribbon[numRibbons];

    for (int i = 0; i < numRibbons; i++)
    	ribbons[i] = new Ribbon(w, h, imsLd.getImage( ribImageSets[set][i] ), baseHeights[i],
						(moveFactors[i]*moveSize) );

    switchOverlay = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    overlayGfx = switchOverlay.createGraphics();
  }  // end of RibbonsManager()

  public int getNumBgs()
  {
	  return ribImageSets.length;
  }

  public void moveRight()
  { for (int i=0; i < numRibbons; i++)
      ribbons[i].moveRight();
  }

  public void moveLeft()
  { for (int i=0; i < numRibbons; i++)
      ribbons[i].moveLeft();
  }

  public void stayStill()
  { for (int i=0; i < numRibbons; i++)
      ribbons[i].stayStill();
  }
  
  public void switchTo( int set, int w, int h, int baseMvSz, ImagesLoader imsLd )
  {
	  switchRibbons = new Ribbon[ribImageSets[set].length];
	  for( int i = 0; i < switchRibbons.length; i++ )
	  {
	    	switchRibbons[i] = new Ribbon(w, h, imsLd.getImage( ribImageSets[set][i] ), baseHeights[i],
					(moveFactors[i]*moveSize) );
	    	switchRibbons[i].moveLeft();
	  }
	  switchingx = w;
	  switchingv = w/50;
  }


  public void update()
  {
	  for (int i=0; i < numRibbons; i++)
		  ribbons[i].update();
	  if (switchRibbons != null)
	  {
		  for( int i = 0; i < switchRibbons.length; i++ )
		  {
			  switchRibbons[i].update();
		  }
		  switchingx -= switchingv;
		  if(switchingx < 0)
		  {
			  ribbons = switchRibbons;
			  switchRibbons = null;
		  }
	  }
  }

  public void display(Graphics g)
  /* The display order is important.
     Display ribbons from the back to the front of the scene. */
  {
	  for (int i=0; i < numRibbons; i++)
		  ribbons[i].display(g);
	  if (switchRibbons != null)
	  {
		  for (int i=0; i < switchRibbons.length; i++)
			  switchRibbons[i].display(overlayGfx);
		  g.drawImage(switchOverlay, switchingx, 0, pWidth, pHeight, switchingx, 0, pWidth, pHeight, null);
	  }
  }

} // end of RibbonsManager

