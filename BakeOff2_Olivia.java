package cs3540;

import java.util.ArrayList;
import java.util.Collections;
import processing.core.PApplet;

public class BakeOff2_Olivia extends PApplet {
	
	// USER FEEDBACK CODE
	int logoFill = color(60, 60, 192, 192);
	int matchOutline = color(120, 255, 80);
	
	// CLICK AND DRAG
	boolean carrying = false;      // true while the square is attached to the cursor
	float grabDX = 0, grabDY = 0;  // cursor-to-center offset at pickup

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

	// The argument passed to main must match the class name
	public static void main(String[] args) {
		// Tell processing what class we want to run.
		PApplet.main("cs3540.BakeOff2_Olivia");
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

		// click and drag
		// EDITED - square also freezes over the submit button
		if (carrying) {
			if (inControlZone(mouseX, mouseY) || overSubmit(mouseX, mouseY)) {
				// freeze over the rotate/size corners or submit button, re-capture offset so it won't jump
				grabDX = logoX - mouseX;
				grabDY = logoY - mouseY;
			} else {
				logoX = mouseX + grabDX;
				logoY = mouseY + grabDY;
			}
		}
		
		// EDITED - user feedback
		// ===========DRAW LOGO SQUARE=================
		pushMatrix();
		translate(logoX, logoY);
		rotate(radians(logoRotation));
		if (isMatched()) {
			stroke(matchOutline);   // bright green outline when it will count as a match
			strokeWeight(6f);
		} else {
			noStroke();
		}
		fill(logoFill);
		rect(0, 0, logoZ, logoZ);
		popMatrix();

		// ===========DRAW EXAMPLE CONTROLS=================
		fill(255);
		scaffoldControlLogic(); // you are going to want to replace this!
		drawSubmitButton(); // NEW - submit button
		fill(255);
		text("Trial " + (trialIndex + 1) + " of " + trialCount, width / 2, inchToPix(.8f));
	}

	// my example design for control, which is terrible
	// EDITED - only rotate and size remain, movement is click-to-carry now
	void scaffoldControlLogic() {
		// upper left corner, rotate counterclockwise
		text("CCW", inchToPix(.4f), inchToPix(.4f));
		if (mousePressed && dist(0, 0, mouseX, mouseY) < inchToPix(.8f))
			logoRotation--;

		// upper right corner, rotate clockwise
		text("CW", width - inchToPix(.4f), inchToPix(.4f));
		if (mousePressed && dist(width, 0, mouseX, mouseY) < inchToPix(.8f))
			logoRotation++;

		// lower left corner, decrease Z
		text("-", inchToPix(.4f), height - inchToPix(.4f));
		if (mousePressed && dist(0, height, mouseX, mouseY) < inchToPix(.8f))
			logoZ = constrain(logoZ - inchToPix(.02f), (float) .01, inchToPix(4f)); // leave min and max alone!

		// lower right corner, increase Z
		text("+", width - inchToPix(.4f), height - inchToPix(.4f));
		if (mousePressed && dist(width, height, mouseX, mouseY) < inchToPix(.8f))
			logoZ = constrain(logoZ + inchToPix(.02f), (float) .01, inchToPix(4f)); // leave min and max alone!
	}

	// completely edited
	public void mousePressed() {
		if (userDone) return;
		if (startTime == 0) { startTime = millis(); println("time started!"); }

		// NEW - submit button: only this submits, and it also ends any carry
		if (overSubmit(mouseX, mouseY)) {
			carrying = false;
			submitTrial();
			return;
		}

		if (inControlZone(mouseX, mouseY)) return;  // corner presses are controls, not pickup/drop

		if (carrying) {
			carrying = false;                 // second click drops
		} else if (overLogo(mouseX, mouseY)) {
			carrying = true;                  // first click picks up
			grabDX = logoX - mouseX;
			grabDY = logoY - mouseY;
		}
	}

	//DONT NEED
//	public void mouseReleased() {
//		// check to see if user clicked middle of screen within 3 inches, which this
//		// code uses as a submit button
//		if (dist(width / 2, height / 2, mouseX, mouseY) < inchToPix(3f)) {
//			if (userDone == false && !checkForSuccess())
//				errorCount++;
//
//			trialIndex++; // and move on to next trial
//
//			if (trialIndex == trialCount && userDone == false) {
//				userDone = true;
//				finishTime = millis();
//			}
//		}
//	}
	
	//EDITED - this is for the user feedback idea
	// Mirrors checkForSuccess() without side effects, used only for visual feedback
	boolean isMatched() {
		if (trialIndex >= trialCount) return false;
		Destination d = destinations.get(trialIndex);
		boolean closeDist = dist(d.x, d.y, logoX, logoY) < inchToPix(.05f);
		boolean closeRotation = calculateDifferenceBetweenAngles(d.rotation, logoRotation) <= 5;
		boolean closeZ = abs(d.z - logoZ) < inchToPix(.1f);
		return closeDist && closeRotation && closeZ;
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
	
	//click and drag helpers
	// True over the four corner controls (rotate/size)
	boolean inControlZone(float mx, float my) {
		float r = inchToPix(.8f);
		return dist(0, 0, mx, my) < r || dist(width, 0, mx, my) < r
				|| dist(0, height, mx, my) < r || dist(width, height, mx, my) < r;
	}

	// Hit test that accounts for rotation, with a 0.25" minimum pickup size
	boolean overLogo(float mx, float my) {
		float dx = mx - logoX, dy = my - logoY;
		float a = radians(-logoRotation);
		float lx = dx * cos(a) - dy * sin(a);
		float ly = dx * sin(a) + dy * cos(a);
		float half = max(logoZ / 2, inchToPix(.25f));
		return abs(lx) <= half && abs(ly) <= half;
	}
	
	// moves on to the next trial (same logic as the old mouseReleased submit)
	void submitTrial() {
		if (userDone == false && !checkForSuccess()) errorCount++;
		trialIndex++;
		if (trialIndex == trialCount && userDone == false) {
			userDone = true;
			finishTime = millis();
		}
	}

	// NEW - submit button helpers (bottom center)
	float submitW()  { return inchToPix(1.6f); }
	float submitH()  { return inchToPix(.5f); }
	float submitCY() { return height - inchToPix(.4f); }

	boolean overSubmit(float mx, float my) {
		return abs(mx - width / 2) < submitW() / 2 && abs(my - submitCY()) < submitH() / 2;
	}

	void drawSubmitButton() {
		stroke(255);
		strokeWeight(2f);
		fill(overSubmit(mouseX, mouseY) ? color(90) : color(60));
		rect(width / 2, submitCY(), submitW(), submitH());
		noStroke();
		fill(255);
		text("SUBMIT", width / 2, submitCY() + inchToPix(.1f));
	}
}