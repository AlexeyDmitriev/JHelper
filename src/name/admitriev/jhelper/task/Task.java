package name.admitriev.jhelper.task;

import com.intellij.openapi.project.Project;
import name.admitriev.jhelper.components.Configurator;
import net.egork.chelper.task.StreamConfiguration;
import net.egork.chelper.task.Test;
import net.egork.chelper.task.TestType;
import net.egork.chelper.util.InputReader;
import net.egork.chelper.util.OutputWriter;

import java.util.Arrays;

/**
 * Represent problem from programing contest
 */
public class Task {
	private final String name;
	private final String className;
	private final String path;
	private final StreamConfiguration input;
	private final StreamConfiguration output;
	private final TestType testType;
	private final Test[] tests;

	public Task(
			String name,
			String className,
			String path,
			StreamConfiguration input,
			StreamConfiguration output,
			TestType testType,
			Test[] tests
	) {
		this.input = input;
		this.output = output;
		this.name = name;
		this.className = className;
		this.path = path;
		this.testType = testType;
		this.tests = Arrays.copyOf(tests, tests.length);
	}

	public Task(Task task) {
		this(task.name, task.className, task.path, task.input, task.output, task.testType, task.tests);
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

	public Test[] getTests() {
		return Arrays.copyOf(tests, tests.length);
	}

	public void saveTask(OutputWriter out) {
		out.printString(name);
		out.printString(className);
		out.printString(path);
		out.printEnum(input.type);
		out.printString(input.fileName);
		out.printEnum(output.type);
		out.printString(output.fileName);
		out.printEnum(testType);

		out.printLine(tests.length);
		for (Test test : tests) {
			test.saveTest(out);
		}
	}

	public static Task loadTask(InputReader in) {
		String name = in.readString();
		String className = in.readString();
		String path = in.readString();
		StreamConfiguration.StreamType inputStreamType = in.readEnum(StreamConfiguration.StreamType.class);
		String inputFileName = in.readString();
		StreamConfiguration.StreamType outputStreamType = in.readEnum(StreamConfiguration.StreamType.class);
		String outputFileName = in.readString();
		TestType testType = in.readEnum(TestType.class);
		int testsNumber = in.readInt();
		Test[] tests = new Test[testsNumber];
		for (int i = 0; i < testsNumber; ++i) {
			tests[i] = Test.loadTest(in);
		}


		return new Task(
				name,
				className,
				path,
				new StreamConfiguration(inputStreamType, inputFileName),
				new StreamConfiguration(outputStreamType, outputFileName),
				testType,
				tests
		);
	}

	public static Task emptyTask(Project project) {
		return new Task(
				"",
				"",
				String.format(defaultPathFormat(project), ""),
				StreamConfiguration.STANDARD,
				StreamConfiguration.STANDARD,
				TestType.SINGLE,
				new Test[0]
		);
	}

	public static String defaultPathFormat(Project project) {
		Configurator configurator = project.getComponent(Configurator.class);
		Configurator.State configuration = configurator.getState();
		String path = configuration.getTasksDirectory();
		return path + "/%s.task";
	}

	public Task withTests(Test[] newTests) {
		return new Task(name, className, path, input, output, testType, newTests);
	}

	public TestType getTestType() {
		return testType;
	}
}
