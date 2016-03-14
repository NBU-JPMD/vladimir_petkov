package bg.nbu.javadetails.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import bg.nbu.javadetails.commons.Constants;

/**
 * Simple echo server.
 *
 * Returns the same message received as input. Adds an EOTB char for indicating
 * end of transmission.
 */
public class Server {

	private final int _port;

	/**
	 * @param port
	 *            the port on which the server will be open
	 */
	public Server(int port) {
		if (port < 0 || port > 65535) {
			throw new IllegalArgumentException("The port must be a number between 0 and 65 535");
		}

		_port = port;
	}

	/**
	 * Starts the echo server.
	 *
	 * @throws IOException
	 */
	public void startServer() throws IOException {

		ServerSocketChannel serverSocket = initServer();
		int operations = serverSocket.validOps();

		Selector selector = Selector.open();
		serverSocket.register(selector, operations);

		processConnections(serverSocket, selector);
	}

	private ServerSocketChannel initServer() throws IOException {
		System.out.println("Starting server at port: " + _port);

		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		InetSocketAddress hostAddress = new InetSocketAddress(_port);
		serverSocketChannel.socket().bind(hostAddress);

		serverSocketChannel.configureBlocking(false);

		return serverSocketChannel;
	}

	private void processConnections(ServerSocketChannel serverSocket, Selector selector)
			throws IOException, ClosedChannelException {

		System.out.println("Waiting for connections...");

		while (true) {
			selector.select();

			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while (it.hasNext()) {
				SelectionKey key = (SelectionKey) it.next();

				if (key.isAcceptable()) {
					acceptConnection(serverSocket, selector);

				}

				if (key.isReadable()) {
					doEcho(key);
				}

				it.remove();
			}
		}
	}

	private void acceptConnection(ServerSocketChannel serverSocket, Selector selector)
			throws IOException, ClosedChannelException {

		SocketChannel client = serverSocket.accept();
		client.configureBlocking(false);
		client.register(selector, SelectionKey.OP_READ);

		System.out.println("New connection from: " + client.getRemoteAddress());
	}

	private void doEcho(SelectionKey key) throws IOException {

		SocketChannel client = (SocketChannel) key.channel();
		String clientAddress = client.getRemoteAddress().toString();

		StringBuilder messageBuilder = new StringBuilder();

		try {
			readMessage(client, messageBuilder);

		} catch (IOException e) { // the client forcibly closed the connection
			client.close();
			System.out.println(clientAddress + " : " + e.getMessage());
			return;
		}

		String message = messageBuilder.toString();
		System.out.println("Message read from " + clientAddress + " client: " + message);

		// indicates end of connection
		if (message.equals(Constants.CONNNECTION_STOP_MESSAGE)) {
			client.close();
			System.out.println("Client " + clientAddress + " send closing message.");

		} else { // returning the same message
			try {
				writeMessage(client, message);
			} catch (IOException e) { // the client forcibly closed the connection

				client.close();
				System.out.println(clientAddress + " : " + e.getMessage());
				return;
			}
		}
	}

	private void readMessage(SocketChannel client, StringBuilder recievedStrBuilder) throws IOException {

		while (true) {
			ByteBuffer buffer = ByteBuffer.allocate(512);
			client.read(buffer);
			String output = new String(buffer.array());

			if (output.indexOf((char) 23) != -1) {
				output = output.substring(0, output.indexOf((char) 23));
				recievedStrBuilder.append(output);
				break;
			}

			recievedStrBuilder.append(output);
			buffer.flip();
			buffer.clear();
		}
	}

	private void writeMessage(SocketChannel client, String recievedStr) throws IOException {

		// adding end of transmission block char to indicate well.. end of transmission
		recievedStr += (char) 23;
		client.write(ByteBuffer.wrap(recievedStr.getBytes()));
	}
}
