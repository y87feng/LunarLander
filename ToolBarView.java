import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// the edit toolbar
public class ToolBarView extends JPanel{
    private GameModel model;

    JButton undo = new JButton("Undo");
    JButton redo = new JButton("Redo");

    public ToolBarView(GameModel model) {
        this.model = model;

        model.addView(new IView() {
            @Override
            public void updateView() {
                if (model.canUndo()) {
                    undo.setEnabled(true);
                } else {
                    undo.setEnabled(false);
                }

                if (model.canRedo()) {
                    redo.setEnabled(true);
                } else {
                    redo.setEnabled(false);
                }
            }
        });

        setLayout(new FlowLayout(FlowLayout.LEFT,20,10));

        undo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (model.canUndo()) {
                    model.undo();
                }
            }
        });

        redo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (model.canRedo()) {
                    model.redo();
                }
            }
        });

        // prevent buttons from stealing focus
        undo.setFocusable(false);
        redo.setFocusable(false);

        add(undo);
        add(redo);
    }

}
