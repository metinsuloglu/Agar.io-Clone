import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

public class GamePanel extends JPanel implements ActionListener, MouseListener, KeyListener {
	
	private DataPanel dataPanel;
	private long tStart;
	
	private Circle player;
	private List<Circle> opponents;
	private List<Food> foods;
	
	private double viewX;
	private double viewY;
	
	private Timer gameTimer;
	
	private RandomAccessFile highScoreFile;
	private double highScore;
	private JLabel highScoreLabel;
	
	private Multiplayer network;
	private Thread receiver;
	private Alert receivedAlert;
	
	private JPanel alignmentPanel;
	private DefaultListModel<String> listModel;
	private JList<String> leaderboard;
	
	private boolean mouseIsPressed;
	private int keyPressed;
	private boolean twoPlayersJoined;
	private String name;
	
	private Random rand;
	private Point spawnPoint;
	
	private JLabel warningLabel;
	
	private final String HOST;
	
	private Image backgroundImage;
	private InputStream inputStream;
	
	public GamePanel(DataPanel dataPanel, String name, final String HOST) {

		this.dataPanel = dataPanel;
		this.name = name;
		this.HOST = HOST;
		
		setLayout(new BorderLayout());

		try {
			inputStream = getClass().getResourceAsStream("/background.png");
			if(inputStream!=null) backgroundImage = ImageIO.read(inputStream);
			else System.out.println("Couldn't find background image.");
		} catch (IOException e) {
			System.out.println("Couldn't load background image.");
			e.printStackTrace();
		}
		
		rand = new Random();
		
		gameTimer = new Timer(GameConstants.GAME_TIMER_DELAY, this);
		
		addMouseListener(this);
		addKeyListener(this);
		
		setUpGame();
	}
	
	private void setUpGame() {

		mouseIsPressed = false;
		keyPressed = 0;
		twoPlayersJoined = false;

		spawnPoint = randomPoint();
		
		foods = Collections.synchronizedList(new ArrayList<Food>());
		opponents = Collections.synchronizedList(new ArrayList<Circle>());
		player = new Circle(GameConstants.START_SIZE, spawnPoint.getX(), spawnPoint.getY(), randomColor(), GameConstants.START_VELOCITY, name);
		viewX = spawnPoint.getX() - GameConstants.SCREEN_SIZE/2;
		viewY = spawnPoint.getY() - GameConstants.SCREEN_SIZE/2;
		
		try {
			highScoreFile = new RandomAccessFile(new File("highScore.dat"), "rw");
			highScore = highScoreFile.readDouble();
		} catch (FileNotFoundException e) {
			highScore = BigDecimal.valueOf(player.getSize()).setScale(1, RoundingMode.HALF_UP).doubleValue();
		} catch (IOException io) {
			highScore = BigDecimal.valueOf(player.getSize()).setScale(1, RoundingMode.HALF_UP).doubleValue();
		}

		highScoreLabel = new JLabel("Highscore: " + highScore);
		highScoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		highScoreLabel.setFont(new Font("Arial", Font.BOLD, 18));
		highScoreLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(highScoreLabel, BorderLayout.SOUTH);

		network = new Multiplayer(HOST);
		
		network.writeAlert(new Alert("JOIN", player));

		receiver = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					receivedAlert = network.readAlert();
					
					if(receivedAlert.getType().equals("FOODS")) {
						foods = (List<Food>) receivedAlert.getData();
					} 
					else if(receivedAlert.getType().equals("OPPONENTS")) {
						List<Circle> opponentList = (List<Circle>) receivedAlert.getData();
						if(opponentList.size() != 0) twoPlayersJoined = true;
						opponents = opponentList;
					} 
					else if(receivedAlert.getType().equals("JOIN")) {
						twoPlayersJoined = true;
						opponents.add((Circle) receivedAlert.getData());
					} 
					else if(receivedAlert.getType().equals("CHANGE")) {
						Circle playerChanged = (Circle) receivedAlert.getData();
						synchronized(opponents) {
							for(Iterator<Circle> iterator = opponents.iterator(); iterator.hasNext();) {
								Circle opponent = iterator.next();
								if(opponent.getName().equals(playerChanged.getName())) {
									iterator.remove();
									opponents.add(playerChanged);
									break;
								}
							}
						}
					} 
					else if(receivedAlert.getType().equals("FOOD_EATEN")) {
						Food foodEaten = (Food) receivedAlert.getData();
						synchronized(foods) {
							for(Iterator<Food> iterator = foods.iterator(); iterator.hasNext();) {
								Food food = iterator.next();
								if(food.getX() == foodEaten.getX() && food.getY() == foodEaten.getY()) {
									iterator.remove();
									break;
								}
							}
						}
					}
					else if(receivedAlert.getType().equals("FOOD_ADDED")) {
						Food foodAdded = (Food) receivedAlert.getData();
						foods.add(foodAdded);
					} 
					else if (receivedAlert.getType().equals("OPPONENT_EATEN")) {
						Circle opponentEaten = (Circle) receivedAlert.getData();
						synchronized(opponents) {
							for(Iterator<Circle> iterator = opponents.iterator(); iterator.hasNext();) {
								Circle opponent = iterator.next();
								if(opponent.getName().equals(opponentEaten.getName())) {
									iterator.remove();
									break;
								}
							}
						}
					}			
					else if(receivedAlert.getType().equals("GAME_OVER")) {
						gameOver(false);
						break;
					}

					if(opponents.size() == 0 && twoPlayersJoined) {
						gameOver(true);
						break;
					}
				}
			}
		});
		receiver.start();

		alignmentPanel = new JPanel();
		alignmentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		alignmentPanel.setOpaque(false);
		add(alignmentPanel, BorderLayout.NORTH);
		listModel = new DefaultListModel<String>();
		leaderboard = new JList<>(listModel);
		leaderboard.setFont(new Font("Arial", Font.PLAIN, 18));
		leaderboard.setBackground(Color.LIGHT_GRAY);
		alignmentPanel.add(leaderboard, BorderLayout.NORTH);
	
		warningLabel = new JLabel("Get back into the arena!");
		warningLabel.setFont(new Font("Arial", Font.BOLD, 50));
		warningLabel.setForeground(Color.RED);
		warningLabel.setHorizontalAlignment(SwingConstants.CENTER);
		warningLabel.setVerticalAlignment(SwingConstants.CENTER);

		gameTimer.start();
		
		tStart = System.currentTimeMillis();
	}

	public void gameOver(boolean won) {
		gameTimer.stop();
		twoPlayersJoined = false;
		network.closeConnection();
		remove(highScoreLabel);
		remove(alignmentPanel);
		if(won) {
			String[] options = {"Yes, of course!", "No, thank you."};
			int option = JOptionPane.showOptionDialog(this, "You won!\nThere are no other players left.\nRestart?", "GAME OVER.", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			if(option == JOptionPane.YES_OPTION) setUpGame();
			else if(option == JOptionPane.NO_OPTION) closeGame();
		} else {
			String[] options = {"Oh no!"};
			JOptionPane.showOptionDialog(this, "You lost!\nYou were eaten in " + (int)(System.currentTimeMillis() - tStart)/1000 + " s.", "GAME OVER.", JOptionPane.OK_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			closeGame();
		}
	}
	
	public void closeGame() {
		JOptionPane.showMessageDialog(this, "Thanks for playing!\nBy Liana Nassanova and Metin Suloglu");
		System.exit(0);
	}
	
	private Color randomColor() {
		int r = rand.nextInt(256);
		int g = rand.nextInt(256);
		int b = rand.nextInt(256);
		return new Color(r,g,b);
	}
	
	private Point randomPoint() {
		return new Point(rand.nextInt(GameConstants.BOARD_SIZE+1), rand.nextInt(GameConstants.BOARD_SIZE+1));
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.translate(-1*viewX, -1*viewY);
		
		/*---DRAW BOUNDARY---*/
		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(5));
		g2.drawRect(0, 0, GameConstants.BOARD_SIZE, GameConstants.BOARD_SIZE);
		
		/*---DRAW BACKGROUND IMAGE---*/
		if(backgroundImage!=null)
			g2.drawImage(backgroundImage, 0, 0, GameConstants.BOARD_SIZE, GameConstants.BOARD_SIZE, 0, 0, backgroundImage.getWidth(null), backgroundImage.getHeight(null), null);
		
		/*---DRAW OPPONENTS AND CHECK COLLISION---*/
		synchronized(opponents) {
			for(Iterator<Circle> iterator = opponents.iterator(); iterator.hasNext();) {
				Circle opponent = iterator.next();
				opponent.drawToScreen(g2);
				if(player.getSize() > opponent.getSize() && player.intersects(opponent)) {
					iterator.remove();
					network.writeAlert(new Alert("OPPONENT_EATEN", opponent));
					
					player.setSize(player.getSize() + opponent.getSize()/2);
					player.setVelocity(player.getVelocity()/1.005);

					network.writeAlert(new Alert("CHANGE", player));
				
					checkHighScore();
				}
			}
		}
		
		/*---DRAW FOODS AND CHECK COLLISION---*/
		synchronized(foods) {
			for(Iterator<Food> iterator = foods.iterator(); iterator.hasNext();) {
				Food food = iterator.next();
				if(isOnScreen(food)) {
					food.drawToScreen(g2);
					if(player.intersects(food)) {
						iterator.remove();
						network.writeAlert(new Alert("FOOD_EATEN", food));
						
						if(!(food instanceof NonFood)) {
							player.setSize(player.getSize() + food.getValue()/2);
							player.setVelocity(player.getVelocity()/1.002);
						} else {
							player.setSize(player.getSize() + food.getValue()*4);
						}
						
						network.writeAlert(new Alert("CHANGE", player));
						
						checkHighScore();
						
					}
				}
			}
		}

		/*---DRAW PLAYER---*/
		player.drawToScreen(g2);
		
		g2.translate(viewX, viewY);
		
		dataPanel.updateLabels(player.getSize(), player.getVelocity(), tStart);
		highScoreLabel.setText("Highscore: " + highScore);
	}
	
	private void checkHighScore() {
		if (player.getSize() > highScore){
			highScore = BigDecimal.valueOf(player.getSize()).setScale(1, RoundingMode.HALF_UP).doubleValue();
			try {
				highScoreFile.seek(0);
				highScoreFile.writeDouble(highScore);
			} catch (IOException e) {
				System.out.println("Error: Couldn't save high score");
			}
		}
	}
	
	private boolean isOnScreen(Entity e) {
		return e.getX()+e.getBounds().getWidth()/2 > viewX && e.getX()-e.getBounds().getWidth()/2 < viewX + getWidth() && e.getY()+e.getBounds().getHeight()/2 > viewY && e.getY()-e.getBounds().getHeight()/2 < viewY+getHeight();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(keyPressed != 0) {
			switch(keyPressed) {
			case 1:
				moveTowards(player.getX(), player.getY()-1);
				break;
			case 2:
				moveTowards(player.getX()+1, player.getY());
				break;
			case 3:
				moveTowards(player.getX(), player.getY()+1);
				break;
			case 4:
				moveTowards(player.getX()-1, player.getY());
				break;
			}

			network.writeAlert(new Alert("CHANGE", player));
		} else if(mouseIsPressed) {
			Point2D mousePosition = getMousePosition();
			if(mousePosition == null) return;
		
			moveTowards(mousePosition.getX() + viewX, mousePosition.getY() + viewY);

			network.writeAlert(new Alert("CHANGE", player));
		}
		
		/*---SORT LEADERBOARD---*/
		listModel.clear();
		List<Circle> players = new ArrayList<Circle>();
		players.add(player);
		synchronized(opponents){
			for(Iterator<Circle> iterator = opponents.iterator(); iterator.hasNext();){
				Circle opponent = iterator.next();
				players.add(opponent);
			}
		}
		Collections.sort(players, new Comparator<Circle>() {
			public int compare(Circle c1, Circle c2) {
				if(c2.getSize() > c1.getSize()) return 1;
				else if(c2.getSize() == c1.getSize()) return 0;
				else return -1;
			}
		});
		for(Circle player: players) {
			listModel.addElement(player.getName() + " : " + BigDecimal.valueOf(player.getSize()).setScale(1, RoundingMode.HALF_UP).doubleValue());
		}
		
		repaint();
	}

	public void moveTowards(double x, double y) {
		double dx = x - player.getX();
		double dy = y - player.getY();
		double distance = Math.sqrt(dx*dx + dy*dy);
		
		double movementX = player.getVelocity()/10*dx/distance;
		double movementY = player.getVelocity()/10*dy/distance;
		
		player.setX(player.getX() + movementX);
		player.setY(player.getY() + movementY);

		viewX += movementX;
		viewY += movementY;
		
		if(player.getX()-player.getSize()/2 < 0) { player.setX(player.getSize()/2); viewX = player.getSize()/2 - GameConstants.SCREEN_SIZE/2; }
		if(player.getX()+player.getSize()/2 > GameConstants.BOARD_SIZE) { player.setX(GameConstants.BOARD_SIZE-player.getSize()/2); viewX = GameConstants.BOARD_SIZE-player.getSize()/2 - GameConstants.SCREEN_SIZE/2; }
		if(player.getY()-player.getSize()/2 < 0) { player.setY(player.getSize()/2); viewY = player.getSize()/2 - GameConstants.SCREEN_SIZE/2; }
		if(player.getY()+player.getSize()/2 > GameConstants.BOARD_SIZE) { player.setY(GameConstants.BOARD_SIZE-player.getSize()/2); viewY = GameConstants.BOARD_SIZE-player.getSize()/2 - GameConstants.SCREEN_SIZE/2; }
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseIsPressed = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouseIsPressed = false;
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		int c = e.getKeyCode();
		if(c == KeyEvent.VK_LEFT){
			keyPressed = 4;
		}
		if(c == KeyEvent.VK_UP){
			keyPressed = 1;
		}
		if(c == KeyEvent.VK_RIGHT){
			keyPressed = 2;
		}
		if(c == KeyEvent.VK_DOWN){
			keyPressed = 3;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		keyPressed = 0;
	}
}
