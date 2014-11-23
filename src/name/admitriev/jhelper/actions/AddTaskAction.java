package name.admitriev.jhelper.actions;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.ide.IdeView;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.jetbrains.objc.psi.OCBlockStatement;
import com.jetbrains.objc.psi.OCFile;
import com.jetbrains.objc.psi.OCFunctionDefinition;
import com.jetbrains.objc.psi.visitors.OCRecursiveVisitor;
import name.admitriev.jhelper.configuration.TaskConfiguration;
import name.admitriev.jhelper.configuration.TaskConfigurationType;
import name.admitriev.jhelper.exceptions.JHelperException;
import name.admitriev.jhelper.exceptions.NotificationException;
import name.admitriev.jhelper.generation.FileUtils;
import name.admitriev.jhelper.generation.TemplatesUtils;
import name.admitriev.jhelper.task.Task;
import name.admitriev.jhelper.ui.AddTaskDialog;
import net.egork.chelper.util.OutputWriter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

public class AddTaskAction extends BaseAction {
	/**
	 * Generates task file content depending on custom user template
	 */
	public static String getTaskContent(Project project, String className) {
		String template = TemplatesUtils.getTemplate(project, "task");
		template = template.replace(TemplatesUtils.CLASS_NAME, className);
		return template;
	}

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
		final Task task = dialog.getTask();

		final VirtualFile newTaskFile = FileUtils.findOrCreateByRelativePath(project.getBaseDir(), task.getPath());
		ApplicationManager.getApplication().runWriteAction(
				new Runnable() {
					@Override
					public void run() {
						OutputWriter writer = FileUtils.getOutputWriter(newTaskFile, this);
						task.saveTask(writer);
						writer.flush();
						writer.close();
					}
				}
		);

		createConfigurationForTask(project, task);

		PsiElement generatedFile = generateCPP(project, task, newTaskFile);
		IdeView view = e.getData(LangDataKeys.IDE_VIEW);
		if (view != null) {
			view.selectElement(findMethodBody((OCFile) generatedFile, "solve"));
		}

		reloadProjectInCLion(project);
	}

	private static PsiElement findMethodBody(OCFile file, @NotNull final String method) {
		final Ref<PsiElement> result = new Ref<PsiElement>();
		file.accept(
				new OCRecursiveVisitor() {
					@Override
					public void visitFunctionDefinition(OCFunctionDefinition ocFunctionDefinition) {
						if (method.equals(ocFunctionDefinition.getName())) {
							// continue recursion
							super.visitFunctionDefinition(ocFunctionDefinition);
						}
					}

					@Override
					public void visitBlockStatement(OCBlockStatement ocBlockStatement) {
						result.set(ocBlockStatement.getOpeningBrace().getNextSibling());
					}
				}
		);
		return result.get();
	}

	private static void reloadProjectInCLion(Project project) {
		String errorMessage = "Couldn't reload a CLion project. API changed?";
		try {
			Class<?> clz = AddTaskAction.class.getClassLoader().loadClass("com.jetbrains.cidr.cpp.cmake.CMakeWorkspace");
			Object instance = clz.getMethod("getInstance", Project.class).invoke(null, project);
			clz.getMethod("scheduleReload", boolean.class).invoke(instance, true);
		}
		catch (ClassNotFoundException ignored) {
			// Probably not a CLion, ignore
		}
		catch (InvocationTargetException e) {
			throw new JHelperException(errorMessage, e);
		}
		catch (NoSuchMethodException e) {
			throw new JHelperException(errorMessage, e);
		}
		catch (IllegalAccessException e) {
			throw new JHelperException(errorMessage, e);
		}
	}

	private static PsiElement generateCPP(Project project, Task task, VirtualFile newTaskFile) {
		VirtualFile parent = newTaskFile.getParent();
		final PsiDirectory psiParent = PsiManager.getInstance(project).findDirectory(parent);
		if (psiParent == null) {
			throw new NotificationException("Couldn't open parent directory as PSI");
		}

		Language objC = Language.findLanguageByID("ObjectiveC");
		if (objC == null) {
			throw new NotificationException("Language not found");
		}

		final PsiFile file = PsiFileFactory.getInstance(project).createFileFromText(
				task.getClassName() + ".cpp",
				objC,
				getTaskContent(project, task.getClassName())
		);
		if (file == null) {
			throw new NotificationException("Couldn't generate file");
		}
		return ApplicationManager.getApplication().runWriteAction(
				new Computable<PsiElement>() {
					@Override
					public PsiElement compute() {
						return psiParent.add(file);
					}
				}
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
