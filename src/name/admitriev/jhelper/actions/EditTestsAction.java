package name.admitriev.jhelper.actions;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import name.admitriev.jhelper.configuration.TaskConfiguration;
import name.admitriev.jhelper.ui.EditTestsDialog;
import net.egork.chelper.task.Test;

public class EditTestsAction extends BaseAction {

	@Override
	protected void performAction(AnActionEvent e) {
		Project project = e.getProject();
		RunnerAndConfigurationSettings selectedConfiguration =
				RunManagerImpl.getInstanceImpl(project).getSelectedConfiguration();
		if (selectedConfiguration == null) {
			return;
		}
		RunConfiguration configuration = selectedConfiguration.getConfiguration();
		if (configuration instanceof TaskConfiguration) {
			TaskConfiguration taskConfiguration = (TaskConfiguration) configuration;
			Test[] originalTests = taskConfiguration.getTests();
			EditTestsDialog dialog = new EditTestsDialog(originalTests, project);
			dialog.show();
			if (!dialog.isOK()) {
				return;
			}
			Test[] newTests = dialog.getTests();
			taskConfiguration.setTests(newTests);

			// @todo: save configuration
		}
	}
}
