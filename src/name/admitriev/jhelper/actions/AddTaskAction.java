package name.admitriev.jhelper.actions;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import name.admitriev.jhelper.Util;
import name.admitriev.jhelper.configuration.TaskConfiguration;
import name.admitriev.jhelper.configuration.TaskConfigurationType;
import name.admitriev.jhelper.exception.NotificationException;
import name.admitriev.jhelper.task.Task;
import name.admitriev.jhelper.ui.AddTaskDialog;
import net.egork.chelper.util.OutputWriter;

public class AddTaskAction extends AnAction {
	@Override
	public void actionPerformed(AnActionEvent e) {
		Project project = e.getProject();
		if(project == null) {
			throw new NotificationException("Project is not found", "Are you in any project?");
		}

		AddTaskDialog dialog = new AddTaskDialog(project);
		dialog.show();
		if(!dialog.isOK()) {
			return;
		}
		final Task task = dialog.getTask();

		final VirtualFile newTaskFile = Util.findOrCreateByRelativePath(project.getBaseDir(), task.getPath());
		ApplicationManager.getApplication().runWriteAction(new Runnable() {
			@Override
			public void run() {
				OutputWriter writer = Util.getOutputWriter(newTaskFile, this);
				task.saveTask(writer);
				writer.flush();
				writer.close();
			}
		});

		createConfigurationForTask(project, task);

		generateCPP(project, task, newTaskFile);
	}

	private static void generateCPP(Project project, Task task, VirtualFile newTaskFile) {
		VirtualFile parent = newTaskFile.getParent();
		final PsiDirectory psiParent = PsiManager.getInstance(project).findDirectory(parent);
		if(psiParent == null) {
			throw new NotificationException("Can't open parent directory as PSI");
		}

		Language objC = Language.findLanguageByID("ObjectiveC");
		if(objC == null) {
			throw new NotificationException("Language not found");
		}

		final PsiFile file = PsiFileFactory.getInstance(project).createFileFromText(task.getClassName() + ".cpp", objC, generateFileContent(task.getClassName()));
		if(file == null) {
			throw new NotificationException("Can't generate file");
		}
		ApplicationManager.getApplication().runWriteAction(new Runnable() {
			@Override
			public void run() {
				psiParent.add(file);
			}
		});
	}

	private static CharSequence generateFileContent(String className) {
		return "class " + className + " {\n" +
		       "public:\n" +
		       "\tvoid solve() {\n" +
		       "\t\t\n" +
		       "\t}\n" +
		       "};\n" +
		       "int main() {\n" +
		       '\t' + className + " solver;\n" +
		       "\tsolver.solve();\n" +
		       "\treturn 0;\n" +
		       "}\n";
	}

	private static void createConfigurationForTask(Project project, Task task) {
		TaskConfigurationType configurationType = new TaskConfigurationType();
		ConfigurationFactory factory = configurationType.getConfigurationFactories()[0];

		RunManager manager = RunManager.getInstance(project);
		RunnerAndConfigurationSettings configuration = manager.createConfiguration(new TaskConfiguration(project, factory, task), factory);
		manager.addConfiguration(configuration, true);

		manager.setSelectedConfiguration(configuration);
	}
}
