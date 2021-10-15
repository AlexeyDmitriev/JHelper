package name.admitriev.jhelper.configuration;

import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.SimpleConfigurationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NotNullLazyValue;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TaskConfigurationType extends SimpleConfigurationType {
	public TaskConfigurationType() {
		super(
			"name.admitriev.jhelper.configuration.TaskConfigurationType",
			"Task",
			"Task for JHelper",
			new NotNullLazyValue<>() {
				@NotNull
				@Override
				protected Icon compute() {
					return IconLoader.getIcon("/name/admitriev/jhelper/icons/task.png");
				}
			}
		);
	}

	@NotNull
	@Override
	public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
		return new TaskConfiguration(project, this);
	}

}
