package name.admitriev.jhelper.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import name.admitriev.jhelper.components.Configurator;
import name.admitriev.jhelper.ui.ConfigurationDialog;

public class ConfigureAction extends AnAction {
	@Override
	public void actionPerformed(AnActionEvent e) {
		Project project = getEventProject(e);
		assert project != null;
		Configurator configurator = project.getComponent(Configurator.class);
		Configurator.State newConfiguration = ConfigurationDialog.edit(project, configurator.getState());
		if(newConfiguration != null) {
			System.err.println(newConfiguration.getAuthor() + ' ' + newConfiguration.getTasksDirectory());
			configurator.loadState(newConfiguration);
		}
	}
}
