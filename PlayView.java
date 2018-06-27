import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

// the actual game view
public class PlayView extends JPanel {

    private GameModel model;

    public PlayView(GameModel model) {
        this.model = model;

        model.addView(new IView() {
            @Override
            public void updateView() {
                repaint();
            }
        });

        // needs to be focusable for keylistener
        setFocusable(true);

        // want the background to be black
        setBackground(Color.LIGHT_GRAY);

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyTyped(e);

                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (model.Iscrashed() || model.IsLanded()) {
                        model.restart();
                    } else {
                        if (!model.getStart()) {
                            model.setStart(true);
                        }
                        model.setPaused(!model.ship.isPaused());
                    }
                }

                if (!model.ship.isPaused()) {
                    if (e.getKeyCode() == KeyEvent.VK_W) {
                        model.ship.thrustUp();
                    } else if (e.getKeyCode() == KeyEvent.VK_A) {
                        model.ship.thrustLeft();
                    } else if (e.getKeyCode() == KeyEvent.VK_S) {
                        model.ship.thrustDown();
                    } else if (e.getKeyCode() == KeyEvent.VK_D) {
                        model.ship.thrustRight();
                    }
                }

                repaint();
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g; // cast to get 2D drawing methods
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  // antialiasing look nicer
                RenderingHints.VALUE_ANTIALIAS_ON);


        AffineTransform M = g2.getTransform();

        int scale = 3;

        // draw game world

        g2.translate(-scale*(int)model.ship.getPosition().x+getWidth()/2,
                -scale*(int)model.ship.getPosition().y + getHeight()/2);
        g2.scale(scale,scale);

        //draw boundary
        g2.setColor(Color.BLACK);

        int worldwidth = (int)model.getWorldBounds().getWidth();
        int worldheight = (int)model.getWorldBounds().getHeight();

        g2.fillRect(0,-worldheight, worldwidth,worldheight);
        g2.fillRect(worldwidth,-worldheight,worldwidth,3*worldheight);
        g2.fillRect(-worldwidth,-worldheight,worldwidth,3*worldheight);
        g2.fillRect(0,worldheight,worldwidth,worldheight);

        //draw terrin
        g2.setColor(Color.darkGray);
        g2.fillPolygon(model.getPolygon());

        // draw pad
        Rectangle.Double landingPad = model.getLandingPad();
        g2.setColor(Color.RED);

        g2.fillRect((int)landingPad.getX(),(int)landingPad.getY(),
                (int)landingPad.getWidth(),(int)landingPad.getHeight());

        //draw lasers
        g2.setColor(Color.WHITE);
        for (int i = 0; i < model.getlasers().size(); i++) {
            g2.fillRect((int)model.getlasers().get(i).getX(),(int)model.getlasers().get(i).getY(),
                    (int)model.getlasers().get(i).getWidth(),(int)model.getlasers().get(i).getHeight());
        }

        //draw ship
        g2.setColor(Color.BLUE);
        g2.fillRect((int)model.ship.getPosition().x, (int)model.ship.getPosition().y,10,10);

        g2.setTransform(M);

    }
}
