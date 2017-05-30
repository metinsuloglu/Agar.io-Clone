import java.awt.BorderLayout;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.Timer;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Server extends JFrame {
	
	public static List<ClientConnection> clients;
	public static List<Circle> playersInGame;
	public static List<Food> foods;
	
	private Timer spawnFood;
	private FoodSpawner foodSpawner;
	
	public static final int PORT = 24680;
	
	private ServerSocket serverSocket;
	private Socket socket;
	
	ClientConnection newClient;
	Thread newThread;
	
	public static JScrollPane scrollPane;
	public static JTextArea textArea;
	public static int nextID;

	public Server() {
		
		super("Server");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(400,300);
		setLayout(new BorderLayout());
		
		nextID = 0;
		
		clients = Collections.synchronizedList(new ArrayList<ClientConnection>());
		playersInGame = Collections.synchronizedList(new ArrayList<Circle>());
		foods = Collections.synchronizedList(new ArrayList<Food>());
		
		textArea = new JTextArea(5,20);
		textArea.setEditable(false);
		scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		add(scrollPane, BorderLayout.CENTER);

		setVisible(true);
		
		foodSpawner = new FoodSpawner();
		for(int i=0; i<GameConstants.MAX_NUM_FOODS;i++) {
			foodSpawner.spawnFood();
		}

		spawnFood = new Timer(GameConstants.FOOD_SPAWN_DELAY, foodSpawner);
		
		try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
			textArea.append("Couldn't create server socket.\n");
			e.printStackTrace();
		}
		
		spawnFood.start();
		
		while(true) {
			try {
				textArea.append("-x-x-x-x-x-x-x-x-x-x-x-x-\nWaiting for connection...\n-x-x-x-x-x-x-x-x-x-x-x-x-\n");
				socket = serverSocket.accept();
				
				textArea.append("New connection from " + socket.getInetAddress().getHostName() + ".\n");
				newClient = new ClientConnection(socket);
				clients.add(newClient);
				
				newThread = new Thread(newClient);
				newThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		new Server();
	}
	
	public static void addFood(Food food) {
		foods.add(food);
		synchronized(clients) {
			for(Iterator<ClientConnection> iterator = clients.iterator(); iterator.hasNext();) {
				ClientConnection client = iterator.next();
				client.sendAlert(new Alert("FOOD_ADDED", food));
			}
		}
	}

}

class ClientConnection implements Runnable {
	
	private Socket socket;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	private String name;
	
	private boolean gameIsRunning = true;
	
	private Alert receivedAlert;
	
	public ClientConnection(Socket socket) {
		this.socket = socket;
		setUpStreams();
	}
	
	public void setUpStreams() {
		try {
			Server.textArea.append("Setting up streams for new client...\n");
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());
			Server.textArea.append("Finished setting up streams.\n");
		} catch (IOException e) {
			Server.textArea.append("ERROR: Setting up streams failed.\n");
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while(gameIsRunning) {
			try {
				receivedAlert = (Alert) inputStream.readObject();
				
				if(receivedAlert.getType().equals("JOIN")) {
					Circle joinedPlayer = (Circle) receivedAlert.getData();
					name = joinedPlayer.getName();
					
					sendAlert(new Alert("FOODS", Server.foods));
					sendAlert(new Alert("OPPONENTS", Server.playersInGame));
					
					Server.playersInGame.add(joinedPlayer);
					
					sendToAllExceptSelf(receivedAlert);
				}
				else if(receivedAlert.getType().equals("CHANGE")) {
					Circle playerChanged = (Circle) receivedAlert.getData();
					synchronized(Server.playersInGame) {
						for(Iterator<Circle> iterator = Server.playersInGame.iterator(); iterator.hasNext();) {
							Circle player = iterator.next();
							if(player.getName().equals(playerChanged.getName())) {
								iterator.remove();
								Server.playersInGame.add(playerChanged);
								sendToAllExceptSelf(receivedAlert);
								break;
							}
						}
					}
				}
				else if(receivedAlert.getType().equals("FOOD_EATEN")) {
					Food foodEaten = (Food) receivedAlert.getData();
					synchronized(Server.foods) {
						for(Iterator<Food> iterator = Server.foods.iterator(); iterator.hasNext();) {
							Food food = iterator.next();
							if(food.getX() == foodEaten.getX() && food.getY() == foodEaten.getY()) {
								iterator.remove();
								sendToAllExceptSelf(receivedAlert);
								break;
							}
						}
					}
				}
				else if(receivedAlert.getType().equals("OPPONENT_EATEN")) {
					Circle opponentEaten = (Circle) receivedAlert.getData();
					synchronized(Server.playersInGame) {
						for(Iterator<Circle> iterator = Server.playersInGame.iterator(); iterator.hasNext();) {
							Circle player = iterator.next();
							if(player.getName().equals(opponentEaten.getName())) {
								iterator.remove();
								break;
							}
						}
					}
					synchronized(Server.clients) {
						for(Iterator<ClientConnection> iterator = Server.clients.iterator(); iterator.hasNext();) {
							ClientConnection client = iterator.next();
							if(client.name.equals(opponentEaten.getName())) {
								client.sendAlert(new Alert("GAME_OVER", null));
								client.gameOver();
								iterator.remove();
							} else {
								client.sendAlert(receivedAlert);
							}
						}
					}
				}
			} catch (ClassNotFoundException | IOException e) {
				Server.textArea.append("Terminating connection from " + name + "...\n");
				Circle playerDisconnected = null;
				synchronized(Server.playersInGame) {
					for(Iterator<Circle> iterator = Server.playersInGame.iterator(); iterator.hasNext();) {
						Circle player = iterator.next();
						if(player.getName().equals(name)) {
							playerDisconnected = player;
							iterator.remove();
							break;
						}
					}
				}
				synchronized(Server.clients) {
					for(Iterator<ClientConnection> iterator = Server.clients.iterator(); iterator.hasNext();) {
						ClientConnection client = iterator.next();
						if(client == this) {
							client.gameOver();
							iterator.remove();
						} else {
							if(playerDisconnected != null) client.sendAlert(new Alert("OPPONENT_EATEN", playerDisconnected));
						}
					}
				}
				try {
					outputStream.close();
					inputStream.close();
					socket.close();
				} catch (IOException e1) {
					Server.textArea.append("Error closing connection.");
					e1.printStackTrace();
				}
			}
		}
	}
	
	public void gameOver() {
		gameIsRunning = false;
	}
	
	public synchronized void sendAlert(Alert alertToSend) {
		try {
			outputStream.reset();
			outputStream.writeObject(alertToSend);
			outputStream.flush();
		} catch (IOException e) {
			Server.textArea.append("ERROR: Couldn't send object.\n");
			e.printStackTrace();
		}
	}
	
	public void sendToAllExceptSelf(Alert alertToSend) {
		for(ClientConnection client: Server.clients) {
			if(client!=this)
				client.sendAlert(alertToSend);
		}
	}
}
