package name.admitriev.jhelper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import name.admitriev.jhelper.task.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AddTaskDialog extends DialogWrapper {
	private TaskSettingsComponent component;

	public AddTaskDialog(@NotNull Project project) {
		super(project);
		component = new TaskSettingsComponent(
				project,
				new StreamConfigurationPanel.SizeChangedListener() {
					@Override
					public void sizeChanged() {
						pack();
					}
				}
		);
		init();
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return component;
	}

	public Task getTask() {
		return component.getTask();
	}
}
