package name.admitriev.jhelper.configuration;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import name.admitriev.jhelper.exceptions.NotificationException;
import name.admitriev.jhelper.generation.CodeGenerationUtils;
import name.admitriev.jhelper.task.Task;
import org.antlr.v4.runtime.misc.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Class for Running TaskConfiguration
 * It isn't fully compliant with {@link com.intellij.execution.runners.ProgramRunner} Interface because {@link #execute} doesn't call {@link RunProfile#getState}
 * as described in <a href="http://confluence.jetbrains.com/display/IDEADEV/Run+Configurations#RunConfigurations-RunningaProcess">IDEA DEV Confluence</a>
 */
public class TaskRunner extends DefaultProgramRunner {

	public static final String RUN_CONFIGURATION_NAME = "testrunner";

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
	 *
	 * @throws ClassCastException if {@code environment.getRunProfile()} is not {@link TaskConfiguration}.
	 * @throws ExecutionException if output configuration throws it.
	 * @see ExecutionEnvironment#getRunProfile()
	 */
	@Override
	public void execute(@NotNull ExecutionEnvironment environment, @Nullable Callback callback) throws
			ExecutionException {
		Project project = environment.getProject();

		TaskConfiguration taskConfiguration = (TaskConfiguration) environment.getRunProfile();
		generateSubmissionFileForTask(project, taskConfiguration);

		generateRunFileForTask(project, taskConfiguration);

		List<RunnerAndConfigurationSettings> allSettings = RunManager.getInstance(project).getAllSettings();
		RunnerAndConfigurationSettings outputSettings = null;
		for (RunnerAndConfigurationSettings configuration : allSettings) {
			if (configuration.getName().equals(RUN_CONFIGURATION_NAME)) {
				outputSettings = configuration;
			}
		}
		if (outputSettings == null) {
			throw new NotificationException("No run configuration found", "It should be called (" + RUN_CONFIGURATION_NAME + ")");
		}
		ProgramRunnerUtil.executeConfiguration(project, outputSettings, environment.getExecutor());
	}

	private static void generateRunFileForTask(Project project, TaskConfiguration taskConfiguration) {
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

		CodeGenerationUtils.generateRunFile(project, psiFile, task);

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
		CodeGenerationUtils.generateSubmissionFile(project, psiFile, task);
	}
}
