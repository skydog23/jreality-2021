/*
 * Created on Nov 13, 2022
 *
 */
package demo.gears;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.jogamp.opengl.util.Animator;



/**
 * JGears.java <BR>
 * author: Brian Paul (converted to Java by Ron Cemer and Sven Goethel) <P>
 *
 * This version is equal to Brian Paul's version 1.2 1999/10/21
 */

public class JGears extends GLJPanel {
  private static GLCapabilities caps;
  private long startTime;
  private int frameCount;
  private float fps;
  private static Font fpsFont = new Font("SansSerif", Font.BOLD, 24);
  private final DecimalFormat format = new DecimalFormat("####.00");
  private BufferedImage javaImage;
  private BufferedImage openglImage;

  static {
    caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
    caps.setAlphaBits(8);
  }

  public JGears() {
    super(caps, null);
    addGLEventListener(new demo.gears.Gears());
    try {
      InputStream in = JGears.class.getClassLoader().getResourceAsStream("demo/gears/java_logo.png");
      BufferedImage image = ImageIO.read(in);
      javaImage = scaleImage(image, 0.25f, 0.25f);

      in = JGears.class.getClassLoader().getResourceAsStream("demo/gears/opengl_logo.png");
      image = ImageIO.read(in);
      openglImage = scaleImage(image, 0.45f, 0.45f);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  @Override
public void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (startTime == 0) {
      startTime = System.currentTimeMillis();
    }

    if (++frameCount == 30) {
      long endTime = System.currentTimeMillis();
      fps = 30.0f / (endTime - startTime) * 1000;
      frameCount = 0;
      startTime = System.currentTimeMillis();
    }

    if (fps > 0) {
      g.setColor(Color.WHITE);
      g.setFont(fpsFont);
      g.drawString("FPS: " + format.format(fps), getWidth() - 140, getHeight() - 30);
    }

    int sp = 10;
    if (javaImage != null) {
      g.drawImage(javaImage, sp, getHeight() - javaImage.getHeight() - sp, null);
      if (openglImage != null) {
        g.drawImage(openglImage, sp + javaImage.getWidth() + sp, getHeight() - openglImage.getHeight() - sp, null);
      }
    }
  }

  // Helper routine for various demos
  public static JPanel createGradientPanel() {
    JPanel gradientPanel = new JPanel() {
        @Override
		public void paintComponent(Graphics g) {
          ((Graphics2D) g).setPaint(new GradientPaint(0, 0, Color.WHITE,
                                                      getWidth(), getHeight(), Color.DARK_GRAY));
          g.fillRect(0, 0, getWidth(), getHeight());
        }
      };
    gradientPanel.setLayout(new BorderLayout());
    return gradientPanel;
  }

  private BufferedImage scaleImage(BufferedImage img, float xScale, float yScale) {
    BufferedImage scaled = new BufferedImage((int) (img.getWidth() * xScale),
                                             (int) (img.getHeight() * yScale),
                                             BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = scaled.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g.drawRenderedImage(img, AffineTransform.getScaleInstance(xScale, yScale));
    return scaled;
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("Gear Demo");
    frame.getContentPane().setLayout(new BorderLayout());
    final GLJPanel drawable = new JGears();
    drawable.setOpaque(false);

    JPanel gradientPanel = createGradientPanel();
    frame.getContentPane().add(gradientPanel, BorderLayout.CENTER);
    gradientPanel.add(drawable, BorderLayout.CENTER);

    final JCheckBox checkBox = new JCheckBox("Transparent", true);
    checkBox.addActionListener(new ActionListener() {
        @Override
		public void actionPerformed(ActionEvent e) {
          drawable.setOpaque(!checkBox.isSelected());
        }
      });
    frame.getContentPane().add(checkBox, BorderLayout.SOUTH);

    frame.setSize(300, 300);
    final Animator animator = new Animator(drawable);
    frame.addWindowListener(new WindowAdapter() {
        @Override
		public void windowClosing(WindowEvent e) {
          // Run this on another thread than the AWT event queue to
          // make sure the call to Animator.stop() completes before
          // exiting
          new Thread(new Runnable() {
              @Override
			public void run() {
                animator.stop();
                System.exit(0);
              }
            }).start();
        }
      });
    frame.setVisible(true);
    animator.start();
  }
}