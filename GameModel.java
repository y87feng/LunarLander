import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.undo.*;
import javax.vecmath.*;
import java.util.Random;

// View interface
interface IView {
    public void updateView();
}


public class GameModel {

    // Undo manager
    private UndoManager undoManager;

    //Model data
    private ArrayList<IView> m_ViewList = new ArrayList<IView>();

    private Rectangle2D.Double m_Landing_Pad;

    private Polygon polygon = new Polygon();

    Timer timer;

    long lastEndTime = System.currentTimeMillis();

    ArrayList<Rectangle2D.Double> lasers;

    boolean start;
    //method

    public GameModel(int fps, int width, int height, int peaks) {
        undoManager = new UndoManager();

        ship = new Ship(fps, width/2, 50);

        worldBounds = new Rectangle2D.Double(0, 0, width, height);

        lasers = new ArrayList<Rectangle2D.Double>();

        timer = new Timer((int)(1000/fps), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                updatelaser();
            }
        });

        lastEndTime = System.currentTimeMillis();

        start = false;
        // anonymous class to monitor ship updates
        ship.addView(new IView() {
            @Override
            public void updateView() {
                if (!ship.isPaused() && (IsLanded() || Iscrashed())) {
                    setPaused(true);
                }
                setChangedAndNotify();
            }
        });

        m_Landing_Pad = new Rectangle2D.Double(330,100,40,10);

        int x,y = 0;
        int n = 20;
        Random rand = new Random();

        // biuld terrin
        polygon.addPoint(0,height);
        for (int i = 0; i < n; i++) {
            x = (int)(i * (double)width/(n - 1));
            y = rand.nextInt(height/2) + height / 2;
            polygon.addPoint(x,y);
        }
        polygon.addPoint(width,height);
    }

    public void setPeakValue(int index, Point2d newcoord,Point2d originPoint) {

        // create undoable edit
        UndoableEdit undoableEdit = new AbstractUndoableEdit() {

            // capture variables for closure
            final Point2d oldValue = originPoint;
            final Point2d newValue = newcoord;

            // Method that is called when we must redo the undone action
            public void redo() throws CannotRedoException {
                super.redo();
                polygon.xpoints[index] = (int)newValue.x;
                polygon.ypoints[index] = (int)newValue.y;
                rebiuld();
                notifyObservers();
            }

            public void undo() throws CannotUndoException {
                super.undo();
                polygon.xpoints[index] = (int)oldValue.x;
                polygon.ypoints[index] = (int)oldValue.y;
                rebiuld();
                notifyObservers();
            }
        };

        // Add this undoable edit to the undo manager
        undoManager.addEdit(undoableEdit);

        // finally, set the value and notify views
        polygon.xpoints[index] = (int)newcoord.x;
        polygon.ypoints[index] = (int)newcoord.y;
        rebiuld();
        notifyObservers();
    }


    public void setPadValue(Point2d newcoord,Point2d originPoint) {

        // create undoable edit
        UndoableEdit undoableEdit = new AbstractUndoableEdit() {

            // capture variables for closure
            final Point2d oldValue = originPoint;
            final Point2d newValue = newcoord;

            // Method that is called when we must redo the undone action
            public void redo() throws CannotRedoException {
                super.redo();
                m_Landing_Pad.x = newValue.x;
                m_Landing_Pad.y = newValue.y;
                notifyObservers();
            }

            public void undo() throws CannotUndoException {
                super.undo();
                m_Landing_Pad.x = oldValue.x;
                m_Landing_Pad.y = oldValue.y;
                notifyObservers();
            }
        };

        // Add this undoable edit to the undo manager
        undoManager.addEdit(undoableEdit);

        // finally, set the value and notify views
        m_Landing_Pad.x = newcoord.x;
        m_Landing_Pad.y = newcoord.y;
        notifyObservers();
    }

    public void undo() { if (canUndo()) undoManager.undo(); }
    public void redo() { if (canRedo()) undoManager.redo(); }
    public boolean canUndo() { return undoManager.canUndo(); }
    public boolean canRedo() { return undoManager.canRedo(); }
    public boolean getStart() { return start; }
    public void setStart(boolean bool) { start = bool; }

    public Rectangle2D.Double getLandingPad() { return m_Landing_Pad; }
    public void setLandingPad(Point2d position) {
        m_Landing_Pad.x = position.x;
        m_Landing_Pad.y = position.y;
    }

    public Polygon getPolygon() {return polygon; }
    public ArrayList<Rectangle2D.Double> getlasers() { return lasers; }

    void restart() {
        int height = 200;
        int width = 700;

        undoManager = new UndoManager();

        ship = new Ship(60, width/2, 50);

        m_Landing_Pad = new Rectangle2D.Double(330,100,40,10);

        worldBounds = new Rectangle2D.Double(0, 0, width, height);

        start = false;
        // anonymous class to monitor ship updates
        ship.addView(new IView() {
            @Override
            public void updateView() {
                if (!ship.isPaused() && (IsLanded() || Iscrashed())) {
                    setPaused(true);
                }
                setChangedAndNotify();
            }
        });

        lasers = new ArrayList<Rectangle.Double>();

        polygon = new Polygon();

        int x,y = 0;
        int n = 20;
        Random rand = new Random();

        polygon.addPoint(0,height);
        for (int i = 0; i < n; i++) {
            x = (int)(i * (double)width/(n - 1));
            y = rand.nextInt(height/2) + height / 2;
            polygon.addPoint(x,y);
        }
        polygon.addPoint(width,height);
    }


    // World
    // - - - - - - - - - - -
    public final Rectangle2D getWorldBounds() {
        return worldBounds;
    }

    Rectangle2D.Double worldBounds;


    // Ship
    // - - - - - - - - - - -

    public Ship ship;

    public boolean Iscrashed() {
        if (polygon.intersects(ship.getShape()) || !worldBounds.contains(ship.getShape())) {
            return true;
        }

        for (int i = 0; i < lasers.size(); i++) {
            if (lasers.get(i).intersects(ship.getShape())) {
                return true;
            }
        }

        return false;
    }

    public boolean IsLanded() {
        if (m_Landing_Pad.intersects(ship.getShape())) {
            return  true;
        }
        return  false;
    }

    // rebiuld polygon and calculate the bounds
    public void rebiuld() {
        int xps[] = polygon.xpoints;
        int ypx[] = polygon.ypoints;
        int n = polygon.npoints;
        polygon = new Polygon();
        for (int i = 0; i < n; i++) {
            polygon.addPoint(xps[i],ypx[i]);
        }
    }

    // helper function to do both
    void setChangedAndNotify() {
        notifyObservers();
    }

    // set the view observer
    public void addView(IView view) {
        m_ViewList.add(view);
        // update the view to current state of the model
        view.updateView();
    }

    // notify the IView observer
    private void notifyObservers() {
        for (IView view : this.m_ViewList) {
            view.updateView();
        }
    }

    void updatelaser() {
        if (System.currentTimeMillis() - lastEndTime >= 5000.0f) {
            lastEndTime = System.currentTimeMillis();
            Random rand = new Random();
            int y = rand.nextInt((int)getWorldBounds().getHeight()/2);
            lasers.add(new Rectangle2D.Double(getWorldBounds().getWidth(),y,20,5));
        }

        for (int i = 0; i < lasers.size(); i++) {
            lasers.get(i).x -= 1;
        }

        notifyObservers();
    }

    void setPaused(boolean paused) {
        if (paused) {
            timer.stop();
        } else {
            timer.start();
        }
        ship.setPaused(paused);
    }

}



