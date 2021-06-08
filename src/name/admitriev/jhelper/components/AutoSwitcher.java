package name.admitriev.jhelper.components;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunManagerListener;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import name.admitriev.jhelper.configuration.TaskConfiguration;
import org.jetbrains.annotations.NotNull;

public class AutoSwitcher implements RunManagerListener, FileEditorManagerListener {
	private final Project project;
	private boolean busy = false;

	public AutoSwitcher(Project project) {
		this.project = project;
	}

	@Override
	public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
		selectTask(file);
	}

	private void selectTask(VirtualFile file) {
		Runnable selectTaskRunnable = () -> {
			if (busy || file == null) {
				return;
			}
			RunManagerImpl runManager = RunManagerImpl.getInstanceImpl(project);
			RunnerAndConfigurationSettings oldConfiguration = runManager.getSelectedConfiguration();
			if (oldConfiguration != null && !(oldConfiguration.getConfiguration() instanceof TaskConfiguration)) {
				return;
			}
			for (RunConfiguration configuration : runManager.getAllConfigurationsList()) {
				if (configuration instanceof TaskConfiguration) {
					TaskConfiguration task = (TaskConfiguration) configuration;
					String pathToClassFile = task.getCppPath();
					VirtualFile expectedFie = project.getBaseDir().findFileByRelativePath(pathToClassFile);
					if (file.equals(expectedFie)) {
						busy = true;
						RunManager.getInstance(project).setSelectedConfiguration(
								new RunnerAndConfigurationSettingsImpl(
										runManager,
										configuration,
										false
								)
						);
						busy = false;
						return;
					}
				}
			}

		};

		DumbService.getInstance(project).smartInvokeLater(selectTaskRunnable);
	}

	@Override
	public void selectionChanged(@NotNull FileEditorManagerEvent event) {
		selectTask(event.getNewFile());
	}

	@Override
	public void runConfigurationSelected(RunnerAndConfigurationSettings selectedConfiguration) {
		if (selectedConfiguration == null) {
			return;
		}
		RunConfiguration configuration = selectedConfiguration.getConfiguration();
		if (busy || !(configuration instanceof TaskConfiguration)) {
			return;
		}
		busy = true;
		String pathToClassFile = ((TaskConfiguration) configuration).getCppPath();
		VirtualFile toOpen = project.getBaseDir().findFileByRelativePath(pathToClassFile);
		if (toOpen != null) {
			ApplicationManager.getApplication().invokeAndWait(() -> FileEditorManager.getInstance(project).openFile(
					toOpen,
					true
			), ModalityState.NON_MODAL);
		}
		busy = false;
	}

}
