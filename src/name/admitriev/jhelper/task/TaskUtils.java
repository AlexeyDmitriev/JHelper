package name.admitriev.jhelper.task;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import name.admitriev.jhelper.configuration.TaskConfiguration;
import name.admitriev.jhelper.configuration.TaskConfigurationType;
import name.admitriev.jhelper.exceptions.NotificationException;
import name.admitriev.jhelper.generation.FileUtils;
import name.admitriev.jhelper.generation.TemplatesUtils;
import net.egork.chelper.util.OutputWriter;

public class TaskUtils {

	private TaskUtils() {
	}

	/**
	 * Generates task file content depending on custom user template
	 */
	private static String getTaskContent(Project project, String className) {
		String template = TemplatesUtils.getTemplate(project, "task");
		template = TemplatesUtils.replaceAll(template, TemplatesUtils.CLASS_NAME, className);
		return template;
	}

	/**
	 * Save information about task in Project.
	 *
	 * Creates run configuration
	 * Creates class file
	 * Creates task file
	 *
	 * @return generated CPP File
	 */

	public static VirtualFile saveTaskFile(Task task, Project project) {
		VirtualFile taskFile = FileUtils.findOrCreateByRelativePath(project.getBaseDir(), task.getPath());
		if (taskFile == null) {
			throw new NotificationException("Couldn't find task file to save: " + taskFile.getPath());
		}
		ApplicationManager.getApplication().runWriteAction(
				new Runnable() {
					@Override
					public void run() {
						OutputWriter writer = FileUtils.getOutputWriter(taskFile, this);
						task.saveTask(writer);
						writer.flush();
						writer.close();
					}
				}
		);
		return taskFile;
	}
	public static PsiElement saveTask(Task task, Project project) {
		VirtualFile taskFile = saveTaskFile(task, project);

		createConfigurationForTask(project, task);

		return generateCPP(project, task, taskFile);
	}

	private static PsiElement generateCPP(Project project, Task task, VirtualFile newTaskFile) {
		VirtualFile parent = newTaskFile.getParent();
		PsiDirectory psiParent = PsiManager.getInstance(project).findDirectory(parent);
		if (psiParent == null) {
			throw new NotificationException("Couldn't open parent directory as PSI");
		}

		Language objC = Language.findLanguageByID("ObjectiveC");
		if (objC == null) {
			throw new NotificationException("Language not found");
		}

		PsiFile file = PsiFileFactory.getInstance(project).createFileFromText(
				task.getClassName() + ".cpp",
				objC,
				getTaskContent(project, task.getClassName())
		);
		if (file == null) {
			throw new NotificationException("Couldn't generate file");
		}
		return ApplicationManager.getApplication().runWriteAction(
				(Computable<PsiElement>) () -> psiParent.add(file)
		);

	}

	private static void createConfigurationForTask(Project project, Task task) {
		TaskConfigurationType configurationType = new TaskConfigurationType();
		ConfigurationFactory factory = configurationType.getConfigurationFactories()[0];

		RunManager manager = RunManager.getInstance(project);
		RunnerAndConfigurationSettings configuration = manager.createConfiguration(
				new TaskConfiguration(
						project,
						factory,
						task
				),
				factory
		);
		manager.addConfiguration(configuration, true);

		manager.setSelectedConfiguration(configuration);
	}
}
