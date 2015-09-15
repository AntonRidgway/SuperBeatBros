/**
 * ScoreTable.java manages the game's high score table.
 * It also takes care of the I/O involved in saving the
 * scores while the game is off.
 * 
 * by Anton Ridgway, March 2012
 */

package framework;

import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class ScoreTable
{
	final int[] defScores = {30000, 27000, 25000, 22500, 20000, 17000, 15000, 10000, 5000, 7};
	final String[] defNames = {"Dr. Mailler", "Blinky", "Pinky", "Inky", "Clyde", "Osdo", "Kurt", "Hank", "Frank", "Lennie"};
	int[] scores = {0,0,0,0,0,0,0,0,0,0};
	String[] names = {"","","","","","","","","",""};
	
	public ScoreTable()
	{
		try
		{
			File table = new File("hscores.sco");
			Scanner scan = new Scanner(table);
			for (int i = 0; (i < 10 && scan.hasNextLine()); i++)
			{
				scores[i] = scan.nextInt();
				scan.nextLine();
				names[i] = scan.nextLine();
			}
			scan.close();
		}
		catch (FileNotFoundException e)
		{
			System.out.println("File Not Found Exception: "+e+"\nResetting scores to default.");
			resetScores();
		}
	}
	
	/**
	 * resetScores resets the high score table values to default.
	 */
	public void resetScores()
	{
		scores = Arrays.copyOf(defScores, 10);
		names = Arrays.copyOf(defNames, 10);
	}
	
	/**
	 * saveScores saves the high scores to a file.
	 */
	public void saveScores()
	{
		try
		{
		File old = new File("hscores.sco");
		old.delete();
		
		File save = new File("hscores.sco");
		save.createNewFile();
		FileWriter fw = new FileWriter(save);
		for (int i = 0; i < scores.length; i++)
			fw.write(scores[i] + "\n" + names[i] + "\n");
		fw.close();
		}
		catch( IOException e )
		{
			System.out.println("IO Exception: "+e+"\nCould not save high scores table.");
		}
	}
	
	/**
	 * addEntry adds an entry to the high scores table.
	 * @param score the score to add
	 * @param name the associated name
	 */
	public void addEntry( int score, String name )
	{
		int i = 0;
		while (i < 10 && scores[i] > score)
			i++;
		if (i < 10)
		{
			for (int j = 9; j > i; j--)
			{
				scores[j] = scores[j-1];
				names[j] = names[j-1];
			}
			scores[i] = score;
			names[i] = name;
		}
	}
	
	/**
	 * getName returns the name at the given position
	 * 
	 * @param i the position of the name
	 * @return names[i] the name
	 */
	public String getName( int i )
	{
		return names[i];
	}
	
	/**
	 * getScore returns the score at the given position
	 * 
	 * @param i the position of the score
	 * @return scores[i] the score
	 */
	public int getScore( int i )
	{
		return scores[i];
	}
}
