package name.admitriev.jhelper;

import com.intellij.execution.ExecutionTarget;
import com.intellij.execution.ExecutionTargetManager;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace;

public class IDEUtils {
	private IDEUtils() {
	}

	public static void reloadProject(Project project) {
		CMakeWorkspace.getInstance(project).scheduleReload(true);
	}

	public static void chooseConfigurationAndTarget(
		Project project,
		RunnerAndConfigurationSettings runConfiguration,
		ExecutionTarget target
	) {
		RunManager.getInstance(project).setSelectedConfiguration(runConfiguration);
		ExecutionTargetManager.getInstance(project).setActiveTarget(target);
	}
}
