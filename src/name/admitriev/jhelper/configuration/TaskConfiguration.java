package name.admitriev.jhelper.configuration;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.cidr.execution.CidrCommandLineState;
import name.admitriev.jhelper.exceptions.JHelperException;
import name.admitriev.jhelper.task.Task;
import name.admitriev.jhelper.task.TaskUtils;
import name.admitriev.jhelper.ui.TaskSettingsComponent;
import net.egork.chelper.util.InputReader;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * Run Configuration for running JHelper tasks
 */
public class TaskConfiguration extends RunConfigurationBase {
	private Task task;
	private Project project;

	public TaskConfiguration(Project project, ConfigurationFactory factory, Task task) {
		super(project, factory, task.getName());
		this.task = task;
		this.project = project;
	}

	@NotNull
	@Override
	public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
		return new SettingsEditor<TaskConfiguration>() {
			private TaskSettingsComponent component = new TaskSettingsComponent(getProject());

			@Override
			protected void resetEditorFrom(@NotNull TaskConfiguration settings) {
				component.setTask(settings.task);
			}

			@Override
			protected void applyEditorTo(@NotNull TaskConfiguration settings) {
				settings.task = component.getTask();
				setName(settings.task.getName());
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
	public CidrCommandLineState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
		throw new JHelperException("This method is not expected to be used");
	}

	@Override
	public TaskConfiguration clone() {
		TaskConfiguration newConfiguration = (TaskConfiguration) super.clone();
		newConfiguration.task = new Task(task);
		newConfiguration.project = project;
		return newConfiguration;
	}

	@Override
	public void readExternal(Element element) {
		super.readExternal(element);
		String path = element.getAttribute("task_path").getValue();
		VirtualFile projectFile = getProject().getBaseDir();
		VirtualFile taskFile = projectFile.findFileByRelativePath(path);
		if (taskFile == null) {
			return;
		}
		try (InputStream stream = taskFile.getInputStream()) {
			task = Task.loadTask(new InputReader(stream));
		}
		catch (IOException ignored) {
		}

		setName(task.getName());
	}

	@Override
	public void writeExternal(Element element) {
		element.setAttribute("task_path", task.getPath());
		super.writeExternal(element);
		VirtualFile taskFile = project.getBaseDir().findFileByRelativePath(task.getPath());
		if (taskFile == null) {
			return;
		}
		TaskUtils.saveTaskFile(task, project);
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}
}
