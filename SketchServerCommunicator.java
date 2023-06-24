import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Reed Levinson, Spring 2023
 */
public class SketchServerCommunicator extends Thread {
	private Socket sock;					// to talk with client
	private BufferedReader in;				// from client
	private PrintWriter out;				// to client
	private SketchServer server;			// handling communication for

	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
	}

	/**
	 * Sends a message to the client
	 * @param msg
	 */
	public void send(String msg) {
		out.println(msg);
	}
	
	/**
	 * Keeps listening for and handling (your code) messages from the client
	 */
	public void run() {
		try {
			System.out.println("someone connected");
			
			// Communication channel
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			// Tell the client the current state of the world
			List<Integer> ids = server.getSketch().getIDsInOrder(); // gets  the IDs of all shapes in the sketch
			StringBuilder sb = new StringBuilder();

			// converts the given IDs into commands to generate shapes in client sketches using StringBuilder
			ids.stream().forEach((id) -> {
				sb.append("ADD ");
				sb.append(id);
				sb.append(" ");
				sb.append(server.getSketch().shapeFromID(id).toString());
				sb.append("\n");
			});

			String s = sb.toString();
			out.println(s);

			// Keep getting and handling messages from the client
			String line;
			while ((line = in.readLine()) != null) {
				System.out.println("command received: " +line);
				server.resolveCommand(line);
			}

			// Clean up -- note that also remove self from server's list so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
