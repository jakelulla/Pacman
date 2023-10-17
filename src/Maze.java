import java.awt.Color;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Maze
{
   private int[][] maze;
   private int rows, cols, tileWidth, tileHeight;

   // constructor, scans in a file and converts it to a 2D array of integers
   public Maze(String fileName, int tileWidth, int tileHeight) throws IOException
   {
      ArrayList<ArrayList<Integer>> values = new ArrayList<ArrayList<Integer>>();
      int numrows = 0;
      int numcols = 0;
      try
      {
         BufferedReader br = new BufferedReader(new FileReader(fileName));
         String line = br.readLine();
         while (line != null)
         {
            values.add(new ArrayList<Integer>());
            ArrayList<Integer> columns = values.get(numrows);
            if (numrows == 0)
            {
               numcols = line.length();
            } 
            else if (line.length() != numcols)
            {
               throw new IOException("bad num of columns");
            }
            numrows++;
            for (int j = 0; j < line.length(); j++)
            {
               columns.add(Integer.parseInt(line.substring(j, j + 1)));
            }
            line = br.readLine();
         }
         br.close();
      } catch (FileNotFoundException e)
      {
         e.printStackTrace();
      }

      this.rows = numrows;
      this.cols = numcols;
      this.tileWidth = tileWidth;
      this.tileHeight = tileHeight;
      maze = new int[this.rows][this.cols];
      for (int row = 0; row < this.rows; ++row)
      {
         for (int col = 0; col < this.cols; ++col)
         {
            maze[row][col] = values.get(row).get(col);
         }
      }
   }

   // shortcut to fast-forward the maze for testing of each level
   public void ffMaze()
   {
      // gets rid of random dots, and leaves some for testing
      for (int i = 0; i < maze.length; i++)
      {
         for (int j = 0; j < maze[0].length; j++)
         {
            int empty = (int) (Math.random() * 100);
            // 1 out of 100 dots remain
            if (empty != 50 && maze[i][j] == 0)
               maze[i][j] = 2;
         }
      }
   }

   // draws the maze using the values in our array
   public void draw(Graphics2D g2)
   {
      for (int r = 0; r < maze.length; r++)
      {
         for (int c = 0; c < maze[r].length; c++)
         {
            int val = maze[r][c];
            switch (val)
            {

            // This is a dot value
            case 0:
               g2.setColor(Color.BLACK);
               g2.fillRect(c * tileWidth, r * tileHeight, tileWidth, tileHeight);
               g2.setColor(Color.ORANGE);
               g2.fillOval(c * tileWidth + 5, r * tileHeight + 5, 8, 8);
               break;

            // Blue wall value
            case 1:
               g2.setColor(Color.BLUE);
               g2.fillRect(c * tileWidth, r * tileHeight, tileWidth, tileHeight);
               break;

            // Empty path that pac-mac and ghosts can travel through
            case 2:
               g2.setColor(Color.BLACK);
               g2.fillRect(c * tileWidth, r * tileHeight, tileWidth, tileHeight);
               break;

            // Ghost house (where the die, and respawn)
            case 3:
               g2.setColor(Color.GRAY);
               g2.fillRect(c * tileWidth, r * tileHeight, tileWidth, tileHeight);
               break;

            // Power pellet
            case 4:
               g2.setColor(Color.BLACK);
               g2.fillRect(c * tileWidth, r * tileHeight, tileWidth, tileHeight);
               g2.setColor(Color.RED);
               g2.fillOval(c * tileWidth + 3, r * tileHeight + 3, 10, 10);
               break;

            // Exit out of the ghost house
            case 5:
               g2.setColor(Color.GRAY);
               g2.fillRect(c * tileWidth, r * tileHeight, tileWidth, tileHeight);
               break;

            // Dot, and path to the flip-side of the maze
            case 6:
               g2.setColor(Color.BLACK);
               g2.fillRect(c * tileWidth, r * tileHeight, tileWidth, tileHeight);
               g2.setColor(Color.ORANGE);
               g2.fillOval(c * tileWidth + 3, r * tileHeight + 3, 8, 8);
               break;
              
            // Empty Path to the flip-side of the maze
            case 8:
               g2.setColor(Color.BLACK);
               g2.fillRect(c * tileWidth, r * tileHeight, tileWidth, tileHeight);
               break;
               
            // Yellow block
            case 9:
               g2.setColor(Color.YELLOW);
               g2.fillRect(c * tileWidth, r * tileHeight, tileWidth, tileHeight);
               break;

            }

         }
      }
   }

   // getters and setters

   public int[][] getMaze()
   {
      return maze;
   }

   public void setMaze(int[][] maze)
   {
      this.maze = maze;
   }

   public int getRows()
   {
      return rows;
   }

   public void setRows(int rows)
   {
      this.rows = rows;
   }

   public int getCols()
   {
      return cols;
   }

   public void setCols(int cols)
   {
      this.cols = cols;
   }

   public int getTileWidth()
   {
      return tileWidth;
   }

   public void setTileWidth(int tileWidth)
   {
      this.tileWidth = tileWidth;
   }

   public int getTileHeight()
   {
      return tileHeight;
   }

   public void setTileHeight(int tileHeight)
   {
      this.tileHeight = tileHeight;
   }

   // sets a specified row and column to a value, used by pacman to switch between
   // a dot and an empty path
   public void setRC(int r, int c, int val)
   {
      maze[r][c] = val;
   }

   // checks if maze is empty and the level is won
   public boolean isEmpty()
   {
      for (int r = 0; r < maze.length; r++)
      {
         for (int c = 0; c < maze[0].length; c++)
         {
            if (maze[r][c] == 0)
               return false;
         }
      }
      return true;
   }

}