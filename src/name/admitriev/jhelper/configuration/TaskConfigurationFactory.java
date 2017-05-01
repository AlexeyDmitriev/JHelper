package name.admitriev.jhelper.configuration;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class TaskConfigurationFactory extends ConfigurationFactory {
	public TaskConfigurationFactory(@NotNull ConfigurationType type) {
		super(type);
	}

	@NotNull
	@Override
	public TaskConfiguration createTemplateConfiguration(@NotNull Project project) {
		return new TaskConfiguration(project, this);
	}

	@Override
	public boolean isApplicable(@NotNull Project project) {
		return false;
	}
}
