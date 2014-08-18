package name.admitriev.jhelper.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import name.admitriev.jhelper.exceptions.NotificationException;
import name.admitriev.jhelper.generation.SubmitCodeGenerationUtils;


public class GenerateCodeAction extends BaseAction {
	@Override
	public void performAction(AnActionEvent e) {
		Project project = e.getProject();
		PsiFile file = e.getData(CommonDataKeys.PSI_FILE);

		if (file == null) {
			throw new NotificationException("File not found", "Are you editing any file?");
		}

		SubmitCodeGenerationUtils.generateSubmissionFile(project, file);
	}
}
