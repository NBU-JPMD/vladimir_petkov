package bg.nbu.javadetails;

import java.io.IOException;

import bg.nbu.javadetails.server.Server;

/**
 * The main class used to start the server.
 */
public class MainServer {

	public static void main(String[] args) throws IOException {
		Server server = new Server(7);

		server.startServer();
	}
}
