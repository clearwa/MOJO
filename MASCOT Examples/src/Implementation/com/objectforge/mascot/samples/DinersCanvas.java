package com.objectforge.mascot.samples;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.util.PropertyResourceBundle;

import com.objectforge.mascot.utility.MascotDebug;

/**
 * @author Clearwa
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DinersCanvas extends Canvas {
    /**
     * Serialized version
     */
    private static final long serialVersionUID = 1L;
    String basename = "e:/Home/Clearwa/My Documents/Object Forge/MASCOT/Images";
	Image[] images = new Image[5];
	Toolkit toolkit = Toolkit.getDefaultToolkit();
	int numphils;
	boolean[] redraw;
	double[] philX;
	double[] philY;
	int[] state;

	double[] chopX;
	double[] chopY;
	boolean[] untable;
	PropertyResourceBundle bundle;

	private final int SLEEP = Philosopher.PH_SLEEPS;
	private final int HUNGRY = Philosopher.PH_HUNGRY;
	private final int RIGHT = Philosopher.PH_HAS_RIGHT;
	private final int LEFT = Philosopher.PH_HAS_LEFT;
	private final int EAT = Philosopher.PH_EATS;

	public DinersCanvas(int numphils) {
		MediaTracker mt = new MediaTracker(this);

		try {
			bundle = (PropertyResourceBundle) PropertyResourceBundle.getBundle("mascot_machine");
		} catch (RuntimeException e) {
			MascotDebug.println(9,"DinersCanvas: No property file\n\t" + e);
		}
		
		basename = bundle.getString( "mascot.machine.images" );
		this.numphils = numphils;
		redraw = new boolean[numphils];
		philX = new double[numphils];
		philY = new double[numphils];
		state = new int[numphils];
		chopX = new double[numphils];
		chopY = new double[numphils];
		untable = new boolean[numphils];

		initPlacing();
		images[SLEEP] = toolkit.createImage(basename + "/Sleeping.gif");
		mt.addImage(images[SLEEP], 0);
		images[LEFT] = toolkit.createImage(basename + "/Left Fork.gif");
		mt.addImage(images[LEFT], 0);
		images[RIGHT] = toolkit.createImage(basename + "/Right Fork.gif");
		mt.addImage(images[RIGHT], 0);
		images[HUNGRY] = toolkit.createImage(basename + "/Hungry.gif");
		mt.addImage(images[HUNGRY], 0);
		images[EAT] = toolkit.createImage(basename + "/Dining.gif");
		mt.addImage(images[EAT], 0);

		try {
			mt.waitForAll();
		} catch (InterruptedException e) {
		}
	}

	Image offscreen;
	Dimension offscreensize;
	Graphics offgraphics;

	void backdrop() {
		Dimension d = getSize();
		if ((offscreen == null) || (d.width != offscreensize.width) || (d.height != offscreensize.height)) {
			offscreen = createImage(d.width, d.height);
			offscreensize = d;
			offgraphics = offscreen.getGraphics();
			offgraphics.setFont(new Font("Helvetica", Font.BOLD, 18));
		}
		offgraphics.setColor(Color.lightGray);
		offgraphics.fillRect(0, 0, getSize().width, getSize().height);
		for (int i = 0; i < numphils; i++) {
			redraw[i] = true;
		}
	}

	void drawtable() {
		offgraphics.setColor(Color.red);
		offgraphics.fillOval(240, 240, 100, 100);
		offgraphics.setColor(Color.black);
		for (int i = 0; i < numphils; i++) {
			if (untable[i])
				offgraphics.fillOval((int) chopX[i], (int) chopY[i], 10, 10);
		}
	}

	public void paint(Graphics g) {
		backdrop();
		update(g);
	}

	public void update(Graphics g) {
		for (int i = 0; i < numphils; i++) {
			if (redraw[i]) {
				philPaint(offgraphics, i);
				redraw[i] = false;
			}
		}
		drawtable();
		g.drawImage(offscreen, 0, 0, null);
	}

	void philPaint(Graphics g, int i) {
		g.setColor(Color.lightGray);
		g.fillRect((int) philX[i], (int) philY[i], images[0].getWidth(this), images[0].getHeight(this));
		g.drawImage(images[state[i]], (int) philX[i], (int) philY[i], this);
	}

	void setPhil(int id, int s) throws java.lang.InterruptedException {
		state[id] = s;
		redraw[id] = true;
	}

	void setFork(int id, boolean taken) {
		untable[id] = !taken;
	}
	
	private final static double PHIL_RADIUS = 200.0;
	private final static double PHIL_ADJ = 200.0;
	private final static double CHOP_RADIUS = 35.0;
	private final static double CHOP_ADJ = 285.0;

	void initPlacing() {
		double radians = (2.0*Math.PI)/(2.0*(double)numphils);
		double axis;

		for (int i = 0; i < numphils; i++) {
			axis = 2*i*radians;
			philX[i] = (Math.sin(axis) * PHIL_RADIUS) + PHIL_ADJ;
			philY[i] = (Math.cos(axis) * PHIL_RADIUS) + PHIL_ADJ;
			
			axis += radians;
			chopX[i] = (Math.sin(axis) * CHOP_RADIUS) + CHOP_ADJ;
			chopY[i] = (Math.cos(axis) * CHOP_RADIUS) + CHOP_ADJ;
		}

	}
}
