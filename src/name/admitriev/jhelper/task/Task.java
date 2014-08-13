package name.admitriev.jhelper.task;

import com.intellij.openapi.project.Project;
import name.admitriev.jhelper.components.Configurator;
import net.egork.chelper.util.InputReader;
import net.egork.chelper.util.OutputWriter;

public class Task {
	private final String name;
	private final String className;
	private final String path;

	public Task(String name, String className, String path) {
		this.name = name;
		this.className = className;
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public String getClassName() {
		return className;
	}

	public String getPath() {
		return path;
	}

	public Task copy() {
		return new Task(name, className, path);
	}

	public void saveTask(OutputWriter out) {
		out.printString(name);
		out.printString(className);
		out.printString(path);
	}

	public static Task loadTask(InputReader in) {
		String name = in.readString();
		String className = in.readString();
		String path = in.readString();
		return new Task(name, className, path);
	}

	public static Task emptyTask(Project project) {
		Configurator configurator = project.getComponent(Configurator.class);
		Configurator.State configuration = configurator.getState();
		String path = configuration.getTasksDirectory();
		return new Task("", "", path + "/.task");
	}
}
