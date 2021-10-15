package name.admitriev.jhelper.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import name.admitriev.jhelper.components.Configurator;
import name.admitriev.jhelper.exceptions.NotificationException;
import name.admitriev.jhelper.ui.ConfigurationDialog;

public class ConfigureAction extends BaseAction {
	@Override
	public void performAction(AnActionEvent e) {
		Project project = e.getProject();
		if (project == null) {
			throw new NotificationException("No project found", "Are you in any project?");
		}

		Configurator configurator = project.getComponent(Configurator.class);
		ConfigurationDialog x = new ConfigurationDialog(project, configurator.getState());
		x.show();
		if (x.isOK()) {
			configurator.loadState(x.getConfiguration());
		}
	}
}
