import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;

public class PacMan
{
   private int row, col;
   private int w, h;
   private int upKey, downKey, leftKey, rightKey;
   // if chase = true, ghosts are vulnerable
   private boolean chase;
   // open mouth image or closed mouth image
   private boolean open, closed;
   // timer for pacman chasing ghosts, chaseTime variable used to reset chaseTimer
   private int chaseTimer, chaseTime;
   // timer used to switch image between open and closed
   private int switchImageTimer;
   private Clip pacEat, pacPowerUp;

   enum Direction
   {
      STOPPED, UP, DOWN, LEFT, RIGHT
   };

   // direction is current direction
   // nextDirection was used to make it less difficult to time turns in the maze
   private Direction direction = Direction.STOPPED;
   private Direction nextDirection = Direction.STOPPED;
   // counter used to limit the time a turn can be pre-set for
   private int presetTurnCounter;
   private int points;

   public PacMan(int row, int col, int w, int h, int upKey, int downKey, int leftKey, int rightKey)
   {
      this.row = row;
      this.col = col;
      this.w = w;
      this.h = h;
      this.upKey = upKey;
      this.downKey = downKey;
      this.leftKey = leftKey;
      this.rightKey = rightKey;
      presetTurnCounter = 0;
      chase = false;
      chaseTime = 30;
      chaseTimer = chaseTime;
      open = true;
      switchImageTimer = 0;
      direction = Direction.RIGHT;
   }

   // checks if a given direction is a valid move in the maze
   public boolean isValid(Direction direction, Maze maze)
   {
      switch (direction)
      {
      case UP:
         if (row == 0)
            return false;
         return maze.getMaze()[row - 1][col] % 2 == 0;
      case DOWN:
         if (row == maze.getMaze().length - 1)
            return false;
         return maze.getMaze()[row + 1][col] % 2 == 0;
      case LEFT:
         if (col == 0)
            return false;
         return maze.getMaze()[row][col - 1] % 2 == 0;
      case RIGHT:
         if (col == maze.getMaze()[0].length - 1)
            return false;
         return maze.getMaze()[row][col + 1] % 2 == 0;
      }
      return false;
   }

   public void update(Maze maze)
   {
      if (pacEat.getMicrosecondPosition() == pacEat.getMicrosecondLength())
         pacEat.setMicrosecondPosition(0);
      if (pacPowerUp.getMicrosecondPosition() == pacPowerUp.getMicrosecondLength())
         pacPowerUp.setMicrosecondPosition(0);
      if (chase)
      {
         if (pacPowerUp.getMicrosecondPosition() == 0)
            pacPowerUp.start();
         chaseTimer--;
      } 
      else
      {
         pacPowerUp.setMicrosecondPosition(pacPowerUp.getMicrosecondLength());
      }
      switchImageTimer++;
      if (closed)
      {
         open = true;
         closed = false;
      } 
      else if (open)
      {
         closed = true;
         open = false;
      }
      if (chaseTimer == 0)
      {
         chase = false;
         chaseTimer = chaseTime;
      }
      
      
      presetTurnCounter++;
      if (presetTurnCounter < 3 && isValid(nextDirection, maze))
      {
         direction = nextDirection;
      }
      // switches a dot to a empty path
      if (maze.getMaze()[row][col] == 0)
      {
         if (pacEat.getMicrosecondPosition() == 0 && !chase)
            pacEat.start();
         maze.setRC(row, col, 2);
         points += 10;
      }
      // switches a power-up to an empty path, and makes the ghosts vulnerable
      if (maze.getMaze()[row][col] == 4)
      {
         maze.setRC(row, col, 2);
         points += 50;
         chase = true;
      }
      // Dot, and path to the flip-side of the maze to empty path and path to
      // flip-side of maze
      if (maze.getMaze()[row][col] == 6)
      {
         maze.setRC(row, col, 8);
         points += 10;
      }
      switch (direction)
      {
      case UP:
         if (row == 0)
         {
            // allows pacMan to travel to flip-side of maze at cut-throughs
            if (maze.getMaze()[row][col] == 6 || maze.getMaze()[row][col] == 8)
            {
               if (maze.getMaze()[maze.getMaze().length - 1][col] == 6
                     || maze.getMaze()[maze.getMaze().length - 1][col] == 8)
               {
                  maze.setRC(row, col, 8);
                  row = maze.getMaze().length - 1;
               }
            }
         } 
         else if (isValid(Direction.UP, maze))
            row--;
         break;
      case DOWN:
         if (row == maze.getMaze().length)
         {
            // allows pacMan to travel to flip-side of maze at cut-throughs
            if (maze.getMaze()[row][col] == 6 || maze.getMaze()[row][col] == 8)
            {
               if (maze.getMaze()[0][col] == 6 || maze.getMaze()[0][col] == 8)
               {
                  maze.setRC(row, col, 8);
                  row = 0;
               }
            }
         } 
         else if (isValid(Direction.DOWN, maze))
            row++;
         break;
      case LEFT:
         if (col == 0)
         {
            // allows pacMan to travel to flip-side of maze at cut-throughs
            if (maze.getMaze()[row][col] == 6 || maze.getMaze()[row][col] == 8)
            {
               if (maze.getMaze()[row][0] == 6 || maze.getMaze()[row][0] == 8)
               {
                  maze.setRC(row, col, 8);
                  col = maze.getMaze()[0].length - 1;
               }
            }
         } 
         else if (isValid(Direction.LEFT, maze))
            col--;
         break;
      case RIGHT:
         if (col == maze.getMaze()[0].length - 1)
         {
            // allows pacMan to travel to flip-side of maze at cut-throughs
            if (maze.getMaze()[row][col] == 6 || maze.getMaze()[row][col] == 8)
            {
               if (maze.getMaze()[row][0] == 6 || maze.getMaze()[row][0] == 8)
               {
                  maze.setRC(row, col, 8);
                  col = 0;
               }
            }
         } 
         else if (isValid(Direction.RIGHT, maze))
            col++;
         break;
      }
   }

   // getters and setters

   public Clip getPacEat()
   {
      return pacEat;
   }

   public void setPacEat(Clip pacEat)
   {
      this.pacEat = pacEat;
   }

   public boolean isChase()
   {
      return chase;
   }

   public void setChase(boolean chase)
   {
      this.chase = chase;
   }

   public int getChaseTimer()
   {
      return chaseTimer;
   }

   public void setChaseTimer(int chaseTimer)
   {
      this.chaseTimer = chaseTimer;
   }

   public int getChaseTime()
   {
      return chaseTime;
   }

   public void setChaseTime(int chaseTime)
   {
      this.chaseTime = chaseTime;
   }

   public int getCounter()
   {
      return presetTurnCounter;
   }

   public void setCounter(int counter)
   {
      this.presetTurnCounter = counter;
   }

   public int getUpKey()
   {
      return upKey;
   }

   public void setUpKey(int upKey)
   {
      this.upKey = upKey;
   }

   public int getDownKey()
   {
      return downKey;
   }

   public void setDownKey(int downKey)
   {
      this.downKey = downKey;
   }

   public int getLeftKey()
   {
      return leftKey;
   }

   public void setLeftKey(int leftKey)
   {
      this.leftKey = leftKey;
   }

   public int getRightKey()
   {
      return rightKey;
   }

   public void setRightKey(int rightKey)
   {
      this.rightKey = rightKey;
   }

   public Direction getNextDirection()
   {
      return nextDirection;
   }

   public void setNextDirection(Direction nextDirection)
   {
      this.nextDirection = nextDirection;
   }

   public int getRow()
   {
      return row;
   }

   public void setRow(int row)
   {
      this.row = row;
   }

   public int getCol()
   {
      return col;
   }

   public void setCol(int col)
   {
      this.col = col;
   }

   public int getW()
   {
      return w;
   }

   public void setW(int w)
   {
      this.w = w;
   }

   public int getH()
   {
      return h;
   }

   public void setH(int h)
   {
      this.h = h;
   }

   // used by ghost class to determine where pacman is going
   public int getDirection()
   {
      if (direction == Direction.UP)
         return 0;
      if (direction == Direction.DOWN)
         return 1;
      if (direction == Direction.LEFT)
         return 2;
      if (direction == Direction.RIGHT)
         return 3;
      return 4;
   }

   public void setDirection(Direction direction)
   {
      this.direction = direction;
   }

   public int getPoints()
   {
      return points;
   }

   public void setPoints(int points)
   {
      this.points = points;
   }

   public boolean isOpen()
   {
      return open;
   }

   public void setOpen(boolean open)
   {
      this.open = open;
   }

   public boolean isClosed()
   {
      return closed;
   }

   public void setClosed(boolean closed)
   {
      this.closed = closed;
   }

   public int getSwitchImageTimer()
   {
      return switchImageTimer;
   }

   public void setSwitchImageTimer(int switchImageTimer)
   {
      this.switchImageTimer = switchImageTimer;
   }

   public Clip getPacPowerUp()
   {
      return pacPowerUp;
   }

   public void setPacPowerUp(Clip pacPowerUp)
   {
      this.pacPowerUp = pacPowerUp;
   }

   public void thisKeyWasPressed(int theKeyThatWasPressed, Maze maze)
   {
      presetTurnCounter = 0;
      if (theKeyThatWasPressed == upKey)
      {
         nextDirection = Direction.UP;
      } else if (theKeyThatWasPressed == downKey)
      {
         nextDirection = Direction.DOWN;
      } else if (theKeyThatWasPressed == leftKey)
      {
         nextDirection = Direction.LEFT;
      } else if (theKeyThatWasPressed == rightKey)
      {
         nextDirection = Direction.RIGHT;
      }
   }

   // checks collision between pacman and ghost
   public boolean collision(Ghost ghost, Maze maze)
   {
      Rectangle g = ghost.getBounds(maze);
      Rectangle p = getBounds(maze);
      return g.intersects(p);
   }

   public Rectangle getBounds(Maze maze)
   {
      return new Rectangle(col * maze.getTileWidth(), row * maze.getTileHeight(), w, h);
   }

   public void draw(Graphics2D g2, Maze maze)
   {
      Image upImage = new ImageIcon(this.getClass().getResource("pacManUp.png")).getImage();
      Image downImage = new ImageIcon(this.getClass().getResource("pacManDown.png")).getImage();
      Image leftImage = new ImageIcon(this.getClass().getResource("pacManLeft.png")).getImage();
      Image rightImage = new ImageIcon(this.getClass().getResource("pacManRight.png")).getImage();
      if (closed)
      {
         upImage = new ImageIcon(this.getClass().getResource("pacManClosed.png")).getImage();
         downImage = new ImageIcon(this.getClass().getResource("pacManClosed.png")).getImage();
         leftImage = new ImageIcon(this.getClass().getResource("pacManClosed.png")).getImage();
         rightImage = new ImageIcon(this.getClass().getResource("pacManClosed.png")).getImage();
      }
      if (direction == Direction.UP)
      {
         g2.drawImage(upImage, col * maze.getTileWidth(), row * maze.getTileHeight(), h, w, null);
      } else if (direction == Direction.DOWN)
      {
         g2.drawImage(downImage, col * maze.getTileWidth(), row * maze.getTileHeight(), h, w, null);
      } else if (direction == Direction.LEFT)
      {
         g2.drawImage(leftImage, col * maze.getTileWidth(), row * maze.getTileHeight(), w, h, null);
      } else
      {
         g2.drawImage(rightImage, col * maze.getTileWidth(), row * maze.getTileHeight(), w, h, null);
      }
   }

   public void reset(int row, int col)
   {
      this.row = row;
      this.col = col;
      setChase(false);
      direction = Direction.STOPPED;
   }

   @Override
   public String toString()
   {
      return "PacMan [row=" + row + ", col=" + col + ", w=" + w + ", h=" + h + ", upKey=" + upKey + ", downKey="
            + downKey + ", leftKey=" + leftKey + ", rightKey=" + rightKey + ", direction=" + direction + ", points="
            + points + "]";
   }

}