package name.admitriev.jhelper.network;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.util.Consumer;
import name.admitriev.jhelper.common.CommonUtils;
import name.admitriev.jhelper.ui.Notificator;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Simple HTTP Server.
 * Passes every request without headers to given Consumer
 */
public class SimpleHttpServer implements Runnable {
	private final Consumer<String> consumer;
	private final ServerSocket serverSocket;

	public SimpleHttpServer(SocketAddress endpoint, Consumer<String> consumer) throws IOException {
		serverSocket = new ServerSocket();
		serverSocket.bind(endpoint);
		this.consumer = consumer;
	}

	@Override
	public void run() {
		while (true) {
			try {
				if (serverSocket.isClosed()) {
					return;
				}
				try (Socket socket = serverSocket.accept()) {
					InputStream inputStream = socket.getInputStream();
					String request = CommonUtils.getStringFromInputStream(inputStream);
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
			} catch (IOException ignored) {
			}
		}
	}

	public void stop() {
		try {
			serverSocket.close();
		} catch (IOException ignored) {

		}
	}
}
