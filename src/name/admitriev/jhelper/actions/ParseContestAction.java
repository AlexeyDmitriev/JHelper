package name.admitriev.jhelper.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.cidr.lang.psi.OCFile;
import name.admitriev.jhelper.IDEUtils;
import name.admitriev.jhelper.task.TaskData;
import name.admitriev.jhelper.task.TaskUtils;
import name.admitriev.jhelper.ui.ParseDialog;
import name.admitriev.jhelper.ui.UIUtils;

public class ParseContestAction extends BaseAction {
	@Override
	protected void performAction(AnActionEvent e) {
		Project project = e.getProject();
		ParseDialog dialog = new ParseDialog(project);
		dialog.show();
		if (!dialog.isOK()) {
			return;
		}
		for (TaskData taskData : dialog.getResult()) {
			PsiElement generatedFile = TaskUtils.saveNewTask(taskData, project);
			UIUtils.openMethodInEditor(project, (OCFile) generatedFile, "solve");
		}

		IDEUtils.reloadProject(project);
	}
}
