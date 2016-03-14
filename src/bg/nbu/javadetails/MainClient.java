package bg.nbu.javadetails;

import bg.nbu.javadetails.client.Client;

/**
 * The main class used to start the client.
 */
public class MainClient {

	public static void main(String[] args) {
		Client client = new Client("192.168.2.96", 10);

		client.startClient();
	}

}
