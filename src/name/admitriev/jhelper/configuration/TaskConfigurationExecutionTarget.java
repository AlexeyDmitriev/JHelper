package name.admitriev.jhelper.configuration;

import com.intellij.execution.ExecutionTarget;
import com.intellij.execution.RunnerAndConfigurationSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class TaskConfigurationExecutionTarget extends ExecutionTarget {
	private final ExecutionTarget originalTarget;

	TaskConfigurationExecutionTarget(ExecutionTarget originalTarget) {
		this.originalTarget = originalTarget;
	}

	@Override
	public @NotNull String getId() {
		return "name.admitriev.jhelper.configuration.TaskConfigurationExecutionTarget" + originalTarget.getId();
	}

	@Override
	public @NotNull String getDisplayName() {
		return originalTarget.getDisplayName();
	}

	@Override
	public @Nullable Icon getIcon() {
		return originalTarget.getIcon();
	}

	@Override
	public boolean canRun(@NotNull RunnerAndConfigurationSettings runnerAndConfigurationSettings) {
		return runnerAndConfigurationSettings.getConfiguration() instanceof TaskConfiguration;
	}

	public ExecutionTarget getOriginalTarget() {
		return originalTarget;
	}
}
