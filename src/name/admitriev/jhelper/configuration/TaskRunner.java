package name.admitriev.jhelper.configuration;

import com.intellij.execution.ExecutionTarget;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import name.admitriev.jhelper.IDEUtils;
import name.admitriev.jhelper.exceptions.NotificationException;
import name.admitriev.jhelper.generation.CodeGenerationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Class for Running TaskConfiguration
 * It isn't fully compliant with {@link ProgramRunner} Interface because {@link #execute} doesn't call {@link RunProfile#getState}
 * as described in <a href="http://confluence.jetbrains.com/display/IDEADEV/Run+Configurations#RunConfigurations-RunningaProcess">IDEA DEV Confluence</a>
 */
public class TaskRunner implements ProgramRunner<RunnerSettings> {
    private static final String RUN_CONFIGURATION_NAME = "testrunner";

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
     * @see ExecutionEnvironment#getRunProfile()
     */
    @Override
    public void execute(@NotNull ExecutionEnvironment environment) {
        Project project = environment.getProject();

        TaskConfiguration taskConfiguration = (TaskConfiguration) environment.getRunProfile();
        CodeGenerationUtils.generateSubmissionFileForTask(project, taskConfiguration);

        generateRunFileForTask(project, taskConfiguration);

        List<RunnerAndConfigurationSettings> allSettings = RunManager.getInstance(project).getAllSettings();
        RunnerAndConfigurationSettings testRunnerSettings = null;
        for (RunnerAndConfigurationSettings configuration : allSettings) {
            if (configuration.getName().equals(RUN_CONFIGURATION_NAME)) {
                testRunnerSettings = configuration;
            }
        }
        if (testRunnerSettings == null) {
            throw new NotificationException(
                "No run configuration found",
                "It should be called (" + RUN_CONFIGURATION_NAME + ")"
            );
        }

        ExecutionTarget originalExecutionTarget = environment.getExecutionTarget();
        ExecutionTarget testRunnerExecutionTarget = ((TaskConfigurationExecutionTarget) originalExecutionTarget).getOriginalTarget();
        RunnerAndConfigurationSettings originalSettings = environment.getRunnerAndConfigurationSettings();

        IDEUtils.chooseConfigurationAndTarget(project, testRunnerSettings, testRunnerExecutionTarget);
        ProgramRunnerUtil.executeConfiguration(testRunnerSettings, environment.getExecutor());

        IDEUtils.chooseConfigurationAndTarget(project, originalSettings, originalExecutionTarget);
    }

    @Nullable
    public static RunnerAndConfigurationSettings getRunnerSettings(@NotNull Project project) {
        return getSettingsByName(project, RUN_CONFIGURATION_NAME);
    }

    private static void generateRunFileForTask(Project project, TaskConfiguration taskConfiguration) {
        String pathToClassFile = taskConfiguration.getCppPath();
        VirtualFile virtualFile = project.getBaseDir().findFileByRelativePath(pathToClassFile);
        if (virtualFile == null) {
            throw new NotificationException("Task file not found", "Seems your task is in inconsistent state");
        }

        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile == null) {
            throw new NotificationException("Couldn't get PSI file for input file");
        }

        CodeGenerationUtils.generateRunFile(project, psiFile, taskConfiguration);
    }

    @Nullable
    private static RunnerAndConfigurationSettings getSettingsByName(@NotNull Project project, String name) {
        for (RunnerAndConfigurationSettings configuration : RunManager.getInstance(project).getAllSettings()) {
            if (configuration.getName().equals(name)) {
                return configuration;
            }
        }
        return null;
    }
}
