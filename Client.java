/*
<Applet Code="Client.class" fps=10 width=600 height=800> </Applet>
 */
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;

public class Client extends Applet {

    TriggerType triggerType = TriggerType.TIMER_TRIGGER;
    // TriggerType triggerType = TriggerType.EVENT_TRIGGER;
    // threshold for event based sensor (in degrees)
    double threshold = 5;
    // The speed of simulation
    // (How many simulation second elapses when 1 second real time elapses)
    double simSpeed = 0.1;
    // Sensor sampling rate (per simulation second)
    double sensorSamplingRate = 100;
    // advance of simulation time (in second) per step
    double tau_sim = 0.01;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    Physics physics;
		Sensor sensor;
		Actuator actuator;
    UpdatingUIThread animator;

    Socket requestSocket;
    Thread physicsThread;
    Thread sensorThread, actuatorThread;
    Thread updatingUIThread;
    // frames per second for updating UI
    int fps = 10;
    // simulation time between two samples (in seconds)
    double sensorSamplingPeriod_sim = 1.0 / sensorSamplingRate;
    double sensorSamplingPeriod_phy = sensorSamplingPeriod_sim / simSpeed;

		double pole_density = 0.1;
		double pole_length = 1.0;

    String[] configInfo;

    final int APPLET_WIDTH = 800;
    final int APPLET_HEIGHT = 400;

		// Button button;

    /**
     * This method initializes the pole state and sets up animation timing.
     */
    public void init() {

        this.setSize(new Dimension(APPLET_WIDTH, APPLET_HEIGHT));

        String str;

        // Build configuration info string
        configInfo = new String[2];
        StringBuilder sb = new StringBuilder();
        sb.append("Sim. Speed: ").append(String.format("%.3f  ", simSpeed));
        sb.append("   Sim. Step: ").append(String.format("%.3f sec  ",tau_sim));
        configInfo[0] = sb.toString();

        sb = new StringBuilder();
        if(triggerType == TriggerType.EVENT_TRIGGER){
            sb.append("Event Based Sensor  ");
        }else{
            sb.append("Time Based Sensor  ");
        }
        sb.append(String.format("%.2f Hz", sensorSamplingRate));
        if(triggerType == TriggerType.EVENT_TRIGGER){
            sb.append("  Threshold: ").append(String.format("%.02f", threshold));
        }
        configInfo[1] = sb.toString();
        // -------------------------------------

				// Getting pole density and pole length from user
				pole_density = Double.parseDouble(JOptionPane.showInputDialog(this,
							"Enter pole density:",
							"0.1"));
				pole_length = Double.parseDouble(JOptionPane.showInputDialog(this,
							"Enter pole length:",
							"1.0"));

				// Adding a button
				/*
				button = new Button("STOP");
				this.add(button);
				button.addActionListener(this);
				*/

    }

    /**
     * This method starts animating by creating a new Thread.
     */
    public void start() {

        try {
            requestSocket = new Socket("localhost", 25533);
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());
        } catch (IOException e) {
            System.out.println("Not able to bind to server");
        }


        //Start animating!
        if (physicsThread == null) {
        		physics = new Physics(tau_sim, tau_sim / simSpeed, pole_density, pole_length);
            physicsThread = new Thread(physics);
        }
        physicsThread.start();

        if (sensorThread == null) {
						sensor = new Sensor(physics, out, triggerType, threshold,
								sensorSamplingPeriod_sim, sensorSamplingPeriod_phy);
            sensorThread = new Thread(sensor);
        }
        sensorThread.start();

        if (actuatorThread == null) {
						actuator = new Actuator(physics, in);
            actuatorThread = new Thread(actuator);
        }
        actuatorThread.start();

        if (updatingUIThread == null) {
        		animator = new UpdatingUIThread(this, physics, (int) (1000 / fps), configInfo);
            updatingUIThread = new Thread(animator);
        }
        updatingUIThread.start();

    }

    /**
     * This method stops the animating thread and gets rid of the objects necessary for double buffering.
     */
    public void stop() {
       //Stop the animating thread.
        // physicsThread = null;
        physics.shutdown();
        sensor.shutdown();
        actuator.shutdown();
				animator.shutdown();

			 try {
            out.writeObject("bye"); // signal to close the sever
            out.flush();

            in.readObject();

            requestSocket.close();
            out.close();
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * This method paints the graphics by calling the update method.
     */
    public void paint(Graphics gr) {
        animator.update(gr);
    }

		/*
		 * Handle button click event
		 */
		/*
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == button) {
				if (button.getLabel().equals("STOP")) {
					System.out.println("STOP clicked");
					this.stop();
					button.setLabel("START");
				} else if (button.getLabel().equals("START")) {
					System.out.println("START clicked");
					this.init();
					this.start();
					button.setLabel("STOP");
				}
			}
		}
		*/
}
