package name.admitriev.jhelper.actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import name.admitriev.jhelper.exceptions.NotificationException;
import name.admitriev.jhelper.ui.Notificator;
import org.jetbrains.annotations.NotNull;

public abstract class BaseAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		try {
			performAction(e);
		} catch (NotificationException exception) {
			Notificator.showNotification(
				exception.getTitle(),
				exception.getContent(),
				NotificationType.ERROR
			);
		} catch (RuntimeException exception) {
			Notificator.showNotification(
				"Please report this exception to our bug tracker",
				"You can see it again in the event log",
				NotificationType.ERROR
			);
			throw exception;
		}
	}

	protected abstract void performAction(AnActionEvent e);
}
