package name.admitriev.jhelper.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import name.admitriev.jhelper.components.Configurator;
import name.admitriev.jhelper.ui.ConfigurationDialog;

public class ConfigureAction extends AnAction {
	@Override
	public void actionPerformed(AnActionEvent e) {
		Project project = e.getProject();
		if(project == null)
			return;

		Configurator configurator = project.getComponent(Configurator.class);
		Configurator.State configuration = configurator.getState();

		ConfigurationDialog x = new ConfigurationDialog(project, configuration);
		x.show();
		if(x.isOK()) {
			configurator.loadState(x.getConfiguration());
		}
	}
}
