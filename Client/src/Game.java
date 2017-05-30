import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;

public class Game extends JFrame {
	
	private Menu menuPanel;
	private GamePanel gamePanel;
	private DataPanel dataPanel;
	
	public void startGame(String name, String host) {
		
		remove(menuPanel);
		
		dataPanel = new DataPanel();
		add(dataPanel, BorderLayout.NORTH);
		gamePanel = new GamePanel(dataPanel, name, host);
		gamePanel.setBackground(Color.WHITE);
		add(gamePanel, BorderLayout.CENTER);
		gamePanel.setFocusable(true);
		gamePanel.requestFocus();
	}

	public void displayMenu() {
		setTitle("Ballario - By Liana Nassanova & Metin Suloglu");
		setSize(GameConstants.SCREEN_SIZE, GameConstants.SCREEN_SIZE);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		menuPanel = new Menu(this);
		add(menuPanel, BorderLayout.CENTER);
		
		setVisible(true);
	}
	
	public static void main(String[] args) {
		new Game().displayMenu();
	}
	

}
