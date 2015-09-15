package sound;

import java.util.Random;
import sound.MidisLoader;

/**
 * MusicManager contains a hard-coded list of midi file names
 * and descriptions, and provides controls to play them, via
 * Davison's MidisLoader class.
 * @author Anton Ridgway
 */

public class MusicManager
{
	private MidisLoader midisLoader;
	private final int TRACKS_SIZE = 14;
	private int currMus;
	private final String[] callNames = {"soul", "snuckeys", "feather", "igotyou", "highway",
			"runaway", "hippie", "fire", "johnny", "boss", "gravity", "valkyries", "shake", "respect"};
	private final String[] trackInfo = {"Soul Man", "Michigan Snuckey Exteriors", "Shake a Tail Feather",
			"I Got You (I Feel Good)", "Highway Surfing", "Runaway Five Left the Building!",
			"New Age Retro Hippie", "Great Balls of Fire",	"Johnny B. Goode", "Big Bad Boss",
			"Gravity Beetle Stage (Airfield Fort)",	"Ride of the Valkyries", "Shake, Rattle and Roll",
			"Respect"};
	
	private boolean canClick =  true;
	private boolean isPaused = false;
		
	public MusicManager()
	{
		currMus = new Random().nextInt(TRACKS_SIZE);
		midisLoader = new MidisLoader();
		midisLoader.setWatcher( new SoundsWatcher()
		{
			public void atSequenceEnd( String s, int i )
			{
				if(canClick)
				{
					currMus = (currMus+1)%TRACKS_SIZE;
					
					//pause until the stop message has gone through and
					//the new midi sequence can start.
					canClick = false; // keep the player from calling next again while sleeping
					try {
						Thread.sleep(500);
					}
					catch (InterruptedException e) {
						System.out.println("Track may not switch properly--InterruptedException: " + e);
					}
					canClick = true;
					start();
				}
			}
		});
		
	    midisLoader.load("boss", "boss.mid");
	    midisLoader.load("feather", "feather.mid");
	    midisLoader.load("fire", "fire.mid");
	    midisLoader.load("gravity", "gravity.mid");
	    midisLoader.load("highway", "highway.mid");
	    midisLoader.load("hippie", "hippie.mid");
	    midisLoader.load("igotyou", "igotyou.mid");
	    midisLoader.load("johnny", "johnny.mid");
	    midisLoader.load("runaway", "runaway.mid");
	    midisLoader.load("snuckeys", "snuckeys.mid");
		midisLoader.load("soul", "soulman.mid");
	    midisLoader.load("valkyries", "valkyries.mid");
	    midisLoader.load("shake", "shake.mid");
		midisLoader.load("respect", "respect.mid");
	}
	
	public void start()
	{
		midisLoader.play(callNames[currMus], false);
	}
	public void stop()
	{
		midisLoader.stop();
	}
	public void pause()
	{
		if (!isPaused)
		{
			midisLoader.pause();
			isPaused = true;
		}
		else
		{
			isPaused = false;
			midisLoader.resume();
		}
	}
	public void next()
	{
		stop();
		currMus = (currMus+1)%TRACKS_SIZE;
		
		//pause until the stop message has gone through and
		//the new midi sequence can start.
		canClick = false; // keep the player from calling next again while sleeping
		try {
			Thread.sleep(500);
		}
		catch (InterruptedException e) {
			System.out.println("Track may not switch properly--InterruptedException: " + e);
		}
		canClick = true;
		
		start();
	}
	public void prev()
	{
		stop();
		currMus = (currMus-1+TRACKS_SIZE)%TRACKS_SIZE;
		
		//pause until the stop message has gone through and
		//the new midi sequence can start.
		canClick = false; // keep the player from calling next again while sleeping
		try {
			Thread.sleep(500);
		}
		catch (InterruptedException e) {
			System.out.println("Track may not switch properly--InterruptedException: " + e);
		}
		canClick = true;
		
		start();

	}
	public String getInfo()
	{
		return trackInfo[currMus];
	}
	public boolean canClick()
	{
		return canClick;
	}

}
