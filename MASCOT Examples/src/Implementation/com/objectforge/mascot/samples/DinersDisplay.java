package com.objectforge.mascot.samples;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.objectforge.mascot.IDA.SPElement;
import com.objectforge.mascot.IDA.StatusPool;
import com.objectforge.mascot.machine.internal.Activity;
import com.objectforge.mascot.machine.internal.model.MascotMachineException;
import com.objectforge.mascot.machine.roots.AbstractRoot;
import com.objectforge.mascot.utility.MascotDebug;

/**
 * @author Clearwa
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DinersDisplay extends AbstractRoot {

	public class WAdapter extends WindowAdapter {

		/**
		 */
		public void windowClosing(WindowEvent e) {
			MascotDebug.println(9,"Window closing");
			win.dispose();
		}

		/**
		 */
		public void windowClosed(WindowEvent e) {
			MascotDebug.println(9,"Window closed");
			doTerminate();
		}
	}

	Object[] args;
	int numphils;
	int[] philstates;
	DinersCanvas myCanvas;
	int[] modcounts;
	StatusPool pool;
	int currentCount = 0;

	//Display objects
	JFrame win = new JFrame();
	String philmessages[];
	JPanel buttonbox = new JPanel();
	JButton exit = new JButton();
	JButton resume = new JButton();
	JSlider slider = new JSlider(10, 6000, 5000);
	boolean pause = false;
	
	/**
	 */
	public void mascotRoot(Activity activity, Object[] args) {
		this.args = args;
		numphils = ((Integer) args[0]).intValue();
		setStatus("numphils", new Integer( numphils ) );
		
		philstates = new int[numphils];
		modcounts = new int[numphils];
		win.getContentPane().setLayout(new BorderLayout());
		myCanvas = new DinersCanvas((numphils = ((Integer) args[0]).intValue()));
		for (int i = 0; i < numphils; i++) {
			myCanvas.setFork(i, false);
			try {
				myCanvas.setPhil(i, Philosopher.PH_SLEEPS);
			} catch (InterruptedException e) {
			}
		}
		myCanvas.setSize(560, 600);
		win.getContentPane().add(myCanvas, "Center");
		win.setTitle("MOJO Dining Philosophers - " + numphils + " diners");

		buttonbox.setLayout(new BorderLayout());

		resume.setText("Pause");
		exit.setText("Exit");
		resume.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setRun(pause);
				pause = !pause;
				resume.setText((pause) ? "Resume" : "Pause");
			}
		});
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				win.dispose();
			}
		});

		final JSlider slider = new JSlider(50, 6000, 5000);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!slider.getValueIsAdjusting()) {
					Object[] values = new Object[2];

					values[0] = "time";
					values[1] = new Integer(slider.getValue());
					try {
						write("status-pool", values);
					} catch (MascotMachineException ev) {
					}
				}
			}
		});

		pool = (StatusPool) resolve("status-pool");
		philmessages = new String[numphils];

		buttonbox.add(slider, "Center");
		buttonbox.add(exit, "East");
		buttonbox.add(resume, "West");
		win.getContentPane().add(buttonbox, "South");

		SPElement status = null;
		try {
			Object[] statusValues = status("status-pool");
			status = (SPElement) statusValues[StatusPool.STAT_SESSION_TITLE];
		} catch (MascotMachineException e) {
		}

		setStatus("diners", win);

		win.setTitle(
			"MOJO Dining Philosophiers - "
				+ numphils
				+ " Philosophers for session "
				+ ((Object[]) status.contents)[1]);

		win.addWindowListener(new WAdapter());
		win.pack();
		win.setVisible( true );
		resumeRoot();
	}

	private void setStatus(Object key, Object value) {
		Object[] values = new Object[2];

		values[0] = key;
		values[1] = value;
		try {
			write("status-pool", values);
		} catch (MascotMachineException e) {
		}
	}

	private void setRun(boolean state) {
		setStatus("run", new Boolean(state));
	}

	private void doTerminate() {
		setStatus("diners", null);
		setStatus("terminate", new Boolean(true));
	}

	/**
	 */
	public void resumeRoot() {
		boolean[][] forks = new boolean[numphils + 1][2];
		while (true) {
			Vector state = (Vector) pool.read(currentCount);
			currentCount = ((Integer) state.remove(0)).intValue();
//			MascotDebug.println(9, "DinersDisplay gets read");
			int size = state.size();

			if (size < numphils)
				continue;

			for (int i = 0; i < size; i++) {
				SPElement spElement = (SPElement) state.remove(0);
				Object[] elements = (Object[]) spElement.contents;
				int pstate = ((Integer) elements[Philosopher.PH_STATE]).intValue();
				int pindex = ((Integer) elements[Philosopher.PH_INDEX]).intValue() - 1;

				if (spElement.modCount > modcounts[pindex]) {
					philstates[pindex] = pstate;
					philmessages[pindex] = (String) elements[Philosopher.PH_MESSAGE];
				}
				modcounts[pindex] = spElement.modCount;
			}

			for (int i = 0; i < numphils; i++) {
				int philState = philstates[i];
				boolean[] currentForks = forks[i];
				
				if( philState==Philosopher.PH_EATS)
					currentForks[0] = currentForks[ 1 ] = true;
				else if( philState==Philosopher.PH_HAS_LEFT )
					currentForks[0] = true;
				else if(philState==Philosopher.PH_HAS_RIGHT) 
					currentForks[1] = true;
				else
					currentForks[0] = currentForks[ 1 ] = false;
				if( i==0 )
					forks[numphils] = currentForks;
			}
			
			for( int i=0;i<numphils;i++){
				myCanvas.setFork(i, forks[i][1] || forks[i+1][0]);
				try {
					myCanvas.setPhil(i, philstates[i]);
				} catch (InterruptedException e) {
				}
			}
			myCanvas.repaint();
		}
	}

	

}
