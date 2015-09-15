/**
 * GameMenu.java manages the display for the game's GUI menu.
 * by Anton Ridgway
 */

package framework;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import image.ImagesLoader;
import sound.MusicManager;

public class GameMenu {

	private int menuScreen = 0;
	private int pWidth;
	private int pHeight;
	private int keysX;
	private int keysY;
	private int staffX;
	private int staffY;

	private Rectangle startButton;
	private Rectangle musicButton;
	private Rectangle playPauseButton;
	private Rectangle prevButton;
	private Rectangle nextButton;
	private Rectangle scoreButton;
	private Rectangle scoreResetButton;
	private Rectangle storyButton;
	private Rectangle howToButton;
	private Rectangle creditsButton;
	private Rectangle quitButton;
	private Rectangle backButton;
	
	private BufferedImage title;
	private BufferedImage keys;
	private BufferedImage staff;
	private BufferedImage staffHowTo;
	private BufferedImage boombox;
	private BufferedImage back;
	private BufferedImage resetScores;
	
	private Font font;
	private Font smallFont;
	private ScoreTable scoreTable;
	private MusicManager musicMan;
	
	public GameMenu( int pW, int pH, ScoreTable sTable, MusicManager mMan, ImagesLoader imsLd )
	{
		pWidth = pW;
		pHeight = pH;
		int bWidth = 310;
		int bHeight = 73;
		
		startButton = new Rectangle( pWidth-bWidth-50, pHeight-bHeight*5-20, bWidth, bHeight );
		musicButton = new Rectangle( pWidth-250, pHeight-326, 200, 45 );
		playPauseButton = new Rectangle( (pWidth-800)/2+320, pHeight-207, 159, 83 );
		prevButton = new Rectangle( (pWidth-800)/2+156, pHeight-207, 160, 83 );
		nextButton = new Rectangle( (pWidth-800)/2+483, pHeight-207, 160, 83 );
		scoreButton = new Rectangle( pWidth-bWidth-50, pHeight-bHeight*4-20, bWidth, bHeight );
		scoreResetButton = new Rectangle((pWidth-250)/2, pHeight*3/4, 250, 80);
		storyButton = new Rectangle( pWidth-bWidth-50, pHeight-bHeight*3-20, bWidth, bHeight );
		howToButton = new Rectangle( pWidth-bWidth-50, pHeight-bHeight*2-20, bWidth, bHeight );
		creditsButton = new Rectangle( pWidth-250, pHeight-108, 209, 46 );
		quitButton = new Rectangle( pWidth-bWidth-50, pHeight-bHeight-20, bWidth, bHeight );
		backButton = new Rectangle(0, 0, 120, 240);
		
		font = new Font("Arial Narrow", Font.PLAIN, 24);
		smallFont = new Font("Arial Narrow", Font.BOLD, 20);
		scoreTable = sTable;
		musicMan = mMan;
		
		title = imsLd.getImage("title");
		keys = imsLd.getImage("keys");
		staff = imsLd.getImage("staff");
		staffHowTo = imsLd.getImage("staffhowto");
		boombox = imsLd.getImage("boombox");
		back = imsLd.getImage("back");
		resetScores = imsLd.getImage("resetScore");
		
		
		keysX = pWidth-keys.getWidth();
		keysY = pHeight-keys.getHeight();
		staffX = (pWidth-staff.getWidth())/2;
		staffY = (pHeight-staff.getHeight())/4;
	}
	
	/**
	 * mouseClick handles mouse click events while the menu state
	 * is active.
	 * 
	 * @param mX the mouse x-coordinate
	 * @param mY the mouse y-coordinate
	 * @return 1 if the game should exit
	 * 		   2 if the game state should change
	 * 		   3 if the high score has been reset
	 */
	public int mouseClick( int mX, int mY )
	{
		if ( menuScreen == 0 )
		{
			if (musicButton.contains(mX,mY))
			{
				// go to music options screen
				menuScreen = 4;
			}
			else if (creditsButton.contains(mX,mY))
			{
				// go to credits screen
				menuScreen = 5;
			}
			else if (startButton.contains(mX,mY))
			{
				// start the game
				return 2;
			}
			else if (scoreButton.contains(mX,mY))
			{
				// go to score screen
				menuScreen = 1;
			}
			else if (storyButton.contains(mX,mY))
			{
				// go to story screen
				menuScreen = 2;
			}
			else if (howToButton.contains(mX,mY))
			{
				// go to how-to screen
				menuScreen = 3;
			}
			else if (quitButton.contains(mX,mY))
			{
				// quit the game
				return 1;
			}
		}
		else if ( menuScreen == 1 )
		{
			if (scoreResetButton.contains(mX,mY))
			{
				scoreTable.resetScores();
				return 3;
			}
			else if(backButton.contains(mX,mY))
				menuScreen = 0;
		}
		else if ( menuScreen == 4 )
		{
			if (playPauseButton.contains(mX, mY) && musicMan.canClick())
				musicMan.pause();
			else if (prevButton.contains(mX,mY) && musicMan.canClick())
				musicMan.prev();
			else if (nextButton.contains(mX, mY) && musicMan.canClick())
				musicMan.next();
			else if(backButton.contains(mX,mY))
				menuScreen = 0;
		}
		else
		{
			if(backButton.contains(mX,mY))
				menuScreen = 0;
		}
		return 0;
	}

	/**
	 * displayMenu display's the various portions of the game menu,
	 * depending on a switch statement on the menuScreen integer.
	 * @param g the Graphics object to display to.
	 */
	public void displayMenu( Graphics g )
	{
		Graphics2D g2 = (Graphics2D)g;
		g2.setFont(font);

		switch(menuScreen)
		{
			case 0:
				if(keys != null)
					g2.drawImage(keys, keysX, keysY, null);
				else
				{
					g2.setColor(Color.black);
					g2.fillRect( pWidth-320, 0, 300, pHeight/2 );
					g2.setColor(Color.white);
					g2.fill(startButton);
					g2.fill(scoreButton);
					g2.fill(storyButton);
					g2.fill(howToButton);
					g2.fill(quitButton);
				}
				if ( title != null )
					g2.drawImage(title, keysX+10, keysY+40, null);
				else
				{
					g2.setColor(Color.red);
					g2.drawString("SUPER", keysX+30, keysY+20);
					g2.setColor(Color.white);
					g2.drawString("BEAT BROS!", keysY+20, keysY+50);
				}
				g2.setColor(Color.black);
				g2.drawString("Let's Boogie!", (int)startButton.getX()+10, (int)startButton.getY()+45);
				g2.drawString("High Scores", (int)scoreButton.getX()+10, (int)scoreButton.getY()+45);
				g2.drawString("Story", (int)storyButton.getX()+10, (int)storyButton.getY()+45);
				g2.drawString("How to Play", (int)howToButton.getX()+10, (int)howToButton.getY()+45);
				g2.drawString("Exit", (int)quitButton.getX()+10, (int)quitButton.getY()+45);
				g2.setColor(Color.white);
				g2.drawString("Audio", (int)musicButton.getX()+30, (int)musicButton.getY()+27);
				g2.drawString("Credits", (int)creditsButton.getX()+30, (int)creditsButton.getY()+34);
				break;
			case 1:
				if( staff != null )
					g2.drawImage(staff, staffX, staffY, null);
				else
				{
					g2.setColor(Color.black);
					g2.fillRect(75, 50, pWidth-150, pHeight*2/3);
				}
				
				if ( back != null )
					g2.drawImage(back, 0, 0, null);
				else
				{
					g2.setColor(Color.green);
					g2.fill(backButton);
					g2.setColor(Color.white);
					g2.drawString("BACK", 10, 30);
				}
				
				if (resetScores != null)
					g2.drawImage(resetScores, (int)scoreResetButton.getX(), (int)scoreResetButton.getY(), null);
				else
				{
					g2.setColor(Color.green);
					g2.fill(scoreResetButton);
					g2.setColor(Color.white);
					g2.drawString("Reset Scores", (int)scoreResetButton.getX()+60, (int)scoreResetButton.getY()+30);
				}
				
				g2.setColor(Color.black);
				g2.drawString("High Scores", staffX+300, staffY+30);
				for (int i = 0; i < 10; i++)
				{
					g2.drawString(scoreTable.getName(i), staffX+125, staffY+30*(i+3));
					g2.drawString(""+scoreTable.getScore(i), staffX+525, staffY+30*(i+3));
				}
				
				break;
			case 2:
				if( staff != null )
					g2.drawImage(staff, staffX, staffY, null);
				else
				{
					g2.setColor(Color.black);
					g2.fillRect(75, 50, pWidth-150, pHeight*2/3);
				}
				if ( back != null )
					g2.drawImage(back, 0, 0, null);
				else
				{
					g2.setColor(Color.green);
					g2.fill(backButton);
					g2.setColor(Color.white);
					g2.drawString("BACK", 10, 30);
				}
				g2.setColor(Color.black);
				g2.drawString("The Plot Thickens!", staffX+275, staffY+30);
				g2.drawString("The Super Beat Bros were totally rockin' out at the local", staffX+100, staffY+105);
				g2.drawString("awesome diner, when suddenly some bogus space aliens", staffX+110, staffY+135);
				g2.drawString("burst through the door, angered by their happenin' beats!", staffX+100, staffY+165);
				g2.drawString("But the Super Beat Bros were too stupendous for those", staffX+110, staffY+195);
				g2.drawString("corporate fat cats, and high-tailed it to their super awesome", staffX+100, staffY+225);
				g2.drawString("soul-powered Beatmobile!  The aliens were in hot pursuit,", staffX+110, staffY+255);
				g2.drawString("but the Super Beat Bros knew that they could hold them off", staffX+100, staffY+285);
				g2.drawString("with pure, funky musical energy! Aw yeah!", staffX+110, staffY+315);
				break;
			case 3:
				if( staff != null )
					g2.drawImage(staffHowTo, staffX, staffY, null);
				else
				{
					g2.setColor(Color.gray);
					g2.fillRect(75, 50, pWidth-150, pHeight*2/3);
				}
				if ( back != null )
					g2.drawImage(back, 0, 0, null);
				else
				{
					g2.setColor(Color.green);
					g2.fill(backButton);
					g2.setColor(Color.white);
					g2.drawString("BACK", 10, 30);
				}
				
				g2.setColor(Color.black);
				g2.drawString("How To Play", staffX+300, staffY+30);
				g2.drawString("Move around with WASD",  staffX+100, staffY+85);
				g2.drawString("or the arrow keys.",  staffX+150, staffY+115);
				g2.drawString("Don't get hit by incoming missiles!",  staffX+100, staffY+205);
				g2.drawString("Draw a musical energy field",  staffX+130, staffY+245);
				g2.drawString("to protect yourself.",  staffX+180, staffY+275);
				g2.drawString("Click on music notes to get more energy!",  staffX+100, staffY+340);
				g2.drawString("Click the head of the alien Wyrm",  staffX+120, staffY+380);
				g2.drawString("to send him packing!",  staffX+260, staffY+410);
				break;
			case 4:
				if ( back != null )
					g2.drawImage(back, 0, 0, null);
				else
				{
					g2.setColor(Color.green);
					g2.fill(backButton);
					g2.setColor(Color.white);
					g2.drawString("BACK", 10, 30);
				}
				
				if ( boombox != null )
					g2.drawImage(boombox, (pWidth-800)/2, pHeight-300, null);
				else
				{
					g2.setColor(Color.DARK_GRAY);
					g2.fillRect((pWidth-800)/2, pHeight-300, 800, 300);
					g2.setColor(Color.blue);
					g2.fillRect((pWidth-500)/2, pHeight-250, 500, 50);
					g2.setColor(Color.GRAY);
					g2.fill( prevButton );
					g2.fill( playPauseButton );
					g2.fill( nextButton );
					g2.setColor(Color.white);
					g2.drawString( "|<<", (int)prevButton.getX()+10, (int)prevButton.getY()+30 );
					g2.drawString( ">||", (int)playPauseButton.getX()+10, (int)playPauseButton.getY()+30 );
					g2.drawString( ">>|", (int)nextButton.getX()+10, (int)nextButton.getY()+30 );
				}

				g2.setColor(Color.white);
				g2.drawString( musicMan.getInfo(), (pWidth-800)/2+170, pHeight-250 );
				break;
			case 5:
				if( staff != null )
					g2.drawImage(staff, staffX, staffY, null);
				else
				{
					g2.setColor(Color.black);
					g2.fillRect(75, 50, pWidth-150, pHeight*2/3);
				}
				if ( back != null )
					g2.drawImage(back, 0, 0, null);
				else
				{
					g2.setColor(Color.green);
					g2.fill(backButton);
					g2.setColor(Color.white);
					g2.drawString("BACK", 10, 30);
				}
				g2.setColor(Color.black);
				g2.drawString("Credits", staffX+320, staffY+30);
				g2.drawString("Super Beat Bros. by Anton Ridgway, March-April 2012", staffX+120, staffY+60);
				g2.drawString("Based on code by Andrew Davison, and modified by Roger Mailler.", staffX+85, staffY+90);
				g2.setFont(smallFont);
				g2.drawString("\"Soulman\" copyright Isaac Hayes and David Porter", staffX+70, staffY+115);
				g2.drawString("\"Michigan Snuckey Exteriors\" copyright LucasArts", staffX+70, staffY+140);
				g2.drawString("\"Shake a Tail Feather\" copyright The Five Du-Tones", staffX+70, staffY+165);
				g2.drawString("\"I Got You (I Feel Good)\" copyright James Brown", staffX+70, staffY+190);
				g2.drawString("\"Highway Surfing\", copyright LucasArts", staffX+70, staffY+215);
				g2.drawString("\"Runaway Five Left the Building!\" copyright Nintendo", staffX+70, staffY+240);
				g2.drawString("\"New Age Retro Hippie\" copyright Nintendo", staffX+70, staffY+265);
				g2.drawString("\"Great Balls of Fire\" copyright Jerry Lee Lewis", staffX+70, staffY+290);
				g2.drawString("\"Johnny B. Goode\" copyright Chuck Berry", staffX+70, staffY+315);
				g2.drawString("\"Big Bad Boss\" copyright Nintendo", staffX+70, staffY+340);
				g2.drawString("\"Gravity Beetle Stage (Airfield Fort)\" copyright Capcom", staffX+70, staffY+365);
				g2.drawString("\"Ride of the Valkyries\" by Richard Wagner", staffX+70, staffY+390);
				g2.drawString("\"Shake, Rattle and Roll\" copyright Charles Calhoun", staffX+70, staffY+415);
				g2.drawString("\"Respect\" copyright Otis Redding", staffX+70, staffY+440);
				g2.drawString("Sound Effects from Andrew", staffX+440, staffY+440);
				g2.drawString("Davison and Freesound.org", staffX+440, staffY+455);
				g2.setFont(font);
				break;
		}
	}
}
