package name.admitriev.jhelper.configuration;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import name.admitriev.jhelper.task.Task;
import org.jetbrains.annotations.NotNull;

public class TaskConfigurationFactory extends ConfigurationFactory {
	TaskConfigurationFactory(@NotNull ConfigurationType type) {
		super(type);
	}

	@Override
	public RunConfiguration createTemplateConfiguration(Project project) {
		return new TaskConfiguration(project, this, Task.emptyTask(project));
	}
}
