import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

public class Circle extends Entity {
	
	private double velocity;
	private String name;

	public Circle(double size, double x, double y, Color color, double velocity, String name) {
		super(x, y, size, color);
		this.velocity = velocity;
		this.name = name;
	}

	public double getVelocity() {
		return velocity;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity < GameConstants.MIN_VELOCITY ? GameConstants.MIN_VELOCITY : velocity;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setSize(double size) {
		this.size = size < GameConstants.MIN_SIZE ? GameConstants.MIN_SIZE : size;
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
		
		setBounds(shape.getBounds());
		
		g.setColor(Color.BLACK);
		g.setFont(new Font("Arial", Font.PLAIN, 14));
		int width = g.getFontMetrics().stringWidth(getName());
		g.drawString(getName(), (int)(getX()-width/2), (int)(getY()+5));
	}
	
	public boolean intersects(Entity e) {
		return Math.sqrt(Math.pow(Math.abs(e.getX()-getX()),2) + Math.pow(Math.abs(e.getY()-getY()),2)) <= getBounds().getWidth()/2 + e.getBounds().getWidth()/2;
	}
	
}
