import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

public class Food extends Entity {

	private double value;
	
	public Food(double x, double y, double size, Color color) {
		super(x, y, size, color);
		this.setValue(size * GameConstants.VALUE_TO_SIZE_RATIO);
	}

	public void drawToScreen(Graphics2D g) {
		g.setColor(GameConstants.BORDER_COLOR);
		BasicStroke stroke = new BasicStroke(5);
		g.setStroke(stroke);
		Ellipse2D border = new Ellipse2D.Double(getX()-getSize()/2, getY()-getSize()/2, getSize(), getSize());
		g.draw(border);
		
		g.setColor(getColor());
		Ellipse2D shape = new Ellipse2D.Double(getX()-getSize()/2, getY()-getSize()/2, getSize(), getSize());
		g.fill(shape);
		setBounds(stroke.createStrokedShape(shape).getBounds());
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
	
}
