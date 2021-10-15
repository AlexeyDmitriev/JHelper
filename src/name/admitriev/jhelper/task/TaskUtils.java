package name.admitriev.jhelper.task;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import name.admitriev.jhelper.configuration.TaskConfiguration;
import name.admitriev.jhelper.configuration.TaskConfigurationType;
import name.admitriev.jhelper.exceptions.NotificationException;
import name.admitriev.jhelper.generation.FileUtils;
import name.admitriev.jhelper.generation.TemplatesUtils;

public class TaskUtils {

	private TaskUtils() {
	}

	public static PsiElement saveNewTask(TaskData taskData, Project project) {
		createConfigurationForTask(project, taskData);
		return generateCPP(project, taskData);
	}

	private static PsiElement generateCPP(Project project, TaskData taskData) {
		VirtualFile parent = FileUtils.findOrCreateByRelativePath(project.getBaseDir(), FileUtils.getDirectory(taskData.getCppPath()));
		PsiDirectory psiParent = PsiManager.getInstance(project).findDirectory(parent);
		if (psiParent == null) {
			throw new NotificationException("Couldn't open parent directory as PSI");
		}

		Language objC = Language.findLanguageByID("ObjectiveC");
		if (objC == null) {
			throw new NotificationException("Language not found");
		}

		PsiFile file = PsiFileFactory.getInstance(project).createFileFromText(
			FileUtils.getFilename(taskData.getCppPath()),
			objC,
			getTaskContent(project, taskData.getClassName())
		);
		if (file == null) {
			throw new NotificationException("Couldn't generate file");
		}
		return ApplicationManager.getApplication().runWriteAction(
			(Computable<PsiElement>) () -> psiParent.add(file)
		);

	}

	/**
	 * Generates task file content depending on custom user template
	 */
	private static String getTaskContent(Project project, String className) {
		String template = TemplatesUtils.getTemplate(project, "task");
		template = TemplatesUtils.replaceAll(template, TemplatesUtils.CLASS_NAME, className);
		return template;
	}

	private static void createConfigurationForTask(Project project, TaskData taskData) {
		TaskConfigurationType configurationType = new TaskConfigurationType();
		ConfigurationFactory factory = configurationType.getConfigurationFactories()[0];

		RunManager manager = RunManager.getInstance(project);
		TaskConfiguration taskConfiguration = new TaskConfiguration(
			project,
			factory
		);
		taskConfiguration.setFromTaskData(taskData);
		RunnerAndConfigurationSettings configuration = manager.createConfiguration(
			taskConfiguration,
			factory
		);
		configuration.storeInDotIdeaFolder();

		manager.setSelectedConfiguration(configuration);
	}
}
