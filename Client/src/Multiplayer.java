import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;

public class Multiplayer {
	
	private Socket socket;
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	
	private static final int PORT = 24680;
	
	public Multiplayer(final String HOST) {
		try {
				socket = new Socket(HOST, PORT);
			
				System.out.println("Connected to the server.\nSetting up streams...");

				outputStream = new ObjectOutputStream(socket.getOutputStream());
				inputStream = new ObjectInputStream(socket.getInputStream());

				System.out.println("Streams set up.");
		} catch (IOException e) {
			System.out.println("Error connecting to server.");
			e.printStackTrace();
		}
	}
	
	public void writeAlert(Alert a) {
		try {
			outputStream.reset();
			outputStream.writeObject(a);
			outputStream.flush();
		} catch (IOException e) {
			System.out.println("Error sending alert.");
			e.printStackTrace();
		}
	}
	
	public Alert readAlert() {
		try {
			return (Alert) inputStream.readObject();
		} catch (ClassNotFoundException | IOException e) {
			System.out.println("Error reading from stream.");
			e.printStackTrace();
			return null;
		}
	}
	
	public void closeConnection() {
		System.out.println("Closing connection...");
		try {
			outputStream.close();
			inputStream.close();
			socket.close();
		} catch (IOException e) {
			System.out.println("Error closing connection.");
			e.printStackTrace();
		}
		System.out.println("Finished closing connection.");
	}
}
