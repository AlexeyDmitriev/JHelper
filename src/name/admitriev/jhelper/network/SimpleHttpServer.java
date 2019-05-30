package name.admitriev.jhelper.network;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.util.Consumer;
import name.admitriev.jhelper.ui.Notificator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Simple HTTP Server.
 * Passes every request without headers to given Consumer
 */
public class SimpleHttpServer implements Runnable {
	private static final String RESPONSE_CONTENT = "OK";
	private static final String RESPONSE_HEADERS = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/plain\r\n" + "Content-Length: ";
	private static final String RESPONSE_END_OF_HEADERS = "\r\n\r\n";

	private Consumer<String> consumer;
	private ServerSocket serverSocket = null;

	public SimpleHttpServer(int port, Consumer<String> consumer) throws IOException {
		serverSocket = new ServerSocket(port);
		this.consumer = consumer;
	}

	@Override
	public void run() {
		while (true) {
			try {
				if (serverSocket.isClosed()) {
					return;
				}
				try (Socket socket = serverSocket.accept();
					 PrintWriter serverOut = new PrintWriter(
							 new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true)) {

					InputStream inputStream = socket.getInputStream();
					// Make a response
					serverOut.println(RESPONSE_HEADERS + RESPONSE_CONTENT.length() + RESPONSE_END_OF_HEADERS + RESPONSE_CONTENT);

					String request = readFromStream(inputStream);
					String[] strings = request.split("\n\n", 2);

					//ignore headers
					if (strings.length < 2) {
						Notificator.showNotification(
								"ChromeParser",
								"Got response without body. Ignore.",
								NotificationType.INFORMATION
						);
						continue;
					}
					String text = strings[1];

					ApplicationManager.getApplication().invokeLater(
							() -> consumer.consume(text),
							ModalityState.defaultModalityState()
					);
				}
			}
			catch (IOException ignored) {
			}
		}
	}

	private static String readFromStream(InputStream inputStream) throws IOException {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(inputStream, "UTF-8")
		)) {
			StringBuilder builder = new StringBuilder();
			String line;
			//noinspection NestedAssignment
			while ((line = reader.readLine()) != null)
				builder.append(line).append('\n');
			return builder.toString();
		}
	}

	public void stop() {
		try {
			serverSocket.close();
		}
		catch (IOException ignored) {

		}
	}
}
