import java.awt.*;
import java.net.*;
import java.util.*;
import java.io.*;

/**
 * A server to handle sketches: getting requests from the clients,
 * updating the overall state, and passing them on to the clients
 *
 * @author Reed Levinson, Spring 2023
 */
public class SketchServer {
	private ServerSocket listen;						// for accepting connections
	private ArrayList<SketchServerCommunicator> comms;	// all the connections with clients
	private Sketch sketch;								// the state of the world
	
	public SketchServer(ServerSocket listen) {
		this.listen = listen;
		sketch = new Sketch();
		comms = new ArrayList<SketchServerCommunicator>();
	}

	public Sketch getSketch() {
		return sketch;
	}
	
	/**
	 * The usual loop of accepting connections and firing off new threads to handle them
	 */
	public void getConnections() throws IOException {
		System.out.println("server ready for connections");
		while (true) {
			SketchServerCommunicator comm = new SketchServerCommunicator(listen.accept(), this);
			comm.setDaemon(true);
			comm.start();
			addCommunicator(comm);
		}
	}

	/**
	 * Adds the communicator to the list of current communicators
	 */
	public synchronized void addCommunicator(SketchServerCommunicator comm) {
		comms.add(comm);
	}

	/**
	 * Removes the communicator from the list of current communicators
	 */
	public synchronized void removeCommunicator(SketchServerCommunicator comm) {
		comms.remove(comm);
	}

	/**
	 * Sends the message from the one communicator to all (including the originator)
	 */
	public synchronized void broadcast(String msg) {
		for (SketchServerCommunicator comm : comms) {
			comm.send(msg);
		}
	}

	/**
	 * Method used to resolve a command fed to server from a client editor
	 * Returns relevant command to all client editors (including one in which change was made)
	 * @param command command to be parsed and re-fed to client editors
	 */
	public synchronized void resolveCommand (String command) {
		// splits command up on spaces
		String[] parts = command.split(" ");
		switch (parts[0]) {
			// if ADD, generates new shape using parseCommand method in Sketch, adds shape to overall server sketch,
			// receives its ID, generates appropriate command for client editors,
			// and broadcasts to all clients to execute
			case "ADD" -> {
				Shape shape = Sketch.parseCommand(command);
				int id = sketch.addShapeFromServer(shape);
				String s = "ADD " + id + " " + shape.toString();
				broadcast(s);
			}

			// if MOVE, determines which shape to be moved from command using embedded ID, gathers particular
			// dx and dy distances, applies to server sketch, and broadcasts to all client editors
			// the move command to execute
			case "MOVE" -> {
				int id = Integer.parseInt(parts[1]);
				Shape shape = sketch.shapeFromID(id);
				shape.moveBy(Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
				broadcast(command);
			}

			// if RECOLOR, gets shape ID from command, determines shape from shape ID in sketch, set color of shape
			// to new color in server sketch, and broadcasts recoloring command to all clients to execute
			case "RECOLOR" -> {
				int id = Integer.parseInt(parts[1]);
				Shape shape = sketch.shapeFromID(id);
				shape.setColor(new Color(Integer.parseInt(parts[2])));
				broadcast(command);
			}

			// if DELETE, gets shape ID from command, removes shape from server sketch using ID, and broadcasts
			// delete command to all client editors to execute
			case "DELETE" -> {
				int id = Integer.parseInt(parts[1]);
				sketch.removeShape(id);
				broadcast(command);
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		new SketchServer(new ServerSocket(4242)).getConnections();
	}
}
