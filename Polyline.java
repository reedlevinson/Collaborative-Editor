import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 * 
 * @author Reed Levinson, Spring 2023
 */
public class Polyline implements Shape {
	private Color color;
	private List<Point> points = new ArrayList<>();

	public Polyline (Point p, Color color) {
		points.add(p);
		this.color = color;
	}

	public void addPoint (Point p) {
		points.add(p);
	}

	@Override
	public void moveBy(int dx, int dy) {
		for (Point p: points) {
			p.translate(dx, dy);
		}
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
		for (int p = 0; p < points.size() - 1; p++) {
			Point p1 = points.get(p);
			Point p2 = points.get(p + 1);
			if (Segment.pointToSegmentDistance(x, y, p1.x, p1.y, p2.x, p2.y) <= 20) return true;
		}
		return false;
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		for (int p = 0; p < points.size() - 1; p++) {
			Point p1 = points.get(p);
			Point p2 = points.get(p + 1);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
	}

	@Override
	public String toString() {
		String pList = "";
		// iterates over all points and builds out stream of x and y values of points
		for (Point p: points) {
			pList += p.x+" "+p.y+" ";
		}
		return "polyline |"+pList+color.getRGB()+"|";
	}

	/**
	 * Creates a new polyline from info embedded in server/client commands
	 * @param parts list of strings with information describing desired polyline
	 * @return generated polyline from information
	 */
	public static Polyline generateShapeFromParts (String[] parts) {
		// info in the form of x1, y1, x2, y2,... xn, yn, color as int
		Polyline polyline = new Polyline(
				new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])),
				new Color(Integer.parseInt(parts[parts.length - 1]))); // references last in list for color info
		for(int i = 2; i < parts.length - 1; i += 2) {
			polyline.addPoint(new Point(Integer.parseInt(parts[i]),Integer.parseInt(parts[i+1])));
		}
		return polyline;
	}
}
