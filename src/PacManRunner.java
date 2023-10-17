import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class PacManRunner extends JPanel implements KeyListener, MouseListener
{
   private int PREF_W;
   private int PREF_H;
   private PacMan pacMan;
   private Timer timer;
   private Maze currentMaze;
   private int level;
   private Ghost ghost, ghost2, ghost3, ghost4;
   private boolean roundOver = false;
   private boolean gameOver = false;
   private boolean gameWon = false;
   private String score;
   private int lives = 3;
   private static JFrame frame;
   private Clip pacEat, pacEatGhost, pacDie, intro, pacPowerUp, ghostSiren;
   private int ghostTally = 0;

   public PacManRunner() throws IOException
   {
      this.addKeyListener(this);
      this.addMouseListener(this);
      this.setFocusable(true);
      level = 1;
      currentMaze = new Maze(String.format("src/Maze%d.txt", level), 21, 21);
      ghost = new Ghost(currentMaze, 21, 21, 1, 1, 18, 7, Color.RED);
      ghost2 = new Ghost(currentMaze, 21, 21, 1, 1, 35, 7, Color.BLUE);
      ghost3 = new Ghost(currentMaze, 21, 21, 1, 1, 60, 7, Color.PINK);
      ghost4 = new Ghost(currentMaze, 21, 21, 1, 1, 85, 7, Color.ORANGE);
      pacMan = new PacMan(1, 1, 21, 21, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT);
      score = 0 + "";
      PREF_W = currentMaze.getCols() * currentMaze.getTileWidth() + 200;
      PREF_H = currentMaze.getRows() * currentMaze.getTileHeight() + 20;
      frame.setSize(getPreferredSize());

      try
      {
         URL url = this.getClass().getClassLoader().getResource("pacEat.wav");
         AudioInputStream audio = AudioSystem.getAudioInputStream(url);
         Clip pacEat = AudioSystem.getClip(); // initialize a sound clip object
         pacEat.open(audio); // direct the clip to play the audio defined above
         pacMan.setPacEat(pacEat);
      } catch (Exception e)
      {
         e.printStackTrace();
      }

      try
      {
         URL url = this.getClass().getClassLoader().getResource("pacEatGhost.wav");
         AudioInputStream audio = AudioSystem.getAudioInputStream(url);
         pacEatGhost = AudioSystem.getClip(); // initialize a sound clip object
         pacEatGhost.open(audio); // direct the clip to play the audio defined above
      } catch (Exception e)
      {
         e.printStackTrace();
      }
      try
      {
         URL url = this.getClass().getClassLoader().getResource("pacDie.wav");
         AudioInputStream audio = AudioSystem.getAudioInputStream(url);
         pacDie = AudioSystem.getClip(); // initialize a sound clip object
         pacDie.open(audio); // direct the clip to play the audio defined above
      } catch (Exception e)
      {
         e.printStackTrace();
      }

      try
      {
         URL url = this.getClass().getClassLoader().getResource("pacPowerUp.wav");
         AudioInputStream audio = AudioSystem.getAudioInputStream(url);
         pacPowerUp = AudioSystem.getClip(); // initialize a sound clip object
         pacPowerUp.open(audio); // direct the clip to play the audio defined above
         pacMan.setPacPowerUp(pacPowerUp);
      } catch (Exception e)
      {
         e.printStackTrace();
      }
      try
      {
         URL url = this.getClass().getClassLoader().getResource("ghostSiren.wav");
         AudioInputStream audio = AudioSystem.getAudioInputStream(url);
         ghostSiren = AudioSystem.getClip(); // initialize a sound clip object
         ghostSiren.open(audio); // direct the clip to play the audio defined above
         ghost.setSound(ghostSiren);
      } catch (Exception e)
      {
         e.printStackTrace();
      }

      try
      {
         URL url = this.getClass().getClassLoader().getResource("Pacman-sound.wav");
         AudioInputStream audio = AudioSystem.getAudioInputStream(url);
         intro = AudioSystem.getClip(); // initialize a sound clip object
         intro.open(audio); // direct the clip to play the audio defined above
         intro.setMicrosecondPosition(0);
         intro.start();
      } catch (Exception e)
      {
         e.printStackTrace();
      }

      timer = new Timer(210, new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent e)
         {
            if (!roundOver && !gameOver && !gameWon && intro.getMicrosecondPosition() == intro.getMicrosecondLength())
            {
               update();
            }
         }

      });
      timer.start();
   }

   // checks collision between ghost and pacman and does what is
   // necessary based on the given situation
   public boolean checkCollision(Ghost g, PacMan p, Maze m)
   {
      if (!roundOver)
      {
         if (p.collision(g, m))
         {
            if (p.isChase())
            {
               ghostTally++;
               pacEatGhost.setMicrosecondPosition(0);
               pacEatGhost.start();
               p.setPoints(p.getPoints() + 200 * ghostTally);
               g.setDead(true);
               return false;
            }
            pacDie.setMicrosecondPosition(0);
            pacDie.start();
            lives--;
            roundOver = true;
            pacMan.setChase(false);
            pacMan.getPacPowerUp().setMicrosecondPosition(pacMan.getPacPowerUp().getMicrosecondLength());
            ghost.getSound().setMicrosecondPosition(ghost.getSound().getMicrosecondLength());
            return true;
         }
      }
      return false;
   }

   public void update()
   {
      // game code goes here

      // checks if user won level
      if (currentMaze.isEmpty())
      {
         gameWon = true;
         ghost.getSound().setMicrosecondPosition(ghost.getSound().getMicrosecondLength());
         pacMan.setChase(false);
         pacMan.getPacPowerUp().setMicrosecondPosition(pacMan.getPacPowerUp().getMicrosecondLength());
      }
      score = pacMan.getPoints() + "";
      pacMan.update(currentMaze);
      if (checkCollision(ghost, pacMan, currentMaze))
         return;
      if (checkCollision(ghost2, pacMan, currentMaze))
         return;
      if (checkCollision(ghost3, pacMan, currentMaze))
         return;
      if (checkCollision(ghost4, pacMan, currentMaze))
         return;
      ghost.update(currentMaze, pacMan);
      ghost2.update(currentMaze, pacMan);
      ghost3.update(currentMaze, pacMan);
      ghost4.update(currentMaze, pacMan);
      // check collision checked twice to make sure ghost and pacman can not pass
      // through each other if they start next to each other and each move once
      if (checkCollision(ghost, pacMan, currentMaze))
         return;
      if (checkCollision(ghost2, pacMan, currentMaze))
         return;
      if (checkCollision(ghost3, pacMan, currentMaze))
         return;
      if (checkCollision(ghost4, pacMan, currentMaze))
         return;
      if (lives == 0)
      {
         gameOver = true;
         pacMan.setChase(false);
         pacMan.getPacPowerUp().setMicrosecondPosition(pacMan.getPacPowerUp().getMicrosecondLength());
         ghost.getSound().setMicrosecondPosition(ghost.getSound().getMicrosecondLength());
      }
      repaint();
   }

   public Dimension getPreferredSize()
   {
      return new Dimension(PREF_W, PREF_H);
   }

   public static void main(String[] args) throws IOException
   {
      frame = new JFrame("PacMan");
      JPanel gamePanel = new PacManRunner();
      frame.add(gamePanel);
      frame.pack();
      frame.setLocationRelativeTo(null);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
      frame.setResizable(true);
   }

   public void paintComponent(Graphics g)
   {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      if (!gameOver)
      {
         g2.setColor(Color.WHITE);
         g2.fillRect(0, 0, this.getWidth(), this.getHeight());
         currentMaze.draw(g2);
         pacMan.draw(g2, currentMaze);
         ghost.draw(g2, currentMaze, pacMan);
         ghost2.draw(g2, currentMaze, pacMan);
         ghost3.draw(g2, currentMaze, pacMan);
         ghost4.draw(g2, currentMaze, pacMan);
         Font stringFont = new Font("SansSerif", Font.PLAIN, 55);
         g2.setFont(stringFont);
         int width = g.getFontMetrics().stringWidth(score);
         g2.setColor(Color.BLACK);
         g2.drawString(score, PREF_W - width - 5, 75);
         Image pacMan = new ImageIcon(this.getClass().getResource("pacManRight.png")).getImage();
         // draws a pacman image to represent lives
         for (int i = 0; i < lives; i++)
         {
            g2.drawImage(pacMan, PREF_W - 60 + 20 * i, 125, 15, 15, null);
         }
         // round over message instructing player how to reset
         if (roundOver)
         {
            stringFont = new Font("SansSerif", Font.PLAIN, 20);
            g2.setFont(stringFont);
            width = g.getFontMetrics().stringWidth("You Died.");
            int width2 = g.getFontMetrics().stringWidth("Press R to restart.");
            g2.drawString("You Died", PREF_W - width - 5, 175);
            g2.drawString("Press R to restart.", PREF_W - width2 - 5, 225);
         }
      }
      // game over message instructing player how to restart
      if (gameOver)
      {
         g2.setColor(Color.RED);
         g2.fillRect(0, 0, this.getWidth(), this.getHeight());
         Font stringFont = new Font("SansSerif", Font.PLAIN, 35);
         g2.setFont(stringFont);
         int width = g.getFontMetrics().stringWidth("Game Over! Press R to try again");
         g2.setColor(Color.WHITE);
         g2.drawString("Game Over! Press R to try again", PREF_W / 2 - width / 2, 75);
      }
      // gamewon message instructing player how to restart or continue to next level
      if (gameWon)
      {
         g2.setColor(Color.GREEN);
         g2.fillRect(0, 0, this.getWidth(), this.getHeight());
         Font stringFont = new Font("SansSerif", Font.PLAIN, 35);
         g2.setFont(stringFont);
         g2.setColor(Color.WHITE);
         if (level < 3) {
            int width = g.getFontMetrics().stringWidth("Level Won! Press N to go to next level.");
            g2.drawString("Level Won! Press N to go to next level.", PREF_W / 2 - width / 2, 75);
         } else {
            frame.setSize(getPreferredSize());
            int width = g.getFontMetrics().stringWidth("Game Won! Press R to restart.");
            g2.drawString("Game Won! Press R to restart.", PREF_W / 2 - width / 2, 75);
         }
      }
      repaint();
   }

   @Override
   public void keyTyped(KeyEvent e)
   {

   }

   // resets maze, pacman, and ghosts
   public void restart(int level)
   {
      try
      {
         currentMaze = new Maze(String.format("src/Maze%d.txt", level), 21, 21);
         PREF_W = currentMaze.getCols() * currentMaze.getTileWidth() + 200;
         PREF_H = currentMaze.getRows() * currentMaze.getTileHeight() + 20;
         frame.setSize(getPreferredSize());
         pacMan.reset(1, 1);
         pacMan.setPoints(0);
         ghost.reset(currentMaze, 12);
         ghost2.reset(currentMaze, 18);
         ghost3.reset(currentMaze, 24);
         ghost4.reset(currentMaze, 30);
         gameOver = false;
         gameWon = false;
      } catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   @Override
   public void keyPressed(KeyEvent e)
   {
      pacMan.thisKeyWasPressed(e.getKeyCode(), currentMaze);
      // resets pacman after player dies but still has lives left
      if (e.getKeyCode() == KeyEvent.VK_R && roundOver)
      {
         pacMan.reset(1, 1);
         ghost.reset(currentMaze, 12);
         ghost2.reset(currentMaze, 18);
         ghost3.reset(currentMaze, 24);
         ghost4.reset(currentMaze, 30);
         roundOver = false;
      }
      // used for testing to fast forward through a maze and try out all levels
      if (e.getKeyCode() == KeyEvent.VK_F)
      {
         currentMaze.ffMaze();
      }
      // restarts after all lives are used or all levels are completed
      if (e.getKeyCode() == KeyEvent.VK_R && (gameOver || gameWon && level == 3))
      {
         intro.setMicrosecondPosition(0);
         intro.start();
         lives = 3;
         level = 1;
         restart(level);
      }
      // advances to next level after level is completed
      if (e.getKeyCode() == KeyEvent.VK_N && gameWon && level != 3)
      {
         level++;
         restart(level);
      }
      repaint();
   }

   @Override
   public void keyReleased(KeyEvent e)
   {
   }

   @Override
   public void mouseClicked(MouseEvent e)
   {

   }

   @Override
   public void mousePressed(MouseEvent e)
   {
   }

   @Override
   public void mouseReleased(MouseEvent e)
   {
   }

   @Override
   public void mouseEntered(MouseEvent e)
   {
   }

   @Override
   public void mouseExited(MouseEvent e)
   {
   }

}
