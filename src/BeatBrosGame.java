/** BeatBrosGame.java, adapted from WormChase.java
 *
 * Anton Ridgway, February 2012, adapted from
 * Roger Mailler, January 2009, adapted from
 * Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th
 *
 * The player character moves around at the bottom of the
 * screen, controlled via WASD or the arrow keys.
 * 
 * The mouse is used to "draw" in the area above the
 * player, to defend him from incoming projectiles
 * and/or invertibrates.
 * 
 * Once the player is hit, the game is over, and a score is
 * displayed.

 -------------

 Uses full-screen exclusive mode, active rendering,
 and double buffering/page flipping.

 On-screen restart and quit buttons.

 Using Java 3D's timer: J3DTimer.getValue()
 *  nanosecs rather than millisecs for the period

 Average FPS / UPS
 20			50			80			100
 Win 98:         20/20       50/50       81/83       84/100
 Win 2000:       20/20       50/50       60/83       60/100
 Win XP (1):     20/20       50/50       74/83       76/100
 Win XP (2):     20/20       50/50       83/83       85/100

 Located in /SuperBeatBros
 */

import java.util.ArrayList;
import java.util.Random;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import entities.DefenseField;
import entities.ExplosionSprite;
import entities.MissileSprite;
import entities.NoteSprite;
import entities.PlayerSprite;
import entities.Wyrm;

import framework.GameMenu;
import framework.Ribbon;
import framework.RibbonsManager;
import framework.ScoreTable;
import image.ImagesLoader;
import sound.ClipsLoader;
import sound.MusicManager;

public class BeatBrosGame extends GameFrame
{

	private static final long serialVersionUID = -2450477630768116721L;

	private static int DEFAULT_FPS = 100;

	private int gameState = 0; //the game's state integer (0 = menu, 1 = in-game)
	private GameMenu gameMenu; //the object that manages the game's menu
	
	private final String IMS_INFO = "imsInfo.txt";
	private final String SNDS_FILE = "clipsInfo.txt";
	private final String[] soundNames = {"explo1","explo2","explo3","strum","wyrmcry"};

	// variables for use with font display
	private Font font;
	private FontMetrics metrics;
	private float goMessageOpacity = 0.0f;
	
	// the various loaders for game content
    private ImagesLoader imsLoader; 
    private ClipsLoader clipsLoader;
    private MusicManager musicMan;
	private RibbonsManager backRibs;
	private Ribbon statusBar;
	private final int baseMoveSize = 20;
	
	// declare mouse management variables
	private boolean mouseDrawing = false;
	
	// declare the in-game objects
	private PlayerSprite player; // the player
	private DefenseField myField; // the defense field
	private Wyrm stiney = null; // the attacking wyrm
	
	// ArrayLists to contain game objects with duplicates
	private ArrayList<MissileSprite> missiles = new ArrayList<MissileSprite>();
	private ArrayList<ExplosionSprite> explosions = new ArrayList<ExplosionSprite>();
	private ArrayList<NoteSprite> inkRefills = new ArrayList<NoteSprite>();

	//keep track of the status bar height
	private final int baseHeight = 60;
	
	//declare score-related variables
	private int score = 0;
	private int highScore;
	private boolean hsBroken = false;
	private int hsStringNum;
	private final String[] hsStrings = {"Awesome", "Funky", "Bodacious", "Gnarly", "Radical",
			"Tubular", "Cosmic", "Abundant", "Total"};
	private final int hsStringsLen = 9;
	private String playerName = "YOU!";
	private BufferedImage harmonica;
	
	private final int timeScore = 10;
	private final int wyrmScore = 5000;
	private final int inkScore = 300;
	
	//probability variables, used as "1 out of X"
	//probability that a missile will be aimed for the Player
	private int oddsOfAimed = 20;
	//probability that a new wyrm will appear in a given state update
	private int oddsOfWyrm = 1000000;
	
	//declare "paint" management variables
	private final int paintMax = 80000;
	private final int paintInit = 16000;
	private int paintAmount = paintInit;
	
	private Color blueColor = new Color(64,198,249);
	private Color darkBlueColor = new Color(6,86,234);
	private Color brownColor = new Color(90, 45, 0);
	
	/* Declare various time counter variables:
	 * 
	 * A final "init" variable is used for timer reset upon a game restart.
	 * A "between" variable is used to determine how many state updates
	 * 		should take place between item generation; this should vary
	 * 		as the game continues.
	 * A "timer" variable, which is the actual counter, adjusted in each
	 * 		state update.
	 */
	//Score Timer (time to increase player score)
	private final int stInit = 10;
	private int sTimer = stInit;
	//missileTimer (time to create a Missile object)
	private final int mtInit = 500;
	private int mtBetween = mtInit/4;
	private int mTimer = mtInit;
	//inkTimer (time to create an InkRefill object)
	private final int itInit = 150;
	private int itBetween = itInit;
	private int iTimer = itInit;
	//wormTimer (time to create a wyrm, if not generated randomly)
	private final int wtInit = 3000;
	private int wTimer = wtInit;
	//difficultyTimer (time to adjust difficulty settings)
	private final int dtInit = 100;
	private int dTimer = dtInit;
	//backgroundTimer (time to switch out the background)
	private final int NUM_BGS = 3;
	private final int btInit = 3000;
	private int bTimer = btInit;
	private int currBg = 0;

	// used by quit 'button'
	private volatile boolean isOverQuitButton = false;
	private Rectangle quitArea;

	// used by the restart 'button'
	private volatile boolean isOverRestartButton = false;
	private Rectangle restartArea;
	
    //private DecimalFormat df = new DecimalFormat("0.##");  // 2 dp


    /**
     * The BeatBrosGame constructor, which simply refers to the
     * constructor of the GameFrame class it extends.
     *  
     * @param period the period that determines the game's rendering
     * 			rate, as used in the GameFrame constructor.
     */
	public BeatBrosGame(long period)
	{
		super(period);
	}

	/**
	 * simpleInitialize creates the various game objects
	 * needed to start the game.
	 */
	@Override
	protected void simpleInitialize()
	{
		// set up message font
		font = new Font("Arial Narrow", Font.PLAIN, 24);
		metrics = this.getFontMetrics(font);

		// specify screen areas for the buttons
		restartArea = new Rectangle(pWidth - 100, pHeight - 45, 70, 15);
		quitArea = new Rectangle(pWidth - 100, pHeight - 20, 70, 15);
		
	    imsLoader = new ImagesLoader(IMS_INFO); 
	    clipsLoader = new ClipsLoader(SNDS_FILE);
	    backRibs = new RibbonsManager(0, pWidth, pHeight, baseMoveSize, imsLoader);
	    backRibs.moveLeft();
	    statusBar = new Ribbon(pWidth, pHeight, imsLoader.getImage( "road" ), 0, baseMoveSize );
	    statusBar.moveLeft();
	    
		//start game music
	    musicMan = new MusicManager();
	    musicMan.start();   // repeatedly play it

	    scoreTable = new ScoreTable();
		gameMenu = new GameMenu(pWidth, pHeight, scoreTable, musicMan, imsLoader);
	    highScore = scoreTable.getScore(0);
	    // set gameOverMessage adjective value
	    hsStringNum = new Random().nextInt(hsStringsLen);
	    harmonica = imsLoader.getImage("harmonica");
	    
		// create game components
		player = new PlayerSprite(pWidth, pHeight, baseHeight, imsLoader);
		player.loopImage( (int)(period/1000000L), 0.5);
		myField = new DefenseField(pWidth, pHeight, imsLoader);
	}

	/**
	 * mousePress overrides the method of the GameFrame,
	 * and provides functionality for each mouse click
	 * scenario, including clicking on in-game objects.
	 * It also uses a boolean to keep track of a held mouse
	 * button.
	 * 
	 * @param x the x-coordinate of the mousePress event
	 * @param y the y-coordinate of the mousePress event
	 */
	@Override
	protected void mousePress(int x, int y)
	{
		if (gameState == 1)
		{
	
			if (isOverRestartButton) //Restart Button
			{
				mouseDrawing = false;
				goMessageOpacity = 0.0f;				
				
				score = 0;
				hsBroken = false;
				hsStringNum = new Random().nextInt(hsStringsLen);
				paintAmount = paintInit;
				
				//reset all timers
				sTimer = stInit;
				// This fraction must coordinate with that in the variable's initialization
				mtBetween = mtInit/4;
				mTimer = mtInit;
				itBetween = itInit;
				iTimer = itInit;
				wTimer = wtInit;
				dTimer = dtInit;
				
				//reset all game objects
				myField.clear();
				player.reset();
				stiney = null;
				missiles.clear();
				explosions.clear();
				inkRefills.clear();
				gameOver = false;
			}
			else if (isOverQuitButton)// Quit Button
			{
				gameState = 0;
				player.releaseControls();
			}
			
			else// clicking on the play area
			{
				if (!gameOver)
				{
					//allow only one mouse action per click
					boolean mouseDone = false;
					
					for( int i = 0; i < inkRefills.size(); i++)
					{
						if( inkRefills.get(i).getMyRectangle().contains(mouseX, mouseY) )
						{
							collectNote(inkRefills.get(i));
							mouseDone = true;
						}
					}
					if (!mouseDone && stiney != null && stiney.nearHead(mouseX, mouseY))
					{
						stiney = null;
						score += wyrmScore;
						mouseDone = true;
					}
					
					// draw to the DefenseField, if the player has enough ink
					if (!mouseDone)
						mouseDrawing = true;
	
					//reset for the next click
					mouseDone = false;
				}
			}
		}
		else
		{
			int toDo = gameMenu.mouseClick( mouseX, mouseY );
			if (toDo == 1)
				running = false;
			else if (toDo == 2)
				gameState = 1;
			else if (toDo == 3)
				highScore = scoreTable.getScore(0);
		}
	} // end of testPress()
	
	/**
	 * mouseRelease sets mouseDrawing to false, to signify
	 * that a held mouse button has been released.
	 */
	@Override
	protected void mouseRelease()
	{
		mouseDrawing = false;
	}

	/**
	 * mouseMove keeps track of whether the mouse is
	 * over the restart or quit button.
	 * 
	 * @param x the x-coordinate of the mouseMove event
	 * @param y the y-coordinate of the mouseMove event
	 */
	@Override
	protected void mouseMove(int x, int y)
	{
		if (running)  // stops problems with a rapid move after pressing 'quit'
		{
			isOverRestartButton = restartArea.contains(x, y) ? true : false;
			isOverQuitButton = quitArea.contains(x, y) ? true : false;
		}
	}

	/**
	 * keyPressGame manages keyboard input not used to exit the
	 * game.  In this game, only the Player needs to be notified
	 * of key input.
	 * 
	 * @param num the number code to be passed to the Player object's
	 * 			keyInput method.
	 */
	@Override
	protected void keyPressGame(int num)
	{
		if (running && gameState == 1)
		{ // stops problems with a rapid move after pressing 'quit'
			player.keyInput(num);
		}
	}

	/**
	 * simpleRender renders each of the game objects.  The order
	 * of the calls in this method determines the z-depth of the game.
	 * 
	 * @param gScr the Graphics object to render to
	 */
	@Override
	protected void simpleRender(Graphics gScr) {

		//draw the background
		backRibs.display(gScr);
		
		// draw game elements		
		player.drawSprite(gScr);
		
		for ( int i = 0; i < inkRefills.size(); i++ )
			inkRefills.get(i).drawSprite(gScr);
		for ( int i = 0; i < missiles.size(); i++ )
			missiles.get(i).drawSprite(gScr);
		
		if (gameState == 0)
			myField.draw(gScr, false);
		else
			myField.draw(gScr, true);
		
		if (stiney != null)
			stiney.draw(gScr);
		
		for ( int i = 0; i < explosions.size(); i++ )
			explosions.get(i).drawSprite(gScr);
		
		if(gameState == 1)
		{
			gScr.setColor(Color.white);
			int brush = myField.getBrushSize();
			gScr.drawOval(mouseX-brush,mouseY-brush,2*brush,2*brush);
		}
		
		if (statusBar.isNullImage())
		{
			gScr.setColor(brownColor);
			gScr.fillRect(0, pHeight-baseHeight, pWidth, baseHeight);
		}
		else
			statusBar.display(gScr);
		
		if (gameOver)
			gameOverMessage(gScr);
		
		gScr.setFont(font);

		if(gameState == 1)
		{
		// report time used, score, and remaining paint at bottom left
		gScr.setColor( Color.white );
		gScr.drawString("Score: " + score, 20, pHeight - 35);
		gScr.drawString("High Score: " + highScore, 20, pHeight - 10);
		gScr.drawString("Energy: ", 230, pHeight - 20);
		
		gScr.setColor(Color.black);
		gScr.fillRect(300, pHeight-45, 200, 30);
		gScr.setColor(darkBlueColor);
		gScr.fillRect(302, pHeight-43, (int)(196*(((double)paintAmount)/paintMax)), 26);

		// draw the restart and quit 'buttons'
		drawButtons(gScr);
		}
		else
			gameMenu.displayMenu(gScr);
		
		//gScr.setColor(Color.blue);
	    // report frame count & average FPS and UPS at top left
		//gScr.drawString("Average FPS/UPS: " + df.format(averageFPS) + ", " +
	    //                          df.format(averageUPS), 20, 25);  // was (10,55)
		
		gScr.setColor(Color.black);
		

	} // end of simpleRender()

	/**
	 * drawButtons draws the game buttons (for Restart and
	 * Quit) to the screen.
	 * 
	 * @param g the Graphics object to draw to.
	 */
	private void drawButtons(Graphics g) {
		g.setColor(Color.black);

		// draw the restart 'button'
		if (isOverRestartButton)
			g.setColor(Color.white);

		g.drawOval(restartArea.x, restartArea.y, restartArea.width, restartArea.height);
		g.drawString("Restart", restartArea.x+4, restartArea.y + 10);
		
		if (isOverRestartButton)
			g.setColor(Color.black);

		// draw the quit 'button'
		if (isOverQuitButton)
			g.setColor(Color.white);

		g.drawOval(quitArea.x, quitArea.y, quitArea.width, quitArea.height);
		g.drawString("Menu", quitArea.x + 10, quitArea.y + 10);

		if (isOverQuitButton)
			g.setColor(Color.black);
	} // drawButtons()

	/**
	 * gameOverMessage draws an ending message to the
	 * screen when called.  Additional functionality is
	 * provided in the case of a high score.
	 */
	@Override
	protected void gameOverMessage(Graphics g)
	// center the game-over message in the panel
	{
		Graphics2D g2d = (Graphics2D)g;
		Composite c = g2d.getComposite(); // backup the old composite

		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				goMessageOpacity));

		String msg1 = "Game Over, Bro.";
		String msg2 =  "Your " + hsStrings[hsStringNum] + " Score: " + score;
		String msg3 =  "Sweet high score!";
		
		int x = (pWidth - metrics.stringWidth(msg1)) / 2;
		int y = (pHeight - metrics.getHeight()) / 2;
		
		if(harmonica != null)
		{
			g.drawImage(harmonica, (pWidth-harmonica.getWidth())/2, (pHeight-harmonica.getHeight())/2, null);
		}
		else
		{
			g.setColor(Color.black);
			g.fillRect(x-20, y-25, metrics.stringWidth(msg1)+40, 60);
		}
		
		g.setColor(blueColor);
		g.setFont(font);
		g.drawString(msg1, x, y);
		
		x = (pWidth - metrics.stringWidth(msg2)) / 2;
		g.drawString(msg2, x, y+25);
		
		if(hsBroken)
		{
			x = (pWidth - metrics.stringWidth(msg3)) / 2;
			
			g.setColor(Color.white);
			g.drawString(msg3, x, y+50);
		}
		
		goMessageOpacity += 0.05f;
		if (goMessageOpacity > 1.0 )
			goMessageOpacity = 1.0f;
		
		g2d.setComposite(c); //return the composite setting to normal
	} // end of gameOverMessage()
	
	/**
	 * collectNote adds the value of the current NoteSprite to the
	 * player's amount, if possible, and removes the NoteSprite
	 * from play.
	 * 
	 * @param i the NoteSprite to collect.
	 */
	public void collectNote( NoteSprite i )
	{
		paintAmount += i.getValue();
		if (paintAmount > paintMax)
			paintAmount = paintMax;
		inkRefills.remove(i);
		
		clipsLoader.play(soundNames[3], false);
		score += inkScore;
	}
	
	/**
	 * doPaint will update the defenseArray state each frame, according to
	 * varying states of the mouse.
	 */
	protected void doPaint()
	{
		boolean noDraw = false; //don't draw if something is in the way

		if( stiney!=null && (stiney.nearHead(mouseX, mouseY) || stiney.touchedAt(mouseX, mouseY)))
			noDraw = true;
		
		if(!noDraw)
		{
			//Calculate paint needed, so that only a whole dot can be drawn
			int paintNeeded = 4*myField.getBrushSize()*myField.getBrushSize()-4;
			
			 // was the click within the drawable area?
			if (mouseDrawing && paintAmount >= paintNeeded && myField.inRangeBrush(mouseX, mouseY))
			{
				paintAmount = myField.drawBrush(mouseX, mouseY, paintAmount);
			}
		}
	}// end of doPaint()
	
	
	/**
	 * moveAll calls the move() methods of each in-game object in turn.
	 * In consequence, this method determines the order that each object
	 * actually performs its movement calculations.
	 */
	protected void moveAll()
	{
		player.move();
		
		if (gameState == 1)
		{
			for ( int i = 0; i < inkRefills.size(); i++ )
			{
				if(inkRefills.get(i).isFinished())
					inkRefills.remove(i);
				else
				{
					inkRefills.get(i).updateSprite();
					if (inkRefills.get(i).getMyRectangle().intersects(player.getMyRectangle()))
						collectNote(inkRefills.get(i));
				}
			}
			for ( int i = 0; i < explosions.size(); i++ )
			{
				if(explosions.get(i).isFinished())
					explosions.remove(i);
				else
					explosions.get(i).updateSprite();
			}
			for ( int i = 0; i < missiles.size(); i++ )
			{
				if (missiles.get(i).isFinished())
				{
					clipsLoader.play( soundNames[new Random().nextInt(3)], false);
					missiles.remove(i);
				}
				else
					missiles.get(i).updateSprite();
			}
	
			if (stiney != null)
				stiney.move();
		}
		backRibs.update();
		statusBar.update();

	}//end of moveAll()
	
	
	/**
	 * generateStuff creates new objects for the game, according to the time
	 * counters of each, as well as to randomly generated integers.
	 */
	protected void generateStuff()
	{
		Random rand = new Random();
	//manage score
		if(sTimer > 0)
			--sTimer;
		else {
			score += timeScore;
			sTimer = stInit;
		}
		if (score > highScore) {
			highScore = score;
			hsBroken = true;
		}
		
	//manage missiles
		if(mTimer > 0)
			--mTimer;
		else {
			if (rand.nextInt(oddsOfAimed) == 0)
				missiles.add(new MissileSprite(pWidth, pHeight, baseHeight, baseMoveSize, 50, 100,
						explosions, myField, player, true, imsLoader, (int)(period/1000000L)));
			else
				missiles.add(new MissileSprite(pWidth, pHeight, baseHeight, baseMoveSize, 50, 100,
						explosions, myField, player, false, imsLoader, (int)(period/1000000L)));

			mTimer = mtBetween;
		}

	//manage ink
		if(iTimer > 0)
			--iTimer;
		else {
			inkRefills.add(new NoteSprite(pWidth,pHeight,pHeight-baseHeight,16000,myField,imsLoader));
			iTimer = itBetween;
		}
		
	//manage wyrm
		if(stiney == null) {
			if (rand.nextInt(oddsOfWyrm) == 0) {
				stiney = new Wyrm(pWidth, pHeight, pHeight-baseHeight, myField, imsLoader);
				wTimer = 3000;
				clipsLoader.play(soundNames[4], false);
			}
			else if( wTimer > 0 )
				--wTimer;
			else {
				stiney = new Wyrm(pWidth, pHeight, pHeight-baseHeight, myField, imsLoader);
				wTimer = 3000;
			}
		}

	//adjust "between" times and probabilities, to scale game difficulty with time
		if (dTimer > 0)
			--dTimer;
		else {
			if (mtBetween > 5)
				--mtBetween;
			if (oddsOfAimed > 3)
				--oddsOfAimed;
			
			if(itBetween < 300)
				++itBetween;
			
			if (oddsOfWyrm > 10000)
				oddsOfWyrm -= 500;
			dTimer = dtInit;
		}
		
		//keep track of time to change the background
		if(bTimer > 0)
			--bTimer;
		else
		{
			currBg = (currBg+1)%NUM_BGS;
			backRibs.switchTo(currBg, pWidth, pHeight, baseMoveSize, imsLoader);
			bTimer = btInit;
		}
	}

	/**
	 * simpleUpdate calls several methods to be performed on each state
	 * update, and ends the game if the Player has been hit.
	 */
	@Override
	protected void simpleUpdate()
	{
		if (gameState == 1)
			doPaint(); // Manage the screen painting
			
			moveAll(); // move() all the game objects
		
		if (gameState == 1)
		{
			if (player.isHit(explosions, stiney)) // Was the player hit?
			{
				gameOver = true;
				scoreTable.addEntry(score, playerName);
			}
			else
				generateStuff(); // Generate more game objects
		}
	} //end of simpleUpdate()

	/**
	 * The main method for the game; creates a MissileGame object
	 * to run the game.
	 *  
	 * @param args sets a non-default fps rate
	 */
	public static void main(String args[])
	{
		int fps = DEFAULT_FPS;
		if (args.length != 0)
			fps = Integer.parseInt(args[0]);

		long period = (long) 1000.0 / fps;
		System.out.println("fps: " + fps + "; period: " + period + " ms");
		new BeatBrosGame(period * 1000000L); // ms --> nanosecs
	} // end of main()

} // end of MissileGame class
