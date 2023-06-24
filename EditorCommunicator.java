import java.io.*;
import java.net.Socket;
import java.awt.*;

/**
 * Handles communication to/from the server for the editor
 * 
 * @author Reed Levinson, Spring 2023
 */
public class EditorCommunicator extends Thread {
	private PrintWriter out;		// to server
	private BufferedReader in;		// from server
	protected Editor editor;		// handling communication for

	/**
	 * Establishes connection and in/out pair
	 */
	public EditorCommunicator(String serverIP, Editor editor) {
		this.editor = editor;
		System.out.println("connecting to " + serverIP + "...");
		try {
			Socket sock = new Socket(serverIP, 4242);
			out = new PrintWriter(sock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			System.out.println("...connected");
		}
		catch (IOException e) {
			System.err.println("couldn't connect");
			System.exit(-1);
		}
	}

	/**
	 * Sends message to the server
	 */
	public void send(String msg) {
		out.println(msg);
	}

	/**
	 * Keeps listening for and handling (your code) messages from the server
	 */
	public void run() {
		try {
			// Handle messages
			String line;
			while ((line = in.readLine()) != null) {
				System.out.println(line);
				parseCommand(line);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("server hung up");
		}
	}

	/**
	 * Takes in a command from server, parses it, and calls method
	 * for client editor to make appropriate changes to sketch
	 * @param command command from server to be parsed
	 */
	public void parseCommand (String command) {
		// breaks command across spaces
		String[] parts = command.split(" ");
		switch (parts[0]) {
			// if ADD, extracts shape info from command, checks which shape to generate,
			// generates that shape, and tells editor to add to sketch
			case "ADD" -> {
				Shape shape = null;
				String[] info = command.split("\\|")[1].split(" ");
				switch (parts[2]) {
					case "ellipse" -> {
						shape = Ellipse.generateShapeFromParts(info);
					}
					case "rectangle" -> {
						shape = Rectangle.generateShapeFromParts(info);
					}
					case "polyline" -> {
						shape = Polyline.generateShapeFromParts(info);
					}
					case "segment" -> {
						shape = Segment.generateShapeFromParts(info);
					}
				}

				editor.add(Integer.parseInt(parts[1]), shape);
			}

			// if MOVE, tells editor to move particular shape (from ID) (parts[1])
			// a certain dx (parts[2]) and dy (parts[3]
			case "MOVE" -> {
				editor.move(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
			}

			// if RECOLOR, tells editor to recolor particular shape (from ID) (parts[1]) to color ((parts[2])
			case "RECOLOR" -> {
				editor.recolor(Integer.parseInt(parts[1]), new Color(Integer.parseInt(parts[2])));
			}

			// if DELETE, tells editor to delete particular shape (from ID) (parts[1]) from sketch
			case "DELETE" -> {
				editor.delete(Integer.parseInt(parts[1]));
			}
		}
	}


	// Send editor requests to the server

	/**
	 * Sends an ADD command to server from client/editor
	 * @param shape shape to be added
	 */
	public void sendAdd(Shape shape) {
		String s = "";
		s += "ADD " + shape.toString();
		send(s);
	}

	/**
	 * Sends a MOVE command to server from client/editor
	 * @param id ID of shape to be moved
	 * @param p1 initial position (of mouse in shape)
	 * @param p2 final position (of mouse in shape) after move
	 */
	public void sendMove(Integer id, Point p1, Point p2) {
		String s = "";
		s += "MOVE " + id + " " + (p2.x - p1.x) + " " + (p2.y - p1.y);
		send(s);
	}

	/**
	 * Sends a DELETE command to server from client/editor
	 * @param id ID of shape to be deleted
	 */
	public void sendDelete(Integer id) {
		String s = "DELETE " + id;
		send(s);
	}

	/**
	 * Sends a RECOLOR command to server from client/editor
	 * @param id ID of shape to be recolored
	 * @param color new color of shape
	 */
	public void sendRecolor(Integer id, int color) {
		String s = "RECOLOR "+ id + " " + color;
		send(s);
	}
	
}
