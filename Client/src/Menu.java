import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class Menu extends JPanel implements ActionListener {

	private JLabel welcomeLabel;
	private JLabel creatorsLabel;
	
	private JLabel nameLabel;
	private JTextField nameField;
	
	private JLabel serverLabel;
	private JTextField serverField;
	
	private JButton startButton;
	
	private GridBagConstraints gbc;
	
	private Game gameFrame;
	
	private Image img;
	private InputStream inputStream;
	
	public Menu(Game gameFrame) {
		setLayout(new GridBagLayout());
		setBackground(Color.BLACK);
		this.gameFrame = gameFrame;
		gbc = new GridBagConstraints();

		try {
			inputStream = getClass().getResourceAsStream("/menuBackground.png");
			if(inputStream!=null) img = ImageIO.read(inputStream);
			else System.out.println("Couldn't find background image.");
		} catch (IOException e) {
			System.out.println("Couldn't load background image.");
			e.printStackTrace();
		}
		
		welcomeLabel = new JLabel("Welcome to Ballario!");
		welcomeLabel.setFont(new Font("Impact", Font.BOLD, 35));
		welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		welcomeLabel.setForeground(Color.WHITE);
		creatorsLabel = new JLabel("Metin Suloglu & Liana Nassanova");
		creatorsLabel.setFont(new Font("Arial", Font.ITALIC, 20));
		creatorsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		creatorsLabel.setForeground(Color.WHITE);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(welcomeLabel, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 50, 0);
		add(creatorsLabel, gbc);
		
		nameLabel = new JLabel("Enter a name");
		nameLabel.setFont(new Font("Arial", Font.BOLD, 30));
		nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		nameLabel.setForeground(Color.WHITE);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(20, 0, 0, 0);
		add(nameLabel, gbc);
		
		nameField = new JTextField();
		nameField.setFont(new Font("Arial", Font.BOLD, 20));
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 10, 0);
		gbc.ipadx = 10;
		gbc.ipady = 10;
		add(nameField, gbc);
		
		serverLabel = new JLabel("Enter the server address");
		serverLabel.setFont(new Font("Arial", Font.BOLD, 30));
		serverLabel.setHorizontalAlignment(SwingConstants.CENTER);
		serverLabel.setForeground(Color.WHITE);
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(20, 0, 0, 0);
		add(serverLabel, gbc);
		
		serverField = new JTextField();
		serverField.setFont(new Font("Arial", Font.BOLD, 20));
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 10, 0);
		gbc.ipadx = 10;
		gbc.ipady = 10;
		add(serverField, gbc);
		
		startButton = new JButton("Start Game");
		startButton.setFont(new Font("Impact", Font.BOLD, 25));
		startButton.addActionListener(this);
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(20, 20, 20, 20);
		gbc.ipadx = 30;
		gbc.ipady = 30;
		add(startButton, gbc);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(nameField.getText().isEmpty()) {
			nameField.setBackground(Color.PINK);
		} else nameField.setBackground(Color.WHITE);
		if(serverField.getText().isEmpty()) {
			serverField.setBackground(Color.PINK);
		} else serverField.setBackground(Color.WHITE);
		if(!nameField.getText().isEmpty() && !serverField.getText().isEmpty()) {
			gameFrame.startGame(nameField.getText(), serverField.getText());
		}
	}
	
}
