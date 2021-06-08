package name.admitriev.jhelper.ui;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;

public class Notificator {
	private static final NotificationGroup GROUP = NotificationGroupManager.getInstance().getNotificationGroup("name.admitriev.jhelper.Notificator");

	private Notificator() {
	}

	public static void showNotification(String title, String content, NotificationType notificationType) {
		GROUP.createNotification(title, content, notificationType, null).notify(null);
	}

	public static void showNotification(String content, NotificationType notificationType) {
		showNotification("", content, notificationType);
	}
}
