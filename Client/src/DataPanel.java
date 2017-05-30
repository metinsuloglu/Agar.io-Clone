import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class DataPanel extends JPanel {
	
	private JLabel speedLabel;
	private JLabel circleWeightLabel;
	private JLabel timeLabel;
	
	public DataPanel() {
		
		circleWeightLabel = new JLabel();
		circleWeightLabel.setFont(new Font("Arial", Font.BOLD, 20));
		circleWeightLabel.setHorizontalAlignment(SwingConstants.CENTER);
		circleWeightLabel.setBackground(new Color(153,255,153));
		circleWeightLabel.setOpaque(true);
		
		speedLabel = new JLabel();
		speedLabel.setFont(new Font("Arial", Font.BOLD, 20));
		speedLabel.setHorizontalAlignment(SwingConstants.CENTER);
		speedLabel.setBackground(new Color(153,153,255));
		speedLabel.setOpaque(true);
		
		timeLabel = new JLabel();
		timeLabel.setFont(new Font("Arial", Font.BOLD, 20));
		timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		timeLabel.setBackground(new Color(255,153,153));
		timeLabel.setOpaque(true);
		
		setLayout(new GridLayout(0,3));

		add(circleWeightLabel);
		add(speedLabel);
		add(timeLabel);
	}
	
	public void updateLabels(double circleWeight, double speed, double tStart) {
		circleWeightLabel.setText("Circle Weight: " + BigDecimal.valueOf(circleWeight).setScale(1, RoundingMode.HALF_UP).doubleValue());
		speedLabel.setText("Speed: " + BigDecimal.valueOf(speed).setScale(1, RoundingMode.HALF_UP).doubleValue());
		timeLabel.setText("Time: " + (int)(System.currentTimeMillis() - tStart)/1000 + " s");
	}
}
