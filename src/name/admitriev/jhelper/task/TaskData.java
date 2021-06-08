package name.admitriev.jhelper.task;

import com.intellij.openapi.project.Project;
import name.admitriev.jhelper.components.Configurator;
import net.egork.chelper.task.StreamConfiguration;
import net.egork.chelper.task.Test;
import net.egork.chelper.task.TestType;

import java.util.Arrays;

/**
 * Represent configuration of a task
 */
public class TaskData {
	private final String name;
	private final String className;
	private final String cppPath;
	private final StreamConfiguration input;
	private final StreamConfiguration output;
	private final TestType testType;
	private final Test[] tests;

	public TaskData(
			String name,
			String className,
			String cppPath,
			StreamConfiguration input,
			StreamConfiguration output,
			TestType testType,
			Test[] tests
	) {
		this.input = input;
		this.output = output;
		this.name = name;
		this.className = className;
		this.cppPath = cppPath;
		this.testType = testType;
		this.tests = Arrays.copyOf(tests, tests.length);
	}

	public TaskData(TaskData task) {
		this(task.name, task.className, task.cppPath, task.input, task.output, task.testType, task.tests);
	}

	public String getName() {
		return name;
	}

	public String getClassName() {
		return className;
	}

	public String getCppPath() {
		return cppPath;
	}

	public StreamConfiguration getInput() {
		return input;
	}

	public StreamConfiguration getOutput() {
		return output;
	}

	public Test[] getTests() {
		return Arrays.copyOf(tests, tests.length);
	}

	public static TaskData emptyTaskData(Project project) {
		return new TaskData(
				"",
				"",
				String.format(defaultCppPathFormat(project), ""),
				StreamConfiguration.STANDARD,
				StreamConfiguration.STANDARD,
				TestType.SINGLE,
				new Test[0]
		);
	}

	public static String defaultCppPathFormat(Project project) {
		Configurator configurator = project.getService(Configurator.class);
		Configurator.State configuration = configurator.getState();
		String path = configuration.getTasksDirectory();
		return path + "/%s.cpp";
	}

	public TestType getTestType() {
		return testType;
	}

}
