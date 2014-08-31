package name.admitriev.jhelper.task;

import com.intellij.openapi.project.Project;
import name.admitriev.jhelper.components.Configurator;
import net.egork.chelper.task.StreamConfiguration;
import net.egork.chelper.util.InputReader;
import net.egork.chelper.util.OutputWriter;

/**
 * Represent problem from programing contest
 */
public class Task {
	private final String name;
	private final String className;
	private final String path;
	private final StreamConfiguration input;
	private final StreamConfiguration output;

	public Task(String name, String className, String path, StreamConfiguration input, StreamConfiguration output) {
		this.input = input;
		this.output = output;
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

	public StreamConfiguration getInput() {
		return input;
	}

	public StreamConfiguration getOutput() {
		return output;
	}

	public Task copy() {
		return new Task(name, className, path, input, output);
	}

	public void saveTask(OutputWriter out) {
		out.printString(name);
		out.printString(className);
		out.printString(path);
		out.printEnum(input.type);
		out.printString(input.fileName);
		out.printEnum(output.type);
		out.printString(output.fileName);
	}

	public static Task loadTask(InputReader in) {
		String name = in.readString();
		String className = in.readString();
		String path = in.readString();
		StreamConfiguration.StreamType inputStreamType = in.readEnum(StreamConfiguration.StreamType.class);
		String inputFileName = in.readString();
		StreamConfiguration.StreamType outputStreamType = in.readEnum(StreamConfiguration.StreamType.class);
		String outputFileName = in.readString();

		return new Task(
				name,
				className,
				path,
				new StreamConfiguration(inputStreamType, inputFileName),
				new StreamConfiguration(outputStreamType, outputFileName)
		);
	}

	public static Task emptyTask(Project project) {
		Configurator configurator = project.getComponent(Configurator.class);
		Configurator.State configuration = configurator.getState();
		String path = configuration.getTasksDirectory();
		return new Task("", "", path + "/.task", StreamConfiguration.STANDARD, StreamConfiguration.STANDARD);
	}
}
