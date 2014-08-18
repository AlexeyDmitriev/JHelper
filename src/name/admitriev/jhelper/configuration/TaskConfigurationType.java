package name.admitriev.jhelper.configuration;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.openapi.util.IconLoader;

public class TaskConfigurationType extends ConfigurationTypeBase {

	public TaskConfigurationType() {
		super("name.admitriev.jhelper.configuration.TaskConfigurationType", "Task", "Task for JHelper", IconLoader.getIcon("/name/admitriev/jhelper/icons/task.png"));
		//noinspection ThisEscapedInObjectConstruction
		addFactory(new TaskConfigurationFactory(this));
	}

}
