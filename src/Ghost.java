import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;

public class Ghost
{
   enum Direction
   {
      UP, DOWN, LEFT, RIGHT
   };

   private int row, col;
   private int dx, dy;
   private int w, h;
   // delay leaving the ghost house
   private int delay;
   private Direction direction = Direction.RIGHT;
   private int targetRow, targetCol;
   private Color color;
   // scatterCountDown is how long ghosts have before they "scatter" to their
   // corner
   // scatterTimer is how long ghosts stay in scatter mode
   private int scatterCountDown;
   private int scatterTimer;
   private boolean dead;
   private Clip sound;

   public Ghost(Maze maze, int w, int h, int dx, int dy, int delay, int updateTime, Color color)
   {
      reset(maze, delay);
      this.w = w;
      this.h = h;
      this.dx = dx;
      this.dy = dy;
      this.color = color;
      scatterTimer = 80;
      scatterCountDown = 160;
      dead = false;
   }

   // determines if a direction in the maze is valid
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

   public void update(Maze maze, PacMan pac)
   {
      /*
       * update method: red and pink will look to move horizontally first unless
       * pacman is on the opposite side of the maze vertically
       * 
       * blue will prefer either horizontal or vertical depending on whether dx or dy
       * is farther from pacman
       *
       * orange will move randomly, but if near pacman it will avoid pacman
       */
     
      if (color == Color.RED && !pac.isChase())
      {
         int siren = (int) (Math.random() * 18);
         if (siren == 9)
         {
            if (sound.getMicrosecondPosition() == sound.getMicrosecondLength())
            {
               sound.setMicrosecondPosition(0);
            }
            sound.start();
         }
      }
      double dX = 0;
      double dY = 0;
      boolean targetUp = false;
      boolean targetDown = false;
      boolean targetLeft = false;
      boolean targetRight = false;
      Direction newDirection;
      if (!pac.isChase() && maze.getMaze()[row][col] != 3)
         scatterCountDown--;
      if (scatterCountDown < 0 && !pac.isChase() && maze.getMaze()[row][col] != 3 && !dead)
      {
         scatterTimer--;
         if (scatterTimer == 0)
         {
            scatterCountDown = 160;
            scatterTimer = 80;
         }
         if (color == Color.RED)
         {
            targetRow = 0;
            targetCol = 0;
         }
         if (color == Color.PINK)
         {
            targetRow = 0;
            targetCol = maze.getMaze().length - 1;
         }
         if (color == Color.BLUE)
         {
            targetRow = maze.getMaze().length - 1;
            targetCol = 0;
         }
         if (color == Color.ORANGE)
         {
            targetRow = maze.getMaze().length - 1;
            targetCol = maze.getMaze().length - 1;
         }
      } 
      else
      {
         targetRow = pac.getRow();
         targetCol = pac.getCol();
         if (color == Color.PINK)
         {
            if (pac.getDirection() == 0)
            {
               targetRow = pac.getRow() - 5;
            }
            if (pac.getDirection() == 1)
            {
               targetRow = pac.getRow() + 5;
            }
            if (pac.getDirection() == 2)
            {
               targetRow = pac.getCol() - 5;
            }
            if (pac.getDirection() == 3)
            {
               targetRow = pac.getRow() + 5;
            }
         }
         if (color == Color.ORANGE)
         {
            int row = 0;
            int col = 0;
            while (maze.getMaze()[row][col] % 2 != 0)
            {
               row = (int) (Math.random() * maze.getMaze().length);
               col = (int) (Math.random() * maze.getMaze()[0].length);
            }
            targetRow = row;
            targetCol = col;
         }
      }
      if(dead) {
         Ghost g = new Ghost(maze, 0, 0, 0, 0, 0, 0, color);
         g.reset(maze, delay);
         targetRow = g.getRow();
         targetCol = g.getCol();
      }
      
      dX = Math.abs(col - targetCol);
      dY = Math.abs(row - targetRow);
      targetUp = row > targetRow;
      targetDown = row < targetRow;
      targetLeft = col > targetCol;
      targetRight = col < targetCol;
      
      if(dead) {
         if(dX > dY) newDirection = prefer_horizontal(targetUp, targetDown, targetLeft, targetRight, maze);
         else newDirection = prefer_vertical(targetUp, targetDown, targetLeft, targetRight, maze);

      }
      // movement in ghost house before leaving
      if (maze.getMaze()[row][col] == 3 || maze.getMaze()[row][col] == 5)
      {
         dead = false;
         if (delay != 0)
         {
            delay--;
            if (delay < 20 && maze.getMaze()[row - 1][col] == 3)
            {
               row -= dy;
               return;
            }
            if (delay < 10 && (maze.getMaze()[row - 1][col] != 1))
            {
               row -= dy;
               return;
            }
            if (direction == Direction.RIGHT)
            {
               if (maze.getMaze()[row][col + 1] == 3 || maze.getMaze()[row][col + 1] == 5)
                  col += dx;
               else
               {
                  col -= dx;
                  direction = Direction.LEFT;
               }
            } 
            else
            {
               if (maze.getMaze()[row][col - 1] == 3 || maze.getMaze()[row][col - 1] == 5)
                  col -= dx;
               else
               {
                  col += dx;
                  direction = Direction.RIGHT;
               }
            }
            return;
         }
         newDirection = Direction.UP;
      } 
      else if (color == Color.RED && !dead)
      {
         if (pac.isChase() && maze.getMaze()[row][col] != 3)
            newDirection = avoid(targetUp, targetDown, targetLeft, targetRight, maze);
         else
         {
            if ((pac.getRow() > maze.getRows() / 2 && row < maze.getRows() / 2)
                  || (pac.getRow() < maze.getRows() / 2 && row > maze.getRows() / 2))
            {
               newDirection = prefer_vertical(targetUp, targetDown, targetLeft, targetRight, maze);
            } 
            else
            {
               newDirection = prefer_horizontal(targetUp, targetDown, targetLeft, targetRight, maze);
            }
         }
      } 
      else if (color == Color.PINK && !dead)
      {
         if (pac.isChase() && maze.getMaze()[row][col] != 3)
            newDirection = avoid(targetUp, targetDown, targetLeft, targetRight, maze);
         else
         {
            if ((pac.getRow() > maze.getRows() / 2 && row < maze.getRows() / 2)
                  || (pac.getRow() < maze.getRows() / 2 && row > maze.getRows() / 2))
            {
               newDirection = prefer_vertical(targetUp, targetDown, targetLeft, targetRight, maze);
            } 
            else
            {
               newDirection = prefer_horizontal(targetUp, targetDown, targetLeft, targetRight, maze);
            }
         }
      } 
      else if (color == Color.BLUE && !dead)
      {
         if (pac.isChase() && maze.getMaze()[row][col] != 3)
            newDirection = avoid(targetUp, targetDown, targetLeft, targetRight, maze);
         else if (dX > dY)
         {
            newDirection = prefer_horizontal(targetUp, targetDown, targetLeft, targetRight, maze);
         } 
         else
         {
            newDirection = prefer_vertical(targetUp, targetDown, targetLeft, targetRight, maze);
         }
      } 
      else if (color == Color.ORANGE && isDead())
      {
         if (pac.isChase() && maze.getMaze()[row][col] != 3)
            newDirection = avoid(targetUp, targetDown, targetLeft, targetRight, maze);
         else if (Math.abs(row - pac.getRow()) < 9 || Math.abs(col - pac.getCol()) < 9)
         {
            targetUp = pac.getRow() < row;
            targetDown = pac.getRow() > row;
            targetLeft = pac.getCol() < col;
            targetRight = pac.getCol() > col;
            newDirection = avoid(targetUp, targetDown, targetLeft, targetRight, maze);
         } 
         else
            newDirection = prefer_horizontal(targetUp, targetDown, targetLeft, targetRight, maze);
      } 
      else
      {
         newDirection = prefer_vertical(targetUp, targetDown, targetLeft, targetRight, maze);
      }

      if ((direction == Direction.DOWN) && (newDirection == Direction.UP))
      {
         if (isValid(Direction.RIGHT, maze))
         {
            newDirection = Direction.RIGHT;
         } 
         else if (isValid(Direction.LEFT, maze))
         {
            newDirection = Direction.LEFT;
         }
         else if (isValid(direction, maze))
         {
            newDirection = direction;
         }
      }

      if ((direction == Direction.UP) && (newDirection == Direction.DOWN))
      {
         if (isValid(Direction.RIGHT, maze))
         {
            newDirection = Direction.RIGHT;
         } 
         else if (isValid(Direction.LEFT, maze))
         {
            newDirection = Direction.LEFT;
         } 
         else if (isValid(direction, maze))
         {
            newDirection = direction;
         }
      }

      if ((direction == Direction.LEFT) && (newDirection == Direction.RIGHT))
      {
         if (isValid(Direction.UP, maze))
         {
            newDirection = Direction.UP;
         } 
         else if (isValid(Direction.DOWN, maze))
         {
            newDirection = Direction.DOWN;
         } 
         else if (isValid(direction, maze))
         {
            newDirection = direction;
         }
      }

      if ((direction == Direction.RIGHT) && (newDirection == Direction.LEFT))
      {
         if (isValid(Direction.UP, maze))
         {
            newDirection = Direction.UP;
         } 
         else if (isValid(Direction.DOWN, maze))
         {
            newDirection = Direction.DOWN;
         } 
         else if (isValid(direction, maze))
         {
            newDirection = direction;
         }
      }

      if (!pac.isChase() && maze.getMaze()[row][col] != 3)
      {
         switch (newDirection)
         {
         case UP:
            row -= dy;
            break;
         case DOWN:
            row += dy;
            break;
         case LEFT:
            col -= dx;
            break;
         case RIGHT:
            col += dx;
            break;
         }
      }
      if (pac.isChase() && maze.getMaze()[row][col] != 3)
      {
         if (pac.getChaseTimer() % 2 == 0)
         {
            switch (newDirection)
            {
            case UP:
               row -= dy;
               break;
            case DOWN:
               row += dy;
               break;
            case LEFT:
               col -= dx;
               break;
            case RIGHT:
               col += dx;
               break;
            }
         }
      }
      direction = newDirection;
   }

   // getters and setters

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

   public int getDx()
   {
      return dx;
   }

   public void setDx(int dx)
   {
      this.dx = dx;
   }

   public int getDy()
   {
      return dy;
   }

   public void setDy(int dy)
   {
      this.dy = dy;
   }

   public int getDelay()
   {
      return delay;
   }

   public void setDelay(int delay)
   {
      this.delay = delay;
   }

   public int getTargetRow()
   {
      return targetRow;
   }

   public void setTargetRow(int targetRow)
   {
      this.targetRow = targetRow;
   }

   public int getTargetCol()
   {
      return targetCol;
   }

   public void setTargetCol(int targetCol)
   {
      this.targetCol = targetCol;
   }

   public int getScatterCountDown()
   {
      return scatterCountDown;
   }

   public void setScatterCountDown(int scatterCountDown)
   {
      this.scatterCountDown = scatterCountDown;
   }

   public int getScatterTimer()
   {
      return scatterTimer;
   }

   public void setScatterTimer(int scatterTimer)
   {
      this.scatterTimer = scatterTimer;
   }

   public Direction getDirection()
   {
      return direction;
   }

   public void setDirection(Direction direction)
   {
      this.direction = direction;
   }

   public Color getColor()
   {
      return color;
   }

   public void setColor(Color color)
   {
      this.color = color;
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

   public Clip getSound()
   {
      return sound;
   }

   public void setSound(Clip sound)
   {
      this.sound = sound;
      sound.setMicrosecondPosition(sound.getMicrosecondLength());
   }

   public boolean isDead()
   {
      return dead;
   }

   public void setDead(boolean dead)
   {
      this.dead = dead;
   }

   // used by red, pink, and blue ghosts in different situations
   // will make horizontal movements towards pacman as a priority, otherwise will
   // move vertically
   public Direction prefer_horizontal(boolean targetUp, boolean targetDown, boolean targetLeft, boolean targetRight,
         Maze maze)
   {
      if (targetRight && isValid(Direction.RIGHT, maze))
         return Direction.RIGHT;
      else if (targetLeft && isValid(Direction.LEFT, maze))
         return Direction.LEFT;
      else if (targetUp && isValid(Direction.UP, maze))
         return Direction.UP;
      else if (targetDown && isValid(Direction.DOWN, maze))
         return Direction.DOWN;
      else
      {
         if (isValid(Direction.UP, maze))
            return Direction.UP;
         else if (isValid(Direction.DOWN, maze))
            return Direction.DOWN;
         else if (isValid(Direction.RIGHT, maze))
            return Direction.RIGHT;
         else if (isValid(Direction.LEFT, maze))
            return Direction.LEFT;
      }
      return Direction.LEFT;
   }

   // used by red, pink, and blue ghosts in different situations
   // will make vertical movements towards pacman as a priority, otherwise will
   // move horizontally
   public Direction prefer_vertical(boolean targetUp, boolean targetDown, boolean targetLeft, boolean targetRight,
         Maze maze)
   {
      if (targetUp && isValid(Direction.UP, maze))
         return Direction.UP;
      else if (targetDown && isValid(Direction.DOWN, maze))
         return Direction.DOWN;
      else if (targetRight && isValid(Direction.RIGHT, maze))
         return Direction.RIGHT;
      else if (targetLeft && isValid(Direction.LEFT, maze))
         return Direction.LEFT;
      else
      {
         if (isValid(Direction.RIGHT, maze))
            return Direction.RIGHT;
         else if (isValid(Direction.LEFT, maze))
            return Direction.LEFT;
         else if (isValid(Direction.UP, maze))
            return Direction.UP;
         else if (isValid(Direction.DOWN, maze))
            return Direction.DOWN;
      }
      return Direction.LEFT;
   }
   
   public Direction returnToGhostHouse(boolean targetUp, boolean targetDown, boolean targetLeft, boolean targetRight,
         Maze maze)
   {
      boolean valUp = isValid(Direction.UP, maze) || (row != 0 && maze.getMaze()[row-1][col] == 3) || (row != 0 && maze.getMaze()[row-1][col] == 5);
      boolean valDown = isValid(Direction.DOWN, maze) || (row != maze.getMaze().length && maze.getMaze()[row+1][col] == 3) || (row != maze.getMaze().length && maze.getMaze()[row+1][col] == 5);
      boolean valLeft = isValid(Direction.LEFT, maze) || (col != 0 && maze.getMaze()[row][col-1] == 3) || (col != 0 && maze.getMaze()[row][col-1] == 5);
      boolean valRight = isValid(Direction.RIGHT, maze) || (col != maze.getMaze()[0].length && maze.getMaze()[row][col+1] == 3) || (col != maze.getMaze()[0].length && maze.getMaze()[row][col+1] == 5);
      if (targetUp && valUp)
         return Direction.UP;
      else if (targetDown && valDown)
         return Direction.DOWN;
      else if (targetRight && valRight)
         return Direction.RIGHT;
      else if (targetLeft && valLeft)
         return Direction.LEFT;
      else
      {
         if (valRight)
            return Direction.RIGHT;
         else if (valLeft)
            return Direction.LEFT;
         else if (valUp)
            return Direction.UP;
         else if (valDown)
            return Direction.DOWN;
      }
      return Direction.LEFT;
   }

   // used by orange ghosts, and other ghosts while vulnerable
   // will try to move away from pacman first
   public Direction avoid(boolean targetUp, boolean targetDown, boolean targetLeft, boolean targetRight, Maze maze)
   {
      if (targetUp && isValid(Direction.DOWN, maze))
         return Direction.DOWN;
      else if (targetDown && isValid(Direction.UP, maze))
         return Direction.UP;
      else if (targetRight && isValid(Direction.LEFT, maze))
         return Direction.LEFT;
      else if (targetLeft && isValid(Direction.RIGHT, maze))
         return Direction.RIGHT;
      else
      {
         if (isValid(Direction.RIGHT, maze))
            return Direction.RIGHT;
         else if (isValid(Direction.LEFT, maze))
            return Direction.LEFT;
         else if (isValid(Direction.UP, maze))
            return Direction.UP;
         else if (isValid(Direction.DOWN, maze))
            return Direction.DOWN;
      }
      return Direction.LEFT;
   }

   public Rectangle getBounds(Maze maze)
   {
      return new Rectangle(col * maze.getTileWidth(), row * maze.getTileHeight(), w, h);
   }

   public void draw(Graphics2D g2, Maze maze, PacMan pac)
   {
      Image red = new ImageIcon(this.getClass().getResource("redGhost.png")).getImage();
      Image orange = new ImageIcon(this.getClass().getResource("orangeGhost.png")).getImage();
      Image pink = new ImageIcon(this.getClass().getResource("pinkGhost.png")).getImage();
      Image blue = new ImageIcon(this.getClass().getResource("blueGhost.png")).getImage();
      // switches between panic and panic2 when vulnerable stage is winding down to
      // create blinking effect
      // stays only as panic when vulnerable time is > 18
      Image panic = new ImageIcon(this.getClass().getResource("vulnerableGhost.png")).getImage();
      Image panic2 = new ImageIcon(this.getClass().getResource("blinkingGhost.png")).getImage();
      g2.setColor(color);
      if (pac.isChase() && maze.getMaze()[row][col] != 3)
      {
         if (pac.getChaseTimer() > 18)
         {
            g2.drawImage(panic, col * maze.getTileWidth(), row * maze.getTileHeight(), h, w, null);
         } 
         else
         {
            if (pac.getChaseTimer() % 3 == 0)
               g2.drawImage(panic2, col * maze.getTileWidth(), row * maze.getTileHeight(), h, w, null);
            else
               g2.drawImage(panic, col * maze.getTileWidth(), row * maze.getTileHeight(), h, w, null);
         }
      } 
      else
      {
         if (color == Color.RED)
         {
            g2.drawImage(red, col * maze.getTileWidth(), row * maze.getTileHeight(), h, w, null);
         }
         if (color == Color.BLUE)
         {
            g2.drawImage(blue, col * maze.getTileWidth(), row * maze.getTileHeight(), h, w, null);
         }
         if (color == Color.PINK)
         {
            g2.drawImage(pink, col * maze.getTileWidth(), row * maze.getTileHeight(), h, w, null);
         }
         if (color == Color.ORANGE)
         {
            g2.drawImage(orange, col * maze.getTileWidth(), row * maze.getTileHeight(), h, w, null);
         }
      }
   }

   // finds ghost house in maze and resets to a random location
   public void reset(Maze maze, int delay)
   {
      int minR = maze.getMaze().length;
      int minC = maze.getMaze()[0].length;
      int maxR = 0;
      int maxC = 0;
      for (int r = 0; r < maze.getMaze().length; r++)
      {
         for (int c = 0; c < maze.getMaze()[r].length; c++)
         {
            if (maze.getMaze()[r][c] == 3)
            {
               if (r < minR)
                  minR = r;
               if (c < minC)
                  minC = c;
               if (r > maxR)
                  maxR = r;
               if (c > maxC)
                  maxC = c;
            }
         }
      }
      this.row = (int) (Math.random() * (maxR - minR + 1) + minR);
      this.col = (int) (Math.random() * (maxC - minC + 1) + minC);
      this.delay = delay;
   }

   @Override
   public String toString()
   {
      return "Ghost [row=" + row + ", col=" + col + ", w=" + w + ", h=" + h + ", direction=" + direction + ", color="
            + color + "]";
   }

}