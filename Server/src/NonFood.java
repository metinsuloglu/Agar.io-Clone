import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

public class NonFood extends Food {

	public NonFood(double x, double y, double size) {
		super(x, y, size, Color.RED);
		this.setValue(-1*size*GameConstants.VALUE_TO_SIZE_RATIO);
	}
	
	public void drawToScreen(Graphics2D g) {
		g.setColor(Color.RED);
		BasicStroke stroke = new BasicStroke(15);
		g.setStroke(stroke);
		Ellipse2D border = new Ellipse2D.Double(getX()-getSize()/2, getY()-getSize()/2, getSize(), getSize());
		g.draw(border);
		
		g.setColor(Color.BLACK);
		Ellipse2D shape = new Ellipse2D.Double(getX()-getSize()/2, getY()-getSize()/2, getSize(), getSize());
		g.fill(shape);
		
		setBounds(stroke.createStrokedShape(shape).getBounds());
	}

}
