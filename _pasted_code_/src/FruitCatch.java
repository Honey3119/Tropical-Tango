import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class FruitCatch extends JFrame {

private static final int FRAME_WIDTH = 400;
private static final int FRAME_HEIGHT = 600;
private static final int BASKET_WIDTH = 100;
private static final int BASKET_HEIGHT = 30;
private static final int FRUIT_SIZE = 20;
private static final int INITIAL_FRUIT_FALL_RATE = 2;
private static int FRUIT_FALL_SPEED = 2; // Modified to be non-final
private static final int INITIAL_SCORE = 0;
private static final int SCORE_INCREMENT_RED = 5;
private static final int SCORE_DECREMENT_RED = 1;
private static final int SCORE_DECREMENT_GREEN = 3;
private static double GREEN_FRUIT_SPAWN_PROBABILITY = 0.01; // Modified to be non-final
private static final double BOMB_SPAWN_PROBABILITY = 0.005;
private static final int GAME_DURATION = 60000; // Adjusted to 60 seconds

private int basketX;
private int score;
private Timer timer;
private ArrayList<Fruit> fruits;
private Bomb bomb;
private long startTime;

private JPanel mainPanel;
private JButton startButton;

public FruitCatch() {
setTitle("Fruit Catch");
setSize(FRAME_WIDTH, FRAME_HEIGHT);
setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
setResizable(false);

mainPanel = new JPanel(new BorderLayout());
getContentPane().add(mainPanel);

fruits = new ArrayList<>();
initializeGame();

DrawingPanel drawingPanel = new DrawingPanel();
mainPanel.add(drawingPanel, BorderLayout.CENTER);

startButton = new JButton("Start");
startButton.addActionListener(e -> startGame());
mainPanel.add(startButton, BorderLayout.SOUTH);
}

private void startGame() {
mainPanel.remove(startButton);
mainPanel.revalidate();
mainPanel.repaint();

if (timer != null && timer.isRunning()) {
timer.stop();
}
initializeGame();
fruits.clear();
startTime = System.currentTimeMillis();
timer = new Timer(20, e -> {
updateGame();
repaint();
});
timer.start();
}

private void initializeGame() {
basketX = FRAME_WIDTH / 2 - BASKET_WIDTH / 2;
score = INITIAL_SCORE;
}

private void updateGame() {
long elapsedTime = System.currentTimeMillis() - startTime;
int timeLeft = (int) Math.max(0, (GAME_DURATION - elapsedTime) / 1000);

if (timeLeft <= 0) {
gameOver();
return;
}

// Increase spawn rate and fall speed every 50 points
if (score > 0 && score % 50 == 0) {
GREEN_FRUIT_SPAWN_PROBABILITY += 0.01; // Adjust as needed
FRUIT_FALL_SPEED += 1; // Adjust as needed
}

// Spawn bomb with a certain probability
if (Math.random() < BOMB_SPAWN_PROBABILITY && bomb == null) {
bomb = new Bomb();
}

// Spawn fruits
if (Math.random() < GREEN_FRUIT_SPAWN_PROBABILITY) {
fruits.add(new Fruit(true, false));
} else if (new Random().nextInt(100) < 10) { // Adjust the threshold as needed
fruits.add(new Fruit(false, true));
}

// Update fruits
for (int i = fruits.size() - 1; i >= 0; i--) {
Fruit fruit = fruits.get(i);
fruit.fall();

if (fruit.y >= FRAME_HEIGHT) {
fruits.remove(i);
if (!fruit.isRotten) {
if (fruit.isRed) {
score -= SCORE_DECREMENT_RED;
} else {
score -= SCORE_DECREMENT_GREEN;
}
}
} else if (fruit.isCaught(basketX, FRAME_HEIGHT - BASKET_HEIGHT - 30)) {
fruits.remove(i);
if (fruit.isRed) {
score += SCORE_INCREMENT_RED;
} else {
score -= SCORE_DECREMENT_GREEN;
}
}
}

// Check for collision with the bomb
if (bomb != null && bomb.isCaught(basketX, FRAME_HEIGHT - BASKET_HEIGHT - 30)) {
// If bomb is caught, end the game
bomb = null;
gameOver();
return;
}

// Update bomb
if (bomb != null) {
bomb.fall();
if (bomb.y >= FRAME_HEIGHT) {
// If bomb falls off the screen, remove it
bomb = null;
}
}

// Check for score conditions
if (score < 0) {
gameOver();
}
}

private void gameOver() {
if (timer != null && timer.isRunning()) {
timer.stop();
}
int option = JOptionPane.showOptionDialog(this,
"Game Over! Your score: " + score,
"Game Over",
JOptionPane.YES_NO_OPTION,
JOptionPane.PLAIN_MESSAGE,
null,
new String[]{"Restart", "Exit"},
"Restart");
if (option == JOptionPane.YES_OPTION) {
startGame();
} else {
System.exit(0);
}
}

private class DrawingPanel extends JPanel {
public DrawingPanel() {
setFocusable(true);
addMouseMotionListener(new MouseAdapter() {
@Override
public void mouseDragged(MouseEvent e) {
moveBasket(e.getX());
}
});
}

@Override
protected void paintComponent(Graphics g) {
super.paintComponent(g);

// Draw inverted trapezoid as the basket
int[] xPoints = {basketX, basketX + BASKET_WIDTH, basketX + BASKET_WIDTH - 20, basketX + 20};
int[] yPoints = {FRAME_HEIGHT - BASKET_HEIGHT - 30, FRAME_HEIGHT - BASKET_HEIGHT - 30,
FRAME_HEIGHT - 30, FRAME_HEIGHT - 30};
g.setColor(new Color(165, 42, 42));
g.fillPolygon(xPoints, yPoints, 4);

// Draw bomb if exists
if (bomb != null) {
g.setColor(Color.BLACK);
g.fillRect(bomb.x, bomb.y, FRUIT_SIZE, FRUIT_SIZE);
}

// Draw fruits
for (Fruit fruit : fruits) {
if (fruit.isRotten) {
g.setColor(Color.GREEN);
} else {
g.setColor(Color.RED);
}
g.fillOval(fruit.x, fruit.y, FRUIT_SIZE, FRUIT_SIZE);
}

// Draw score and time left
if (timer != null && timer.isRunning()) {
Font defaultFont = g.getFont();
float newSize = defaultFont.getSize() * 1.06f;
Font newFont = defaultFont.deriveFont(newSize);
g.setFont(newFont);
FontMetrics metrics = g.getFontMetrics(newFont);
int scoreX = 20;
int scoreY = (int) (20 + metrics.getHeight() * 1.06f);
g.setColor(Color.BLACK);
g.drawString("Score: " + score, scoreX, scoreY);

String timeLeftStr = "Time Left: " + (int) Math.ceil((GAME_DURATION - (System.currentTimeMillis() - startTime)) / 1000.0);
int timeLeftX = getWidth() - metrics.stringWidth(timeLeftStr) - 20;
int timeLeftY = scoreY;
g.drawString(timeLeftStr, timeLeftX, timeLeftY);
}
}
}

private void moveBasket(int mouseX) {
basketX = mouseX - BASKET_WIDTH / 2;
if (basketX < 0) {
basketX = 0;
} else if (basketX + BASKET_WIDTH > FRAME_WIDTH) {
basketX = FRAME_WIDTH - BASKET_WIDTH;
}
repaint();
}

private class Fruit {
int x;
int y;
int speed;
boolean isRotten;
boolean isRed;

public Fruit(boolean isRotten, boolean isRed) {
Random random = new Random();
x = random.nextInt(FRAME_WIDTH - FRUIT_SIZE);
y = 0;
speed = FRUIT_FALL_SPEED;
this.isRotten = isRotten;
this.isRed = isRed;
}

public void fall() {
y += speed;
}

public boolean isCaught(int basketX, int basketY) {
return (x >= basketX && x <= basketX + BASKET_WIDTH) && (y >= basketY && y <= basketY + BASKET_HEIGHT);
}
}

private class Bomb {
int x;
int y;
int speed;

public Bomb() {
Random random = new Random();
x = random.nextInt(FRAME_WIDTH - FRUIT_SIZE);
y = 0;
speed = FRUIT_FALL_SPEED * 2; // Double the fall speed of the bomb
}

public void fall() {
y += speed;
}

public boolean isCaught(int basketX, int basketY) {
return (x >= basketX && x <= basketX + BASKET_WIDTH) && (y >= basketY && y <= basketY + BASKET_HEIGHT);
}
}

public static void main(String[] args) {
SwingUtilities.invokeLater(() -> new FruitCatch().setVisible(true));
}
}
