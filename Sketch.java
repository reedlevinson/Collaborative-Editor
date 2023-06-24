import java.util.List;
import java.util.TreeMap;

/**
 * Sketch class used for managing all shapes for both client and server in collaborative painting tool
 * @author Reed Levinson, Spring 2023
 */
public class Sketch {
    int numID; // used for tracking which shape added the sketch is at
    private TreeMap<Integer, Shape> shapeMap; // Map used to associate shape IDs to shapes

    /**
     * Constructor to generate empty ID-shape map and set total shapes to 0
     */
    public Sketch () {
        shapeMap = new TreeMap<>();
        numID = 0;
    }

    /**
     * Returns a list of IDs of shapes in order of front (newest added) to back (oldest)
     * @return list of IDs
     */
    public List<Integer> getIDsInOrder() {
        return shapeMap.navigableKeySet()
                .stream()
                .toList();
    }

    /**
     * Returns a list of shapes in order of front (newest added) to back (oldest)
     * @return list of shapes
     */
    public List<Shape> getShapesInOrder() {
        // uses a stream to iterate through the whole key set and get each shape its associated with
        return shapeMap.navigableKeySet()
                .stream()
                .map((Integer i) -> shapeMap.get(i))
                .toList();
    }

    /**
     * Returns a particular shape from its ID number
     * @param n ID of shape
     * @return shape
     */
    public Shape shapeFromID (int n) { return shapeMap.get(n); }

    /**
     * (only for use by clients)
     * Adds a shape to the shape map with a pre-existing ID
     * @param id ID of shape to be added
     * @param shape shape to be added
     */
    public void addShapeFromClient (int id, Shape shape) {
        shapeMap.put(id, shape);
    }

    /**
     * (only for use by server)
     * Adds a new shape to a new ID, increasing the total number of shapes in sketch
     * and making a new entry in the map
     * @param shape shape to be added
     * @return ID of the newly added shape
     */
    public int addShapeFromServer (Shape shape) {
        numID++;
        int currID = numID;
        shapeMap.put(currID, shape);
        return currID;
    }

    /**
     * Removes a shape from the sketch from its ID
     * @param id ID of shape to be removed
     */
    public void removeShape (int id) {
        shapeMap.remove(id);
    }

    /**
     * Function used to generate a new shape off an ADD command from server
     * @param s ADD command to be parsed
     * @return shape derived from command
     */
    public static Shape parseCommand (String s) {
        // splits command into component parts by spaces (ADD shapeType |info|)
        // then splits info into desired info for shape generation
        String[] parts = s.split(" ");
        String[] info = s.split("\\|")[1].split(" ");
        switch (parts[1]) {
            case "ellipse":
                return Ellipse.generateShapeFromParts(info);
            case "rectangle":
                return Rectangle.generateShapeFromParts(info);
            case "polyline":
                return Polyline.generateShapeFromParts(info);
            case "segment":
                return Segment.generateShapeFromParts(info);
        }
        return null;
    }
}
