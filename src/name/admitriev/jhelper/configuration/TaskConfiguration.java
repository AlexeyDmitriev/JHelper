package name.admitriev.jhelper.configuration;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import name.admitriev.jhelper.task.Task;
import name.admitriev.jhelper.ui.TaskSettingsComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public class TaskConfiguration extends RunConfigurationBase {
	private Task task;
	public TaskConfiguration(Project project, ConfigurationFactory factory, Task task) {
		super(project, factory, task.getName());
		this.task = task;
	}

	@NotNull
	@Override
	public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
		return new SettingsEditor<TaskConfiguration>() {

			private TaskSettingsComponent component = new TaskSettingsComponent(getProject());
			@Override
			protected void resetEditorFrom(TaskConfiguration s) {
				component.setTask(s.task);
			}

			@Override
			protected void applyEditorTo(TaskConfiguration s) {
				s.task = component.getTask();
			}

			@NotNull
			@Override
			protected JComponent createEditor() {
				return component;
			}
		};
	}

	@Override
	public void checkConfiguration() throws RuntimeConfigurationException {
	}

	@Nullable
	@Override
	public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
		throw new ExecutionException("Can't run anything yet");
	}

	@Override
	public TaskConfiguration clone() {
		TaskConfiguration newConfiguration = (TaskConfiguration)super.clone();
		newConfiguration.task = task.copy();
		return newConfiguration;
	}
}
