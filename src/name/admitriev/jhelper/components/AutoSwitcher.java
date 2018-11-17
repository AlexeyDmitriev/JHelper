package name.admitriev.jhelper.components;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunManagerListener;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.openapi.application.TransactionGuard;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import name.admitriev.jhelper.configuration.TaskConfiguration;
import org.jetbrains.annotations.NotNull;

public class AutoSwitcher implements ProjectComponent {
	private final Project project;
	private boolean busy = false;

	public AutoSwitcher(Project project) {
		this.project = project;
	}

	@Override
	public @NotNull
	String getComponentName() {
		return "AutoSwitcher";
	}

	@Override
	public void projectOpened() {
		addSelectedConfigurationListener();
		addFileEditorListeners();
	}

	private void addFileEditorListeners() {
		MessageBus messageBus = project.getMessageBus();
		messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
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
		});
	}

	private void addSelectedConfigurationListener() {
		RunManagerImpl.getInstanceImpl(project).addRunManagerListener(new RunManagerListener() {
			@Override
			public void runConfigurationSelected() {
				RunnerAndConfigurationSettings selectedConfiguration =
						RunManagerImpl.getInstanceImpl(project).getSelectedConfiguration();
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
					TransactionGuard.getInstance().submitTransactionAndWait(() -> FileEditorManager.getInstance(project).openFile(
							toOpen,
							true
					));
				}
				busy = false;
			}
		});
	}

}
