package name.admitriev.jhelper.configuration;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.project.Project;
import name.admitriev.jhelper.task.Task;
import org.jetbrains.annotations.NotNull;

public class TaskConfigurationFactory extends ConfigurationFactory {
	public TaskConfigurationFactory(@NotNull ConfigurationType type) {
		super(type);
	}

	@NotNull
	@Override
	public TaskConfiguration createTemplateConfiguration(@NotNull Project project) {
		return new TaskConfiguration(project, this, Task.emptyTask(project));
	}

	@Override
	public boolean isApplicable(@NotNull Project project) {
		return false;
	}
}
