package name.admitriev.jhelper.configuration;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionTarget;
import com.intellij.execution.ExecutionTargetManager;
import com.intellij.execution.Executor;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import name.admitriev.jhelper.exceptions.NotificationException;
import name.admitriev.jhelper.generation.SubmitCodeGenerationUtils;
import name.admitriev.jhelper.task.Task;
import org.antlr.v4.runtime.misc.Nullable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Class for Running TaskConfiguration
 * It isn't fully compliant with {@link com.intellij.execution.runners.ProgramRunner} Interface because {@link #execute} doesn't call {@link RunProfile#getState}
 * as described in <a href="http://confluence.jetbrains.com/display/IDEADEV/Run+Configurations#RunConfigurations-RunningaProcess">IDEA DEV Confluence</a>
 */
public class TaskRunner extends DefaultProgramRunner {

	@NotNull
	@Override
	public String getRunnerId() {
		return "name.admitriev.jhelper.configuration.TaskRunner";
	}

	@Override
	public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
		return profile instanceof TaskConfiguration;
	}

	/**
	 * Runs specified TaskConfiguration: generates code and then runs output configuration.
	 * @throws ClassCastException if {@code environment.getRunProfile()} is not {@link TaskConfiguration}.
	 * @throws ExecutionException if output configuration throws it.
	 * @see ExecutionEnvironment#getRunProfile()
	 */
	@Override
	public void execute(@NotNull ExecutionEnvironment environment, @Nullable Callback callback) throws ExecutionException {
		Project project = environment.getProject();

		TaskConfiguration taskConfiguration = (TaskConfiguration) environment.getRunProfile();
		generateSubmissionFileForTask(project, taskConfiguration);

		List<RunnerAndConfigurationSettings> allSettings = RunManager.getInstance(project).getAllSettings();
		RunnerAndConfigurationSettings outputSettings = null;
		for (RunnerAndConfigurationSettings configuration : allSettings) {
			if (configuration.getName().equals("output")) {
				outputSettings = configuration;
			}
		}
		if (outputSettings == null) {
			throw new NotificationException("No output configuration found");
		}

		/*
		 * Here we have RunnerAndConfigurationSettings and need to run it.
		 * I don't have cross-IDE way to do that because
		 *   running Application with default ExecutionTarget in AppCode doesn't work and
		 *   it's impossible to specify ExecutionTarget in CLion
		 */
		try {
			Class<ProgramRunnerUtil> clazz = ProgramRunnerUtil.class;
			Method executionMethod = clazz.getMethod("executeConfiguration",
					Project.class,
					DataContext.class,
					RunnerAndConfigurationSettings.class,
					Executor.class,
					ExecutionTarget.class,
					RunContentDescriptor.class,
					boolean.class
			);
			// Probably AppCode.

			// get any ExecutionTarget to run RunConfiguration on
			ExecutionTarget target = ExecutionTargetManager.getInstance(project).getTargetsFor(outputSettings).get(0);

			executionMethod.invoke(null, project, null, outputSettings, environment.getExecutor(), target, null, false);
			return;
		}
		catch (NoSuchMethodException ignore) {
			// OK, not AppCode
		}
		catch (InvocationTargetException e) {
			throw new ExecutionException(e);
		}
		catch (IllegalAccessException e) {
			throw new ExecutionException(e);
		}

		// Probably CLion, other IDEs are not supported.
		ProgramRunnerUtil.executeConfiguration(project, outputSettings, environment.getExecutor());
	}

	private static void generateSubmissionFileForTask(Project project, TaskConfiguration taskConfiguration) {
		Task task = taskConfiguration.getTask();
		String pathToTaskFile = task.getPath();
		String pathToClassFile = pathToTaskFile + "/../" + task.getClassName() + ".cpp";
		VirtualFile virtualFile = project.getBaseDir().findFileByRelativePath(pathToClassFile);
		if (virtualFile == null) {
			throw new NotificationException("Task file not found", "Seems your task is in inconsistent state");
		}

		PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
		if (psiFile == null) {
			throw new NotificationException("Couldn't get PSI file for input file");
		}
		SubmitCodeGenerationUtils.generateSubmissionFile(project, psiFile);
	}
}