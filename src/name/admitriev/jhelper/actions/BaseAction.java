package name.admitriev.jhelper.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import name.admitriev.jhelper.exceptions.JHelperException;
import name.admitriev.jhelper.exceptions.NotificationException;

public abstract class BaseAction extends AnAction {
	@Override
	public void actionPerformed(AnActionEvent e) {
		try {
			performAction(e);
		}
		catch (NotificationException exception) {
			Notification notification = new Notification(
					"JHelper",
					exception.getTitle(),
					exception.getContent(),
					NotificationType.ERROR
			);
			Notifications.Bus.notify(notification);
		}
		catch (JHelperException exception) {
			Notification notification = new Notification(
					"JHelper",
					"Please report this exception to our bug tracker",
					"You may see again that exception through event log",
					NotificationType.ERROR
			);
			Notifications.Bus.notify(notification);
			throw exception;
		}
	}

	protected abstract void performAction(AnActionEvent e);
}
