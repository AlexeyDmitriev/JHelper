package name.admitriev.jhelper.actions;

import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.ide.DataManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.cpp.cmake.model.CMakeConfiguration;
import com.jetbrains.cidr.cpp.cmake.model.CMakeTarget;
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration;
import com.jetbrains.cidr.cpp.execution.CMakeBuildConfigurationHelper;
import com.jetbrains.cidr.execution.BuildTargetAndConfigurationData;
import name.admitriev.jhelper.configuration.TaskRunner;
import name.admitriev.jhelper.ui.Notificator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class SwitchCMakeConfigurationAction extends ComboBoxAction {
	@NotNull
	@Override
	public DefaultActionGroup createPopupActionGroup(JComponent button) {
		DefaultActionGroup actions = new DefaultActionGroup();

		Project project = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(button));
		if (!isProjectValid(project)) {
			return actions;
		}

		final CMakeAppRunConfiguration testRunner = getTestRunner(project);
		if (testRunner == null) {
			Notificator.showNotification(
					"No " + TaskRunner.RUN_CONFIGURATION_NAME + " configuration found",
					NotificationType.ERROR
			);
			return actions;
		}
		final CMakeTarget target = testRunner.getBuildAndRunConfigurations().buildConfiguration.getTarget();
		List<CMakeConfiguration> configurations = new CMakeBuildConfigurationHelper(project).getConfigurations(target);

		for (final CMakeConfiguration configuration : configurations) {
			actions.add(
					new AnAction(configuration.getName()) {
						@Override
						public void actionPerformed(AnActionEvent e) {
							testRunner.setTargetAndConfigurationData(
									new BuildTargetAndConfigurationData(
											target,
											configuration
									)
							);
						}
					}
			);
		}

		return actions;
	}

	@Override
	public void update(AnActionEvent e) {
		Presentation presentation = e.getPresentation();
		presentation.setIcon(null);
		Project project = e.getProject();
		presentation.setIcon(null);
		boolean enabled = false;
		if (isProjectValid(project)) {
			CMakeAppRunConfiguration testRunner = getTestRunner(project);
			if (testRunner != null) {
				presentation.setText(testRunner.getBuildAndRunConfigurations().buildConfiguration.getName());
				enabled = true;
			}
		}

		presentation.setEnabled(enabled);
		if (!enabled) {
			presentation.setText("");
		}
	}

	private static boolean isProjectValid(Project project) {
		return project != null && !project.isDisposed() && project.isInitialized();
	}

	@Nullable
	private static CMakeAppRunConfiguration getTestRunner(Project project) {
		for (RunConfiguration configuration : RunManager.getInstance(project).getAllConfigurationsList()) {
			if (configuration.getName().equals(TaskRunner.RUN_CONFIGURATION_NAME)) {
				return (CMakeAppRunConfiguration) configuration;
			}
		}
		return null;
	}
}
