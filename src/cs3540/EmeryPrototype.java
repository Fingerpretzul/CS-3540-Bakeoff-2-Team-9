package cs3540;

import java.util.ArrayList;
import java.util.Collections;
import processing.core.PApplet;
import processing.core.PVector;

public class EmeryPrototype extends PApplet {

	// these are variables you should probably leave alone
	int index = 0; // starts at zero-ith trial
	float border = 0; // some padding from the sides of window, set later
	int trialCount = 12; // this will be set higher for the bakeoff
	int trialIndex = 0; // what trial are we on
	int errorCount = 0; // used to keep track of errors
	float errorPenalty = 0.5f; // for every error, add this value to mean time
	int startTime = 0; // time starts when the first click is captured
	int finishTime = 0; // records the time of the final click
	boolean userDone = false; // is the user done

	final int screenPPI = 72; // what is the DPI of the screen you are using
	// you can test this by drawing a 72x72 pixel rectangle in code, and then
	// confirming with a ruler it is 1x1 inch.

	// These variables are for my example design. Your input code should
	// modify/replace these!
	float logoX = 500;
	float logoY = 500;
	float logoZ = 50f;
	float logoRotation = 0;

	// These variables are for modifying the logo during
	// dragging
	float activeX = 0;
	float activeY = 0;
	float activeRotation = 0;
	float activeZ = 0;

	boolean buttonPress = false;

	PVector initialClick;

	// The argument passed to main must match the class name
	public static void main(String[] args) {
		// Tell processing what class we want to run.
		PApplet.main("cs3540.EmeryPrototype");
	}

	private class Destination {
		float x = 0;
		float y = 0;
		float rotation = 0;
		float z = 0;
	}

	ArrayList<Destination> destinations = new ArrayList<Destination>();

	public void settings() {
		size(800, 600);
	}

	public void setup() {
		rectMode(CENTER);
		textFont(createFont("Arial", inchToPix(.3f))); // sets the font to Arial that is 0.3" tall
		textAlign(CENTER);
		rectMode(CENTER); // draw rectangles not from upper left, but from the center outwards

		initialClick = new PVector(0, 0);

		// don't change this!
		border = inchToPix(2f); // padding of 1.0 inches

		for (int i = 0; i < trialCount; i++) // don't change this!
		{
			Destination d = new Destination();
			d.x = random(border, width - border); // set a random x with some padding
			d.y = random(border, height - border); // set a random y with some padding
			d.rotation = random(0, 360); // random rotation between 0 and 360
			int j = (int) random(20);
			d.z = ((j % 12) + 1) * inchToPix(.25f); // increasing size from .25 up to 3.0"
			destinations.add(d);
			println("created target with " + d.x + "," + d.y + "," + d.rotation + "," + d.z);
		}

		Collections.shuffle(destinations); // randomize the order of the button; don't change this.
	}

	public void draw() {

		background(40); // background is dark grey
		fill(200);
		noStroke();

		// shouldn't really modify this printout code unless there is a really good
		// reason to
		if (userDone) {
			text("User completed " + trialCount + " trials", width / 2, inchToPix(.4f));
			text("User had " + errorCount + " error(s)", width / 2, inchToPix(.4f) * 2);
			text("User took " + (finishTime - startTime) / 1000f / trialCount + " sec per destination", width / 2,
					inchToPix(.4f) * 3);
			text("User took " + ((finishTime - startTime) / 1000f / trialCount + (errorCount * errorPenalty))
					+ " sec per destination inc. penalty", width / 2, inchToPix(.4f) * 4);
			return;
		}

		// ===========DRAW DESTINATION SQUARES=================
		for (int i = trialIndex; i < trialCount; i++) // reduces over time
		{
			pushMatrix();
			Destination d = destinations.get(i); // get destination trial
			translate(d.x, d.y); // center the drawing coordinates to the center of the destination trial
			rotate(radians(d.rotation)); // rotate around the origin of the destination trial
			noFill();
			strokeWeight(3f);
			if (trialIndex == i)
				stroke(255, 0, 0, 192); // set color to semi translucent
			else
				stroke(128, 128, 128, 128); // set color to semi translucent
			rect(0, 0, d.z, d.z);
			popMatrix();
		}

		// ===========DRAW LOGO SQUARE=================
		pushMatrix();
		translate(logoX, logoY); // translate draw center to the center of the logo square
		rotate(radians(logoRotation + activeRotation)); // rotate using the logo square as the origin
		noStroke();
		fill(60, 60, 192, 192);
		rect(0, 0, logoZ + activeZ, logoZ + activeZ);
		popMatrix();

		// ===========DRAW EXAMPLE CONTROLS=================
		fill(255);
		text("Trial " + (trialIndex + 1) + " of " + trialCount, width / 2, inchToPix(.8f));
	}

	boolean clickWithinSquare = false;
	float initialHeading = 0;
	float xClickOffset = 0;
	float yClickOffset = 0;

	public void mousePressed() {
		initialClick.set(mouseX, mouseY);
		PVector logo = new PVector(logoX, logoY);

		xClickOffset = mouseX - logoX;
		yClickOffset = mouseY - logoY;

		clickWithinSquare = PVector.dist(initialClick, logo) < logoZ / 2;

		PVector rotationVector = PVector.sub(initialClick, logo);
		initialHeading = degrees(rotationVector.heading());

		if (startTime == 0) // start time on the instant of the first user click
		{
			startTime = millis();
			println("time started!");
		}
	}

	float scaleSensitivity = 0.8F;

	public void mouseDragged() {
		if (clickWithinSquare) { // If the user clicks within the square, drag it
			logoX = mouseX - xClickOffset;
			logoY = mouseY - yClickOffset;
		} else {
			PVector mouse = new PVector(mouseX, mouseY);
			PVector logo = new PVector(logoX, logoY);

			// Rotation
			PVector rotationVector = PVector.sub(mouse, logo);
			float heading = degrees(rotationVector.heading());
			activeRotation = heading - initialHeading;

			// Scale
			float originalDist = logo.dist(initialClick);
			float curDist = logo.dist(mouse);
			activeZ = (curDist - originalDist) * scaleSensitivity;
		}
	}

	public void mouseReleased() {
		logoRotation += activeRotation;
		activeRotation = 0;

		logoZ += activeZ;
		activeZ = 0;
	}

	// probably shouldn't modify this, but email me if you want to for some good
	// reason.
	public boolean checkForSuccess() {
		Destination d = destinations.get(trialIndex);
		boolean closeDist = dist(d.x, d.y, logoX, logoY) < inchToPix(.05f); // has to be within +-0.05"
		boolean closeRotation = calculateDifferenceBetweenAngles(d.rotation, logoRotation) <= 5;
		boolean closeZ = abs(d.z - logoZ) < inchToPix(.1f); // has to be within +-0.1"

		println("Close Enough Distance: " + closeDist + " (logo X/Y = " + d.x + "/" + d.y + ", destination X/Y = "
				+ logoX + "/" + logoY + ")");
		println("Close Enough Rotation: " + closeRotation + " (rot dist="
				+ calculateDifferenceBetweenAngles(d.rotation, logoRotation) + ")");
		println("Close Enough Z: " + closeZ + " (logo Z = " + d.z + ", destination Z = " + logoZ + ")");
		println("Close enough all: " + (closeDist && closeRotation && closeZ));

		return closeDist && closeRotation && closeZ;
	}

	public void keyPressed() {
		int sensitivity = 2;
		if (key == 'w')
			logoY -= sensitivity;
		if (key == 'a')
			logoX -= sensitivity;
		if (key == 's')
			logoY += sensitivity;
		if (key == 'd')
			logoX += sensitivity;
		if (key == ' ')
			submit();
	}

	public void submit() {
		if (userDone == false && !checkForSuccess())
			errorCount++;

		trialIndex++; // and move on to next trial

		if (trialIndex == trialCount && userDone == false) {
			userDone = true;
			finishTime = millis();
		}
	}

	// utility function I include to calc diference between two angles
	double calculateDifferenceBetweenAngles(float a1, float a2) {
		double diff = abs(a1 - a2);
		diff %= 90;
		if (diff > 45)
			return 90 - diff;
		else
			return diff;
	}

	// utility function to convert inches into pixels based on screen PPI
	float inchToPix(float inch) {
		return inch * screenPPI;
	}
}
