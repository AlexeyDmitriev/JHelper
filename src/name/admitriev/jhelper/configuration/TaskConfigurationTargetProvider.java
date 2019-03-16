package name.admitriev.jhelper.configuration;

import com.intellij.execution.ExecutionTarget;
import com.intellij.execution.ExecutionTargetManager;
import com.intellij.execution.ExecutionTargetProvider;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskConfigurationTargetProvider extends ExecutionTargetProvider {
	@Override
	public @NotNull List<ExecutionTarget> getTargets(
			@NotNull Project project,
			@NotNull RunConfiguration configuration
	) {
		if (!(configuration instanceof TaskConfiguration)) {
			return Collections.emptyList();
		}
		RunnerAndConfigurationSettings testRunner = TaskRunner.getRunnerSettings(project);
		if (testRunner == null) {
			return Collections.emptyList();
		}
		List<ExecutionTarget> runnerTargets = ExecutionTargetManager.getInstance(project).getTargetsFor(testRunner.getConfiguration());
		List<ExecutionTarget> myTargets = new ArrayList<>();
		for (ExecutionTarget target : runnerTargets) {
			myTargets.add(new TaskConfigurationExecutionTarget(target));
		}
		return myTargets;
	}
}
