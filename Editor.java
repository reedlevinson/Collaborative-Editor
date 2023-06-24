import java.awt.*;
import java.util.List;
import java.awt.event.*;
import javax.swing.*;

/**
 * Client-server graphical editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 *
 * @author Reed Levinson, Spring 2023
 */

public class Editor extends JFrame {	
	private static String serverIP = "localhost";			// IP address of sketch server
	// "localhost" for your own machine;
	// or ask a friend for their IP address

	private static final int width = 800, height = 800;		// canvas size

	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}
	private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";		// type of object to add
	private Color color = Color.black;			// current drawing color

	// Drawing state
	// these are remnants of my implementation; take them as possible suggestions or ignore them
	private Shape curr = null;					// current shape (if any) being drawn
	private Sketch sketch;						// holds and handles all the completed objects
	private int movingId = -1;					// current shape id (if any; else -1) being moved
	private Point drawFrom = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged


	// Communication
	private EditorCommunicator comm;			// communication with the sketch server

	public Editor() {
		super("Graphical Editor");

		sketch = new Sketch();

		// Connect to server
		comm = new EditorCommunicator(serverIP, this);
		comm.start();

		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Put the buttons and canvas together into the window
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Creates a component to draw into
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSketch(g);
			}
		};
		
		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}

			public void mouseReleased(MouseEvent event) {
				handleRelease();
			}
		});		

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				handleDrag(event.getPoint());
			}
		});
		
		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		// Put all the stuff into a panel
		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}

	/**
	 * Getter for the sketch instance variable
	 */
	public Sketch getSketch() {
		return sketch;
	}

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {
		List<Shape> shapeList = sketch.getShapesInOrder();
		for (Shape shape : shapeList) {
			if (shape != null) {
				shape.draw(g);
			}
		}

		// need to draw shape being currently modified (if there is one)
		if (curr != null) {
			curr.draw(g);
		}
	}

	// Helpers for event handlers
	
	/**
	 * Helper method for press at point
	 * In drawing mode, start a new object;
	 * in moving mode, (request to) start dragging if clicked in a shape;
	 * in recoloring mode, (request to) change clicked shape's color
	 * in deleting mode, (request to) delete clicked shape
	 */
	private void handlePress(Point p) {
		switch (mode) {
			case DRAW -> {
				drawFrom = p;
				switch (shapeType) {
					case "ellipse" -> {
						curr = new Ellipse(p.x, p.y, color);
					}
					case "freehand" -> {
						curr = new Polyline(p, color);
					}
					case "rectangle" -> {
						curr = new Rectangle(p.x, p.y, color);
					}
					case "segment" -> {
						curr = new Segment(p.x, p.y, color);
					}
				}
			}

			case MOVE -> {
				// sets movingID to selected shape ID (if there is one)
				movingId = getShapeID(p);
				if (movingId == -1) break;
				moveFrom = p;
				curr = sketch.shapeFromID(movingId);
			}

			// if clicked in a shape (id != -1), sends command to server to delete shape
			case DELETE -> {
				int id = getShapeID(p);
				if (id == -1) break;
				comm.sendDelete(id);
				curr = null;
			}

			// if clicked in a shape (id != -1), sends command to server to recolor shape to editor current color
			case RECOLOR -> {
				int id = getShapeID(p);
				if (id == -1) break;
				System.out.println();
				comm.sendRecolor(id, color.getRGB());
			}
		}
		repaint();
	}

	/**
	 * Helper method for drag to new point
	 * In drawing mode, update the other corner of the object;
	 * in moving mode, (request to) drag the object
	 */
	private void handleDrag(Point p) {
		switch (mode) {
			case DRAW -> {
				switch (shapeType) {
					case "ellipse" -> {
						Ellipse draw = (Ellipse) curr;
						draw.setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
					}
					case "freehand" -> {
						Polyline draw = (Polyline) curr;
						draw.addPoint(p);
					}
					case "rectangle" -> {
						Rectangle draw = (Rectangle) curr;
						draw.setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
					}
					case "segment" -> {
						Segment draw = (Segment) curr;
						draw.setEnd(p.x, p.y);
					}
				}
			}

			// if there is a selected object to move, sends command to server to make appropriate translation
			case MOVE -> {
				if (movingId == -1) break;
				comm.sendMove(movingId, moveFrom, p);
				moveFrom = p;
			}
		}

		repaint();
	}

	/**
	 * Helper method for release
	 * In drawing mode, pass the add new object request on to the server;
	 * in moving mode, release it		
	 */
	private void handleRelease() {
		switch (mode) {
			// sends command to server to add modified/new shape and sets current shape to nothing
			case DRAW -> {
				comm.sendAdd(curr);
				curr = null;
			}

			// resets movingID to -1 (no shape being moved)
			case MOVE -> movingId = -1;
		}

		repaint();
	}

	/**
	 * Gets the ID of the shape from location of mouse
	 * Considers the depth of shapes (selects one closest to front/newest created)
	 * @param p point at location of mouse
	 * @return ID of targeted shape
	 */
	private int getShapeID (Point p) {
		// base case: returns -1, means no shape found at location
		int shapeID = -1;
		for (Integer id: sketch.getIDsInOrder()) {
			Shape s = sketch.shapeFromID(id);
			if (s.contains(p.x, p.y)) { shapeID = id; }
		}
		return shapeID;
	}

	/**
	 * Adds shape to local editor's sketch per command from server
	 * @param id ID of shape to add
	 * @param shape shape to add with descriptions
	 */
	public void add (int id, Shape shape) {
		sketch.addShapeFromClient(id, shape);
		System.out.println("adding " + shape.toString());
		repaint();
	}

	/**
	 * Moves shape in local editor's sketch per command from server
	 * @param id ID of shape to move
	 * @param dx x distance to translate shape
	 * @param dy y distance to translate shape
	 */
	public void move (int id, int dx, int dy) {
		Shape s = sketch.shapeFromID(id);
		s.moveBy(dx, dy);
		repaint();
	}

	/**
	 * Recolors shape in local editor's sketch per command from server
	 * @param id ID of shape to recolor
	 * @param c color to recolor to
	 */
	public void recolor (int id, Color c) {
		sketch.shapeFromID(id).setColor(c);
		System.out.println("recoloring shape ID to " + color);
		repaint();
	}

	/**
	 * Deletes shape in local editor's sketch per command from server
	 * @param id ID of shape to delete
	 */
	public void delete (int id) {
		sketch.removeShape(id);
		System.out.println("deleting shape " + id);
		repaint();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Editor();
			}
		});	
	}
}
