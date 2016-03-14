package bg.nbu.javadetails.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

/**
 * Client used to connect to an echo server.
 */
public class Client {

	private static final String STOP_WORD = "quit";

	private final String _hostAddress;
	private final int _hostPort;

	/**
	 * @param ipAddress
	 *            the ip address of the host
	 * @param port
	 *            the port of the host
	 */
	public Client(String ipAddress, int port) {
		if (ipAddress == null || !ipAddress.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
			throw new IllegalArgumentException("Invalid IP address");
		}
		if (port < 0 || port > 65535) {
			throw new IllegalArgumentException("The port must be a number between 0 and 65 535");
		}

		_hostAddress = ipAddress;
		_hostPort = port;
	}

	/**
	 * Starts a client that connects to server.
	 *
	 * Sends messages received from the standard input. Adds a EOTB char for indicating end of transmission.
	 * 
	 * Prints messages received from the server.
	 */
	public void startClient() {
		System.out.println("Connecting to server: " + _hostAddress + ":" + _hostPort);

		try (Scanner scanner = new Scanner(System.in);
		    Socket client = new Socket(_hostAddress, _hostPort);) {

			while (scanner.hasNext()) {
				String input = scanner.nextLine();
				System.out.println("Sending:" + input);

				// adding end of transmission block (EOTB) char to indicate well.. end of transmission
				input = input + ((char) 23);
				client.getOutputStream().write(input.getBytes());

				if (input.trim().equals(STOP_WORD)) { // the trim removes the EOTB char
					break; // stopping the client
				}

				BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
				StringBuilder response = new StringBuilder();

				int readByte = br.read();
				while (readByte != 23 && readByte != -1) {
					response.append((char) readByte);
					readByte = br.read();
				}

				System.out.println("response:" + response);
			}

		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
}
