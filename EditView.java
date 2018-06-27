/* In A3Enhanced, I add lasers in game. Every 5 seconds there is a new laser created on the right side
*/


import javax.swing.*;
import javax.vecmath.Point2d;
import java.awt.*;
import java.awt.event.*;

import static java.lang.Math.abs;

// the editable view of the terrain and landing pad
public class EditView extends JPanel {

    private GameModel model;

    // pad fields
    boolean padIsDraged = false;
    boolean padIsSelected = false;
    Point2d originPadPoint;
    Point2d offset;

    // Peak fields
    Point2d originPeakPoint;
    Point2d focusPoint;
    int focusPointIndex;
    boolean pointIsSelected = false;
    boolean pointIsDragged = false;

    public EditView(GameModel model) {
        this.model = model;

        // want the background to be black
        setBackground(Color.LIGHT_GRAY);

        // Controller
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point2d position = new Point2d(model.getLandingPad().x,model.getLandingPad().y);
                Polygon polygon = model.getPolygon();

                if (e.getY() >= position.y && e.getY() <= position.y + 10
                        && e.getX() <= position.x + 40 && e.getX() >= position.x) { // inside pad

                    originPadPoint = position;
                    offset = new Point2d((int)e.getX() - position.x,(int)e.getY() - position.y);
                    padIsSelected = true;
                }

                else if (contains(e.getX(),e.getY(), polygon.xpoints,
                        polygon.ypoints,polygon.npoints)) { // inside circle

                    offset = new Point2d(0,(int)e.getY() - focusPoint.y);
                    pointIsSelected = true;
                }
            }
        });

        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (padIsSelected) {
                    Point2d position = new Point2d(e.getX() - offset.x,e.getY() - offset.y);

                    // Check if it is inside world
                    if (model.getWorldBounds().getWidth() >= position.x + 40 && position.x>=0 &&
                            model.getWorldBounds().getHeight() >= position.y + 10 && position.y >= 0) {
                        model.setLandingPad(position);
                    }

                    padIsDraged = true;
                } else if (pointIsSelected) {
                    Point2d tmp = new Point2d(focusPoint.x,e.getY() - offset.y);

                    // Check if it is inside world
                    if (model.getWorldBounds().getHeight() >= tmp.y && tmp.y >= 0) {
                        focusPoint = tmp;
                        model.getPolygon().ypoints[focusPointIndex] = (int) focusPoint.y;
                    }

                    pointIsDragged = true;
                }
                model.setChangedAndNotify();
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (padIsDraged) {
                    // here put position into UNDO stack
                    Point2d position = new Point2d(model.getLandingPad().x,model.getLandingPad().y);
                    model.setPadValue(position,originPadPoint);

                    padIsDraged = false;
                } else if (pointIsDragged) {
                    // here put position into UNDO stack
                    model.setPeakValue(focusPointIndex,focusPoint,originPeakPoint);

                    pointIsDragged = false;
                }
                padIsSelected = false;
                pointIsSelected = false;
            }
        });


        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    originPadPoint = new Point2d(model.getLandingPad().x,model.getLandingPad().y);

                    Point2d position = new Point2d(e.getX() - 20,e.getY() - 5);

                    if (model.getWorldBounds().getWidth() >= position.x + 40 && position.x>=0 &&
                            model.getWorldBounds().getHeight() >= position.y + 10 && position.y >= 0) {
                        model.setPadValue(position,originPadPoint);
                    }
                }
            }
        });


        model.addView(new IView() {
            @Override
            public void updateView() {
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

        //draw bound
        g2.setColor(Color.BLACK);

        int worldwidth = (int)model.getWorldBounds().getWidth();
        int worldheight = (int)model.getWorldBounds().getHeight();

        g2.fillRect(worldwidth,0,getWidth()-worldwidth,worldheight);


        //draw terrin
        g2.setColor(Color.darkGray);
        g2.fillPolygon(model.getPolygon());

        //draw circle
        int xpoint[] = model.getPolygon().xpoints;
        int ypoint[] = model.getPolygon().ypoints;
        int r = 15; //radius
        g2.setStroke(new BasicStroke(1.0f));
        g2.setColor(Color.GRAY);
        for (int i = 1; i < model.getPolygon().npoints - 1; i++) {
            g2.drawOval(xpoint[i] - r,ypoint[i] - r,2*r,2*r);
        }


        // draw pad
        Rectangle.Double landingPad = model.getLandingPad();
        g2.setColor(Color.RED);

        g2.fillRect((int)landingPad.getX(),(int)landingPad.getY(),40,10);


        // ***************************** highlight the change
        if (padIsDraged) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawRect((int)landingPad.getX(),(int)landingPad.getY() ,40,10);
        }

        if (pointIsDragged) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawOval((int)focusPoint.x - r,(int)focusPoint.y - r ,2*r,2*r);
        }

        // *****************************
    }


    // check if the mouse point is inside the circle
    public boolean contains(int x, int y, int[] xpoints, int[]ypoints, int npoints) {
        int r = 15;
        for (int i = 1; i < npoints - 1; i++) {
            if (abs(xpoints[i] - x) <= r && abs(ypoints[i] - y) <= r) {
                focusPoint = new Point2d(xpoints[i],ypoints[i]);
                originPeakPoint = new Point2d(xpoints[i],ypoints[i]);;
                focusPointIndex = i;
                return true;
            }
        }
        return false;
    }
}
