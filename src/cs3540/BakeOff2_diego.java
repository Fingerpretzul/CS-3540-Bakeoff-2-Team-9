package cs3540;

import java.util.ArrayList;
import java.util.Collections;
import processing.core.PApplet;

public class BakeOff2_diego extends PApplet {

// ===== variables you should probably leave alone =====
int index = 0;
float border = 0;
int trialCount = 12;
int trialIndex = 0;
int errorCount = 0;
float errorPenalty = 0.5f;
int startTime = 0;
int finishTime = 0;
boolean userDone = false;
final int screenPPI = 72;

// State of the cursor square (what we control)
float logoX = 500;
float logoY = 500;
float logoZ = 50f;
float logoRotation = 0;

// ===== my control state =====
static final int MODE_NONE   = 0;
static final int MODE_BODY   = 1;
static final int MODE_CORNER = 2;
int    dragMode     = MODE_NONE;
int    activeCorner = -1;      // which corner is being dragged (0..3)
float  grabOffX     = 0;       // body drag: mouse - center at grab time
float  grabOffY     = 0;
boolean wasDragged  = false;   // was this press a drag (vs a clean click)?

float cornerGrabRadius = 0;    // set in setup (inches -> px)

public static void main(String[] args) {
PApplet.main("cs3540.BakeOff2_diego");
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
textFont(createFont("Arial", inchToPix(.3f)));
textAlign(CENTER);
rectMode(CENTER);

border = inchToPix(2f);                  // leave this alone
cornerGrabRadius = inchToPix(0.10f);     // how close to grab a corner

for (int i = 0; i < trialCount; i++)     // leave this alone
{
Destination d = new Destination();
d.x = random(border, width - border);
d.y = random(border, height - border);
d.rotation = random(0, 360);
int j = (int) random(20);
d.z = ((j % 12) + 1) * inchToPix(.25f);
destinations.add(d);
println("created target with " + d.x + "," + d.y + "," + d.rotation + "," + d.z);
}
Collections.shuffle(destinations);       // leave this alone
}

// =============================================================
public void draw() {
background(40);
fill(200);
noStroke();

if (userDone) {
text("User completed " + trialCount + " trials", width / 2, inchToPix(.4f));
text("User had " + errorCount + " error(s)", width / 2, inchToPix(.4f) * 2);
text("User took " + (finishTime - startTime) / 1000f / trialCount + " sec per destination", width / 2,
inchToPix(.4f) * 3);
text("User took " + ((finishTime - startTime) / 1000f / trialCount + (errorCount * errorPenalty))
+ " sec per destination inc. penalty", width / 2, inchToPix(.4f) * 4);
return;
}

// =========== DRAW DESTINATION SQUARES ===========
for (int i = trialIndex; i < trialCount; i++)
{
pushMatrix();
Destination d = destinations.get(i);
translate(d.x, d.y);
rotate(radians(d.rotation));
noFill();
strokeWeight(3f);
if (trialIndex == i) stroke(255, 0, 0, 192);
else                 stroke(128, 128, 128, 128);
rect(0, 0, d.z, d.z);
popMatrix();
}

// =========== CURSOR SQUARE + CORNER HANDLES ===========
drawCursorSquare();

// =========== HINTS ===========
fill(255);
noStroke();
textSize(inchToPix(.18f));
//text("Drag BODY = move (X/Y)      Drag a CORNER = rotate + scale together", width / 2, height - inchToPix(.2f));
//text("Click near CENTER of screen to submit", width / 2, height - inchToPix(.5f));
text("Trial " + (trialIndex + 1) + " of " + trialCount, width / 2, inchToPix(.8f));
}

// =============================================================
//  DRAWING
// =============================================================
void drawCursorSquare() {
pushMatrix();
translate(logoX, logoY);
rotate(radians(logoRotation));
noStroke();
fill(60, 60, 192, 192);
rect(0, 0, logoZ, logoZ);
popMatrix();

noStroke();
fill(255, 255, 255, 160);
ellipse(logoX, logoY, inchToPix(.08f), inchToPix(.08f));

for (int i = 0; i < 4; i++) {
float[] c = getCorner(i);
boolean active = (dragMode == MODE_CORNER && activeCorner == i);
float r = (active) ? inchToPix(.14f) : inchToPix(.10f);
fill(255, 255, 255, active ? 255 : 220);
stroke(20, 20, 20);
strokeWeight(2f);
ellipse(c[0], c[1], r, r);
noStroke();
}
}

// World-space position of corner i (0..3).
float[] getCorner(int i) {
float theta = radians(45 + 90 * i);     // local corner angle (screen y-down)
float rad   = logoZ * sqrt(2) / 2.0f;     // distance center -> corner
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

// Is (mx,my) inside the rotated square body?
boolean insideBody(float mx, float my) {
float dx = mx - logoX, dy = my - logoY;
float a  = -radians(logoRotation);
float lx = dx * cos(a) - dy * sin(a);
float ly = dx * sin(a) + dy * cos(a);
return abs(lx) <= logoZ / 2 && abs(ly) <= logoZ / 2;
}

// =============================================================
//  INPUT  (single-touch / trackpad mouse only)
// =============================================================
public void mousePressed() {
if (startTime == 0) { startTime = millis(); println("time started!"); }

wasDragged   = false;
activeCorner = -1;

// 1) Corner grab  -> rotation + scale together
int c = cornerNear(mouseX, mouseY);
if (c >= 0) { dragMode = MODE_CORNER; activeCorner = c; println("grabbed corner " + c); return; }

// 2) Body grab  -> translate
if (insideBody(mouseX, mouseY)) {
dragMode = MODE_BODY;
grabOffX = mouseX - logoX;
grabOffY = mouseY - logoY;
return;
}

dragMode = MODE_NONE;   // possible clean submit click
}

public void mouseDragged() {
if (dragMode == MODE_BODY) {
logoX = mouseX - grabOffX;
logoY = mouseY - grabOffY;
wasDragged = true;
}
else if (dragMode == MODE_CORNER) {
// grabbed corner follows the cursor:
//   rotation = angle(mouse - center) - baseAngle(corner)
//   scale    = |mouse - center| * sqrt(2)   (corner distance = z*sqrt(2)/2)
float dx = mouseX - logoX, dy = mouseY - logoY;
float d  = sqrt(dx * dx + dy * dy);
if (d > 0.5f) {
float worldAngle = degrees(atan2(dy, dx));
float baseAngle  = 45 + 90 * activeCorner;
logoRotation     = worldAngle - baseAngle;
logoZ            = constrain(d * sqrt(2), (float) 0.01, inchToPix(4f));
}
wasDragged = true;
}
}

public void mouseReleased() {
// submit only on a clean click (not a drag) near screen center
if (!wasDragged && dist(width / 2, height / 2, mouseX, mouseY) < inchToPix(3f)) {
if (userDone == false && !checkForSuccess()) errorCount++;
trialIndex++;
if (trialIndex == trialCount && userDone == false) { userDone = true; finishTime = millis(); }
}
dragMode = MODE_NONE;
activeCorner = -1;
}

// =============================================================
//  VERIFICATION  (DO NOT MODIFY)
// =============================================================
public boolean checkForSuccess() {
Destination d = destinations.get(trialIndex);
boolean closeDist = dist(d.x, d.y, logoX, logoY) < inchToPix(.05f);
boolean closeRotation = calculateDifferenceBetweenAngles(d.rotation, logoRotation) <= 5;
boolean closeZ = abs(d.z - logoZ) < inchToPix(.1f);
println("Close Enough Distance: " + closeDist + " (logo X/Y = " + d.x + "/" + d.y + ", destination X/Y = "
+ logoX + "/" + logoY + ")");
println("Close Enough Rotation: " + closeRotation + " (rot dist="
+ calculateDifferenceBetweenAngles(d.rotation, logoRotation) + ")");
println("Close Enough Z: " + closeZ + " (logo Z = " + d.z + ", destination Z = " + logoZ + ")");
println("Close enough all: " + (closeDist && closeRotation && closeZ));
return closeDist && closeRotation && closeZ;
}

double calculateDifferenceBetweenAngles(float a1, float a2) {
double diff = abs(a1 - a2);
diff %= 90;
if (diff > 45) return 90 - diff;
else return diff;
}

float inchToPix(float inch) { return inch * screenPPI; }
}