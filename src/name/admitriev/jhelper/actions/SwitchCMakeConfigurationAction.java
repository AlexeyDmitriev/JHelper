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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.cpp.cmake.CMakeSettings;
import com.jetbrains.cidr.cpp.cmake.CMakeSettingsKt;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace;
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration;
import com.jetbrains.cidr.execution.BuildTargetAndConfigurationData;
import com.jetbrains.cidr.execution.BuildTargetData;
import name.admitriev.jhelper.configuration.TaskRunner;
import name.admitriev.jhelper.ui.Notificator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

public class SwitchCMakeConfigurationAction extends ComboBoxAction {
	@NotNull
	@Override
	public DefaultActionGroup createPopupActionGroup(JComponent button) {
		DefaultActionGroup actions = new DefaultActionGroup();

		final Project project = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(button));
		if (!isProjectValid(project)) {
			return actions;
		}
		final CMakeSettings cmakeSettings = CMakeSettings.Companion.getInstance(project);
		List<CMakeSettings.Configuration> settings = cmakeSettings.getConfigurations();
		if (settings.isEmpty()) {
			Notificator.showNotification("here", NotificationType.ERROR);
			return actions;
		}
		CMakeWorkspace workspace = CMakeWorkspace.getInstance(project);
		final CMakeSettings.Configuration chosenCMakeConfiguration = getCurrentConfiguration(project);
		String chosenCMakeConfigurationName = chosenCMakeConfiguration.getConfigName();

		List<String> cmakeConfigurations = workspace.getRegisteredConfigurationsTypesFor(chosenCMakeConfigurationName);

		final CMakeAppRunConfiguration testRunner = getTestRunner(project);
		if (testRunner == null) {
			Notificator.showNotification(
					"No configuration named " + TaskRunner.RUN_CONFIGURATION_NAME + " found",
					NotificationType.ERROR
			);
			return actions;
		}
		for (final String cmakeConfiguration : cmakeConfigurations) {
			actions.add(new AnAction(cmakeConfiguration) {
				@Override
				public void actionPerformed(AnActionEvent anActionEvent) {
					final CMakeSettings.Configuration newCMakeConfiguration = new CMakeSettings.Configuration(
							CMakeSettingsKt.normalizeConfigName(cmakeConfiguration),
							chosenCMakeConfiguration.getGenerationOptions(),
							chosenCMakeConfiguration.getGenerationPassSystemEnvironment(),
							chosenCMakeConfiguration.getAdditionalGenerationEnvironment(),
							null
					);
					ApplicationManager.getApplication().runWriteAction(new Runnable() {
						@Override
						public void run() {
							cmakeSettings.setConfigurations(Collections.singletonList(newCMakeConfiguration));
						}
					});
					BuildTargetData targetData = testRunner.getTargetAndConfigurationData().target;
					testRunner.setTargetAndConfigurationData(
							new BuildTargetAndConfigurationData(
									targetData,
									cmakeConfiguration
							)
					);
				}
			});
		}
		return actions;
	}

	@Override
	public void update(AnActionEvent e) {
		Project project = e.getProject();
		Presentation presentation = e.getPresentation();
		CMakeSettings.Configuration configuration = getCurrentConfiguration(project);
		if (configuration == null) {
			presentation.setEnabled(false);
			presentation.setText("");
		}
		else {
			presentation.setEnabled(true);
			presentation.setText(configuration.getConfigName());
		}
	}

	@Nullable
	private CMakeSettings.Configuration getCurrentConfiguration(@Nullable Project project) {
		if (!isProjectValid(project)) {
			return null;
		}
		CMakeSettings cmakeSettings = CMakeSettings.Companion.getInstance(project);
		List<CMakeSettings.Configuration> settings = cmakeSettings.getConfigurations();
		if (settings.size() == 1) {
			return settings.get(0);
		}
		else {
			return null;
		}
	}

	private static boolean isProjectValid(Project project) {
		return project != null && !project.isDisposed() && project.isInitialized();
	}

	@Nullable
	private static CMakeAppRunConfiguration getTestRunner(@NotNull Project project) {
		for (RunConfiguration configuration : RunManager.getInstance(project).getAllConfigurationsList()) {
			if (configuration.getName().equals(TaskRunner.RUN_CONFIGURATION_NAME)) {
				return (CMakeAppRunConfiguration) configuration;
			}
		}
		return null;
	}
}
