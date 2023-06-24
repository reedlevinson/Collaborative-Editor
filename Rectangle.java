import java.awt.Color;
import java.awt.Graphics;

/**
 * A rectangle-shaped Shape
 * Defined by an upper-left corner (x1,y1) and a lower-right corner (x2,y2)
 * with x1<=x2 and y1<=y2
 * 
 * @author Reed Levinson, Spring 2023
 */
public class Rectangle implements Shape {
	private int x1, x2, y1, y2;		// top left and bottom right corners
	private Color color;

	/**
	 * An "empty" rectangle, with only one point set so far
	 */
	public Rectangle(int x1, int y1, Color color) {
		this.x1 = x1; this.x2 = x1;
		this.y1 = y1; this.y2 = y1;
		this.color = color;
	}

	/**
	 * A rectangle defined by two corners
	 */
	public Rectangle(int x1, int y1, int x2, int y2, Color color) {
		setCorners(x1, y1, x2, y2);
		this.color = color;
	}

	/**
	 * Redefines the rectangle based on new corners
	 */
	public void setCorners(int x1, int y1, int x2, int y2) {
		// Ensure correct upper left and lower right
		this.x1 = Math.min(x1, x2);
		this.y1 = Math.min(y1, y2);
		this.x2 = Math.max(x1, x2);
		this.y2 = Math.max(y1, y2);
	}

	@Override
	public void moveBy(int dx, int dy) {
		x1 += dx; y1 += dy;
		x2 += dx; y2 += dy;
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}
		
	@Override
	public boolean contains(int x, int y) {
		return x >= x1 && x <= x2 && y >= y1 && y <= y2;
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		g.fillRect(x1, y1, x2-x1, y2-y1);
	}

	public String toString() { return "rectangle |"+x1+" "+y1+" "+x2+" "+y2+" "+color.getRGB()+"|"; }

	/**
	 * Creates a new rectangle from info embedded in server/client commands
	 * @param parts list of strings with information describing desired rectangle
	 * @return generated segment from rectangle
	 */
	public static Rectangle generateShapeFromParts (String[] parts) {
		// info in the form of x1, y1, x2, y2, color as int
		return new Rectangle(
				Integer.parseInt(parts[0]),
				Integer.parseInt(parts[1]),
				Integer.parseInt(parts[2]),
				Integer.parseInt(parts[3]),
				new Color(Integer.parseInt(parts[4]))
		);
	}
}
