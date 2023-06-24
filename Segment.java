import java.awt.Color;
import java.awt.Graphics;

/**
 * A line segment-shaped Shape
 *
 * @author Reed Levinson, Spring 2023
 */
public class Segment implements Shape {
	private int x1, y1, x2, y2;		// two endpoints
	private Color color;

	/**
	 * Initial 0-length segment at a point
	 */
	public Segment(int x1, int y1, Color color) {
		this.x1 = x1; this.x2 = x1;
		this.y1 = y1; this.y2 = y1;
		this.color = color;
	}

	/**
	 * Complete segment from one point to the other
	 */
	public Segment(int x1, int y1, int x2, int y2, Color color) {
		this.x1 = x1; this.y1 = y1;
		this.x2 = x2; this.y2 = y2;
		this.color = color;
	}

	/**
	 * Update the start (first point) of the segment
	 */
	public void setStart(int x1, int y1) {
		this.x1 = x1; this.y1 = y1;
	}
	
	/**
	 * Update the end (second point) of the segment
	 */
	public void setEnd(int x2, int y2) {
		this.x2 = x2; this.y2 = y2;
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
		return pointToSegmentDistance(x, y, x1, y1, x2, y2) <= 3;
	}

	/**
	 * Helper method to compute the distance between a point (x,y) and a segment (x1,y1)-(x2,y2)
	 * http://stackoverflow.com/questions/849211/shortest-distance-between-a-point-and-a-line-segment
	 */
	public static double pointToSegmentDistance(int x, int y, int x1, int y1, int x2, int y2) {
		double l2 = dist2(x1, y1, x2, y2);
		if (l2 == 0) return Math.sqrt(dist2(x, y, x1, y1)); // segment is a point
		// Consider the line extending the segment, parameterized as <x1,y1> + t*(<x2,y2> - <x1,y1>).
		// We find projection of point <x,y> onto the line. 
		// It falls where t = [(<x,y>-<x1,y1>) . (<x2,y2>-<x1,y1>)] / |<x2,y2>-<x1,y1>|^2
		double t = ((x-x1)*(x2-x1) + (y-y1)*(y2-y1)) / l2;
		// We clamp t from [0,1] to handle points outside the segment.
		t = Math.max(0, Math.min(1, t));
		return Math.sqrt(dist2(x, y, x1+t*(x2-x1), y1+t*(y2-y1)));
	}

	/**
	 * Euclidean distance squared between (x1,y1) and (x2,y2)
	 */
	public static double dist2(double x1, double y1, double x2, double y2) {
		return (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1);
	}
	
	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		g.drawLine(x1, y1, x2, y2);
	}

	@Override
	public String toString() {
		return "segment |"+x1+" "+y1+" "+x2+" "+y2+" "+color.getRGB()+"|";
	}

	/**
	 * Creates a new segment from info embedded in server/client commands
	 * @param parts list of strings with information describing desired segment
	 * @return generated segment from information
	 */
	public static Segment generateShapeFromParts (String[] parts) {
		// info in the form of x1, y1, x2, y2, color as int
		return new Segment(
				Integer.parseInt(parts[0]),
				Integer.parseInt(parts[1]),
				Integer.parseInt(parts[2]),
				Integer.parseInt(parts[3]),
				new Color(Integer.parseInt(parts[4]))
		);
	}
}
