package name.admitriev.jhelper.actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import name.admitriev.jhelper.components.Configurator;
import name.admitriev.jhelper.exceptions.NotificationException;
import name.admitriev.jhelper.ui.ConfigurationDialog;
import name.admitriev.jhelper.ui.Notificator;

public class ConfigureAction extends BaseAction {
	@Override
	public void performAction(AnActionEvent e) {
		Project project = e.getProject();
		if (project == null) {
			throw new NotificationException("No project found", "Are you in any project?");
		}

		Notificator.showNotification("test", NotificationType.WARNING);
		Configurator configurator = project.getService(Configurator.class);
		Configurator.State configuration = configurator.getState();

		ConfigurationDialog x = new ConfigurationDialog(project, configuration);
		x.show();
		if (x.isOK()) {
			configurator.loadState(x.getConfiguration());
		}
	}
}
