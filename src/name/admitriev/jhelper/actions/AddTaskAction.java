package name.admitriev.jhelper.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.cidr.lang.psi.OCFile;
import name.admitriev.jhelper.IDEUtils;
import name.admitriev.jhelper.exceptions.NotificationException;
import name.admitriev.jhelper.task.Task;
import name.admitriev.jhelper.task.TaskUtils;
import name.admitriev.jhelper.ui.AddTaskDialog;
import name.admitriev.jhelper.ui.UIUtils;

public class AddTaskAction extends BaseAction {

	@Override
	public void performAction(AnActionEvent e) {
		Project project = e.getProject();
		if (project == null) {
			throw new NotificationException("No project found", "Are you in any project?");
		}

		AddTaskDialog dialog = new AddTaskDialog(project);
		dialog.show();
		if (!dialog.isOK()) {
			return;
		}
		Task task = dialog.getTask();

		PsiElement generatedFile = TaskUtils.saveTask(task, project);

		UIUtils.openMethodInEditor(project, (OCFile) generatedFile, "solve");

		IDEUtils.reloadProjectInCLion(project);
	}

}
