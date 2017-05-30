import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class FoodSpawner implements ActionListener {
	
	private Random rand;
	
	public FoodSpawner() {
		rand = new Random();
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
	
	public void spawnFood() {
		if(Server.foods.size() < GameConstants.MAX_NUM_FOODS) {
			if (rand.nextDouble()*2 < 1.9)
				Server.addFood(new Food(randomPoint().getX(), randomPoint().getY(), rand.nextDouble()*10+8, randomColor()));
			else
				Server.addFood(new NonFood(randomPoint().getX(), randomPoint().getY(), rand.nextDouble()*10+20));
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		spawnFood();
	}
	
}
