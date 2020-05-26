package com.mvcApp.test.mvcApp.rest;

import java.awt.*;			// graphics and GUI classes
import java.awt.event.*;	// handles button and window events
import java.util.*;			// needed for Date and Properties

/** This is a simple class for producing scatter plots.
 *	It isn't very general or robust, but the code is short and easy to modify.
 *  The plot is fixed in size, with an off-screen buffer to avoid redrawing
 *	everything in the paint method.  The plot can therefore hold pixel-level detail.
 *	Many features could be added, such as labels on axes and grid lines, 
 *	and a listener to display mouse coordinates.
 *
 *	To create a new plot, say something like this:
 *
 *		Plot myPlot = new Plot("Title",-10,10,2,-2,2,.5);
 *
 *	In this example, the horizontal axis goes from -10 to 10 with grid lines every
 *	2 units, while the y axis goes from -2 to 2 with grid lines every .5 units.
 *	To add a point to the plot, say something like "myPlot.addPoint(x,y)".
 *	To change the size, shape, and color of the plot symbols, use
 * 	setPointSize(int), setPointShape(int), and setColor(Color).
 *
 *	@author Dan Schroeder
 */

public class Plot extends Canvas {

	// constants to define plotted point shapes (could add more later):
	public static final int SQUARE = 0;
	public static final int CIRCLE = 1;
	public static final int COLUMN = 100;

	static int plotCount = 0;		// number of Plot objects created so far (used to position window)
	String plotTitle;				// title of plot
	double xMin, xMax, yMin, yMax;	// the ranges of values for the plot to cover
	double xRange, yRange; 			// difference between min and max
	double xInterval, yInterval;	// grid spacing intervals
	int plotWidth = 400;			// width of plot in pixels
	int plotHeight = 400;			// height of plot in pixels
	Color pointColor = Color.red;	// color of plotted points (and lines), red by default
	int pointSize = 3;				// width of plot symbols, 3 pixels by default
	int pointShape = SQUARE;		// default point shape is a square
	boolean connected = false;		// whether to connect the dots
	boolean firstPoint = true;		// whether the next point to be plotted is the first
	int lastx, lasty;				// pixel coordinates of last point plotted
	Frame plotFrame;				// the window that will hold the plot
	Panel controlPanel;				// panel with buttons (declared here so subclasses can add more buttons)
	Image offScreenImage;			// off-screen image where we draw
	Graphics offScreenGraphics;		// graphics context for off-screen image
	Properties printprefs = new Properties();	// for storing user's printing preferences

	/** Creates a new plot with specified title, ranges, and grid spacings. */
	public Plot(String title, double x1, double x2, double x3, double y1, double y2, double y3) {
		plotCount += 1;
		plotTitle = title;
		xMin = x1; xMax = x2; yMin = y1; yMax = y2;
		xInterval = x3; yInterval = y3;
		xRange = xMax - xMin; yRange = yMax - yMin;
		plotFrame = new Frame(title);
		plotFrame.addWindowListener(new WindowAdapter() {	// remove this if you don't want the program
			public void windowClosing(WindowEvent e) {		// to quit when close-box is clicked
				System.exit(0);
		}});
		Panel centerPanel = new Panel();				// to avoid resizing the canvas, create a panel to hold it
		plotFrame.add(centerPanel,BorderLayout.CENTER);	// add the panel to the window
		centerPanel.add(this);							// and add the canvas to the panel
		this.setSize(plotWidth+1,plotHeight+1);			// now we can set the canvas's size and it'll work
			// (+1 is so gridlines show when they're at edges)
		controlPanel = new Panel();						// create a panel to hold the buttons
		plotFrame.add(controlPanel,BorderLayout.SOUTH);	// put it at the bottom of the window
		Button clearButton = new Button("Clear");		// create a button to clear the plot
		clearButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) { clearThePlot(); }
			});											// tell it what to do when the button is clicked
		controlPanel.add(clearButton);					// note that southPanel has the default FlowLayout
		Button printButton = new Button("Print");		// create the print button
		printButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) { printThePlot(); }
			});											// tell it what to do when the button is clicked
		controlPanel.add(printButton);					// add the print button to the bottom panel
		plotFrame.setResizable(false);
		plotFrame.pack();								// make the frame just large enough to hold its components
        offScreenImage = createImage(plotWidth+1,plotHeight+1);	// create the off-screen image where we'll draw
        offScreenGraphics = offScreenImage.getGraphics();		// get its graphics context
        clearThePlot();
		plotFrame.setLocation(400+20*plotCount,20*plotCount);	// put the window in the upper-right part of the screen
		plotFrame.setVisible(true);						// show the window!
		requestFocus();									// take focus away from the clear button
	}

	/** Adds a new point to the plot. */
	public synchronized void addPoint(double newx, double newy) {
		offScreenGraphics.setColor(pointColor);
		int pixelx = (int) Math.round(plotWidth * (newx-xMin) / xRange);	// convert x to a screen coordinate
		int pixely = (int) Math.round(plotHeight * (yMax-newy) / yRange);	// remember that screen y is measured downward
		int offset = (int) (pointSize/2.0);				// offset of top-left corner (rounded down)
		if (pointShape == COLUMN) {
			int yZero = (int) Math.round(yMax * plotHeight / yRange);	// position of y=0 in pixel coordinates
			if (pixely <= yZero) {
				offScreenGraphics.fillRect(pixelx-offset,pixely,pointSize,yZero-pixely); // above horizontal axis
			} else {
				offScreenGraphics.fillRect(pixelx-offset,yZero+1,pointSize,pixely-yZero);	 // below axis
			}
		} else {
			if (pointShape == CIRCLE) {
				offScreenGraphics.fillOval(pixelx-offset,pixely-offset,pointSize-1,pointSize-1);
			} else {
				offScreenGraphics.fillRect(pixelx-offset,pixely-offset,pointSize,pointSize);	// default is SQUARE
			}
		}
		if (connected && !firstPoint) {
			offScreenGraphics.drawLine(lastx,lasty,pixelx,pixely);
		}
		lastx = pixelx; lasty = pixely;
		firstPoint = false;
		repaint();		// tell Java that our paint method needs to be called
	}

	/** Changes the size of the plotted points (newSize in pixels). */
	public void setPointSize(int newSize) {
		pointSize = newSize;
	}

	/** Changes the color of the plotted points and lines. */
	public void setColor(Color newColor) {
		pointColor = newColor;
	}

	/** Changes the shape of the plotted points (see constants above for allowed values). */
	public void setPointShape(int newShape) {
		pointShape = newShape;
	}

	/** Sets a flag to determine whether the plotted points will be connected by lines. */
	public void setConnected(boolean flag) {
		connected = flag;
	}
	
	/** Paints the canvas (by simply copying the off-screen image). */
	public synchronized void paint(Graphics g) {
		g.drawImage(offScreenImage,0,0,plotWidth+1,plotHeight+1,this);
	}

	/** Override update to avoid redrawing background. */
	public void update(Graphics g) {
		paint(g);
	}
	
	/** Clears the plot and draws the axes and grid lines. */
	public synchronized void clearThePlot() {
		offScreenGraphics.setColor(Color.white);
		offScreenGraphics.fillRect(0,0,plotWidth+1,plotHeight+1);	// paint the background white

		int gridPixel;										// screen coordinate for a grid line
		offScreenGraphics.setColor(Color.lightGray);		// grid lines will be gray

		double gridX = Math.ceil(xMin/xInterval) * xInterval;	// find x value of first vertical grid line
		while (gridX <= xMax) {
			gridPixel = (int) Math.round(plotWidth * (gridX-xMin) / xRange);	// convert gridX to screen coordinate
			offScreenGraphics.drawLine(gridPixel,0,gridPixel,plotHeight);		// draw vertical grid line
			gridX += xInterval;
		}

		double gridY = Math.ceil(yMin/yInterval) * yInterval;	// find y value of lowest horizontal grid line
		while (gridY <= yMax) {
			gridPixel = (int) Math.round(plotHeight * (yMax-gridY) / yRange);	// remember that screen y is measured downward
			offScreenGraphics.drawLine(0,gridPixel,plotWidth,gridPixel);		// draw horizontal grid line
			gridY += yInterval;
		}

		int xZero = (int) Math.round(-xMin * plotWidth / xRange);	// position of x=0 in pixel coordinates
		int yZero = (int) Math.round(yMax * plotHeight / yRange);	// position of y=0 in pixel coordinates
		offScreenGraphics.setColor(Color.black);
		offScreenGraphics.drawLine(xZero,0,xZero,plotHeight);		// draw vertical axis
		offScreenGraphics.drawLine(0,yZero,plotWidth,yZero);		// draw horizontal axis

		firstPoint = true;
		
		repaint();
	}

	/** Prints the plot (adapted from Java Examples in a Nutshell, 2nd ed., page 315). */
	public void printThePlot() {
		Toolkit myToolkit = this.getToolkit();
		PrintJob myJob = myToolkit.getPrintJob(plotFrame, "PlotPrint", printprefs);	// show the print dialog
		if (myJob == null) return;			// if user clicked Cancel, don't proceed
		Graphics g = myJob.getGraphics();	// get the graphics context for printing
		g.translate(100,100);				// leave some space at the top and left margins
		Date today = new Date();
		g.drawString(plotTitle + ", printed by XYZ on " + today,0,-10);
		Dimension size = this.getSize();		// could just use plotWidth and plotHeight...
		g.setClip(0,0,size.width,size.height);	// clip any graphics outside border
		this.print(g);							// this in turn calls paint to draw the content
		g.dispose();
		myJob.end();
	}
}