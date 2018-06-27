import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class MessageView extends JPanel{
    private GameModel model;

    // status messages for game
    JLabel fuel = new JLabel("Fuel");
    JLabel speed = new JLabel("Speed");
    JLabel message = new JLabel("Message");

    public MessageView(GameModel model) {
        this.model = model;

        model.addView(new IView() {
            @Override
            public void updateView() {

                //set fuel
                if (model.ship.getFuel() < 10.0f) {
                    fuel.setForeground(Color.RED);
                } else {
                    fuel.setForeground(Color.WHITE);
                }
                fuel.setText("Fuel: " + Double.toString(model.ship.getFuel()));

                // set speed
                if (model.ship.getSpeed() < model.ship.getSafeLandingSpeed()) {
                    speed.setForeground(Color.GREEN);
                } else {
                    speed.setForeground(Color.WHITE);
                }
                DecimalFormat numberFormat = new DecimalFormat("0.00");
                speed.setText("Speed: " + numberFormat.format(model.ship.getSpeed()));

                // set message
                if (model.IsLanded() && model.getStart()) {
                    speed.setForeground(Color.GREEN);
                    speed.setText("Speed: 0.00");
                    if (model.ship.getSpeed() < model.ship.getSafeLandingSpeed()) {
                        message.setText("LANDED!");
                    } else {
                        message.setText("CRASH");
                    }
                } else if (model.Iscrashed() && model.getStart()) {
                    speed.setForeground(Color.GREEN);
                    speed.setText("Speed: 0.00");
                    message.setText("CRASH");
                } else if (model.ship.isPaused()) {
                    message.setText("(Paused)");
                } else {
                    message.setText("");
                }
            }
        });

        // want the background to be black
        setBackground(Color.BLACK);

        setLayout(new FlowLayout(FlowLayout.LEFT));

        add(fuel);
        add(speed);
        add(message);

        for (Component c: this.getComponents()) {
            c.setForeground(Color.WHITE);
            c.setPreferredSize(new Dimension(100, 20));
        }
    }

}