package cs3540;


import java.util.ArrayList;
import java.util.Collections;
import processing.core.PApplet;

public class BakeOff2_OliviaAndDiegoV2 extends PApplet {

	// USER FEEDBACK CODE (Olivia)
	int logoFill = color(60, 60, 192, 192);
	int matchOutline = color(120, 255, 80);

	// CLICK AND CARRY (Olivia) - click square to pick up, click again to drop (drop submits only if matched)
	boolean carrying = false;      // true while the square is attached to the cursor
	float grabDX = 0, grabDY = 0;  // cursor-to-center offset at pickup

	// CORNER DRAG (Diego) - drag a corner handle to rotate + scale together (release submits only if matched)
	boolean draggingCorner = false;
	boolean cornerMoved = false;   // true once the cursor actually moved during a corner drag
	int activeCorner = -1;         // which corner is being dragged (0..3)
	float cornerGrabRadius = 0;    // set in setup (inches -> px)

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

	float logoX = 500;
	float logoY = 500;
	float logoZ = 50f;
	float logoRotation = 0;

	// The argument passed to main must match the class name
	public static void main(String[] args) {
		PApplet.main("cs3540.BakeOff2_OliviaAndDiegoV2");
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
		cornerGrabRadius = inchToPix(0.10f); // how close to grab a corner

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

		// shouldn't really modify this printout code unless there is a really good reason to
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

		// click and carry - square follows cursor
		if (carrying) {
			logoX = mouseX + grabDX;
			logoY = mouseY + grabDY;
		}

		// ===========DRAW LOGO SQUARE (with match feedback)=================
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

		// ===========CENTER DOT + CORNER HANDLES (Diego)=================
		drawHandles();

		// ===========TRIAL TEXT=================
		fill(255);
		noStroke();
		textSize(inchToPix(.3f));
		text("Trial " + (trialIndex + 1) + " of " + trialCount, width / 2, inchToPix(.8f));
	}

	// =============================================================
	// CORNER HANDLES (Diego)
	// =============================================================
	void drawHandles() {
		noStroke();
		fill(255, 255, 255, 160);
		ellipse(logoX, logoY, inchToPix(.08f), inchToPix(.08f));

		for (int i = 0; i < 4; i++) {
			float[] c = getCorner(i);
			boolean active = (draggingCorner && activeCorner == i);
			float r = active ? inchToPix(.14f) : inchToPix(.10f);
			fill(255, 255, 255, active ? 255 : 220);
			stroke(20, 20, 20);
			strokeWeight(2f);
			ellipse(c[0], c[1], r, r);
		}
		noStroke();
	}

	// World-space position of corner i (0..3).
	float[] getCorner(int i) {
		float theta = radians(45 + 90 * i);       // local corner angle (screen y-down)
		float rad = logoZ * sqrt(2) / 2.0f;       // distance center -> corner
		float a = theta + radians(logoRotation);
		return new float[] { logoX + rad * cos(a), logoY + rad * sin(a) };
	}

	// Which corner (0..3) is within grab radius of (mx,my), or -1.
	int cornerNear(float mx, float my) {
		int best = -1;
		float bestD = cornerGrabRadius;
		for (int i = 0; i < 4; i++) {
			float[] c = getCorner(i);
			float dd = dist(mx, my, c[0], c[1]);
			if (dd < bestD) { bestD = dd; best = i; }
		}
		return best;
	}

	// =============================================================
	// INPUT
	// =============================================================
	public void mousePressed() {
		if (userDone) return;
		if (startTime == 0) { startTime = millis(); println("time started!"); }

		// second click drops the square; submits ONLY if it is aligned (green outline)
		if (carrying) {
			// sync position to the cursor so the match check uses the exact drop position
			logoX = mouseX + grabDX;
			logoY = mouseY + grabDY;
			carrying = false;
			if (isMatched()) submitTrial();
			return;
		}

		// corner grab -> rotate + scale together (checked before body so corners win)
		int c = cornerNear(mouseX, mouseY);
		if (c >= 0) {
			draggingCorner = true;
			activeCorner = c;
			cornerMoved = false;
			return;
		}

		// first click on the body picks it up
		if (overLogo(mouseX, mouseY)) {
			carrying = true;
			grabDX = logoX - mouseX;
			grabDY = logoY - mouseY;
		}
	}

	public void mouseDragged() {
		if (draggingCorner) {
			// grabbed corner follows the cursor:
			//   rotation = angle(mouse - center) - baseAngle(corner)
			//   scale    = |mouse - center| * sqrt(2)   (corner distance = z*sqrt(2)/2)
			float dx = mouseX - logoX, dy = mouseY - logoY;
			float d = sqrt(dx * dx + dy * dy);
			if (d > 0.5f) {
				cornerMoved = true;
				float worldAngle = degrees(atan2(dy, dx));
				float baseAngle = 45 + 90 * activeCorner;
				logoRotation = worldAngle - baseAngle;
				logoZ = constrain(d * sqrt(2), (float) 0.01, inchToPix(4f)); // leave min and max alone!
			}
		}
	}

	public void mouseReleased() {
		if (userDone) return;
		// releasing a corner drag submits ONLY if it is aligned (green outline)
		if (draggingCorner && cornerMoved && isMatched()) {
			submitTrial();
		}
		draggingCorner = false;
		activeCorner = -1;
		cornerMoved = false;
	}

	// =============================================================
	// FEEDBACK / VERIFICATION
	// =============================================================
	// Mirrors checkForSuccess() without side effects, used for visual feedback and submit gating
	boolean isMatched() {
		if (trialIndex >= trialCount) return false;
		Destination d = destinations.get(trialIndex);
		boolean closeDist = dist(d.x, d.y, logoX, logoY) < inchToPix(.05f);
		boolean closeRotation = calculateDifferenceBetweenAngles(d.rotation, logoRotation) <= 5;
		boolean closeZ = abs(d.z - logoZ) < inchToPix(.1f);
		return closeDist && closeRotation && closeZ;
	}

	// probably shouldn't modify this
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

	// =============================================================
	// HELPERS
	// =============================================================
	// Hit test that accounts for rotation, with a 0.25" minimum pickup size
	boolean overLogo(float mx, float my) {
		float dx = mx - logoX, dy = my - logoY;
		float a = radians(-logoRotation);
		float lx = dx * cos(a) - dy * sin(a);
		float ly = dx * sin(a) + dy * cos(a);
		float half = max(logoZ / 2, inchToPix(.25f));
		return abs(lx) <= half && abs(ly) <= half;
	}

	// moves on to the next trial
	void submitTrial() {
		if (userDone == false && !checkForSuccess()) errorCount++;
		trialIndex++;
		if (trialIndex == trialCount && userDone == false) {
			userDone = true;
			finishTime = millis();
		}
	}
}