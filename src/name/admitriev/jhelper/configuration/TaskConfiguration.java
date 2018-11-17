package name.admitriev.jhelper.configuration;

import com.intellij.execution.ExecutionTarget;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.execution.CidrCommandLineState;
import name.admitriev.jhelper.exceptions.JHelperException;
import name.admitriev.jhelper.task.TaskData;
import name.admitriev.jhelper.ui.TaskSettingsComponent;
import net.egork.chelper.task.StreamConfiguration;
import net.egork.chelper.task.Test;
import net.egork.chelper.task.TestType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Run Configuration for running JHelper tasks
 */
public class TaskConfiguration extends RunConfigurationBase {
	private String className;
	private String cppPath;
	private StreamConfiguration input;
	private StreamConfiguration output;
	private TestType testType;
	private Test[] tests;

	@Override
	public boolean canRunOn(@NotNull ExecutionTarget target) {
		return target instanceof TaskConfigurationExecutionTarget;
	}

	public TaskConfiguration(Project project, ConfigurationFactory factory) {
		super(project, factory, "");
		className = "";
		cppPath = "";
		input = new StreamConfiguration(StreamConfiguration.StreamType.STANDARD);
		output = new StreamConfiguration(StreamConfiguration.StreamType.STANDARD);
		testType = TestType.SINGLE;
		tests = new Test[0];
	}

	@NotNull
	@Override
	public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
		return new SettingsEditor<TaskConfiguration>() {
			private TaskSettingsComponent component = new TaskSettingsComponent(getProject(), false);

			@Override
			protected void resetEditorFrom(@NotNull TaskConfiguration settings) {
				component.setTaskData(new TaskData(
						getName(),
						className,
						cppPath,
						input,
						output,
						testType,
						new Test[0]
				));
			}

			@Override
			protected void applyEditorTo(@NotNull TaskConfiguration settings) {
				TaskData data = component.getTask();
				settings.className = data.getClassName();
				settings.cppPath = data.getCppPath();
				settings.input = data.getInput();
				settings.output = data.getOutput();
				settings.testType = data.getTestType();
			}

			@Override
			protected @NotNull JComponent createEditor() {
				return component;
			}
		};
	}

	@Override
	public void checkConfiguration() throws RuntimeConfigurationException {
	}

	@Override
	public @Nullable CidrCommandLineState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
		throw new JHelperException("This method is not expected to be used");
	}

	@Override
	public TaskConfiguration clone() {
		TaskConfiguration newConfiguration = (TaskConfiguration) super.clone();
		newConfiguration.className = className;
		newConfiguration.cppPath = cppPath;
		newConfiguration.input = input;
		newConfiguration.output = output;
		newConfiguration.testType = testType;
		newConfiguration.tests = tests.clone();
		return newConfiguration;
	}

	private static StreamConfiguration readStreamConfiguration(
			Element element,
			String typeAttribute,
			String filenameAttribute
	) {
		try {
			StreamConfiguration.StreamType inputType = StreamConfiguration.StreamType.valueOf(
					element.getAttribute(typeAttribute).getValue()
			);
			if (inputType.hasStringParameter) {
				String filename = element.getAttributeValue(filenameAttribute);
				return new StreamConfiguration(inputType, filename);
			}
			else {
				return new StreamConfiguration(inputType);
			}
		} catch (RuntimeException ignored) {
			return StreamConfiguration.STANDARD;
		}
	}

	@Override
	public void readExternal(Element element) {
		super.readExternal(element);
		className = element.getAttributeValue("className", "");
		cppPath = element.getAttributeValue("cppPath", "");
		input = readStreamConfiguration(element, "inputPath", "inputFile");
		output = readStreamConfiguration(element, "outputPath", "outputFile");
		try {
			testType = TestType.valueOf(element.getAttributeValue("testType", "SINGLE"));
		} catch (IllegalArgumentException ignored) {
			testType = TestType.SINGLE;
		}

		List<Element> children = element.getChildren();
		for (Element child : children) {
			if (Objects.equals(child.getName(), "tests")) {
				List<Element> testChildren = child.getChildren();
				tests = new Test[testChildren.size()];
				for (int i = 0; i < testChildren.size(); ++i) {
					tests[i] = readTest(testChildren.get(i));
				}
			}
		}
	}

	private static Test readTest(Element element) {
		assert element.getName().equals("test");
		String input = element.getAttributeValue("input");
		String output = element.getAttributeValue("output");
		boolean active = element.getAttributeValue("active").equals("true");
		return new Test(input, output, 0, active);
	}

	@Override
	public void writeExternal(Element element) {
		element.setAttribute("className", className);
		element.setAttribute("cppPath", cppPath);
		element.setAttribute("inputType", String.valueOf(input.type.name()));
		if (input.fileName != null) {
			element.setAttribute("inputFile", input.fileName);
		}
		element.setAttribute("outputType", String.valueOf(output.type.name()));
		if (output.fileName != null) {
			element.setAttribute("outputFile", output.fileName);
		}
		element.setAttribute("testType", testType.name());

		Element testsElements = new Element("tests");
		for (Test test : tests) {
			Element testElement = new Element("test");
			testElement.setAttribute("input", test.input);
			if (test.output != null) {
				testElement.setAttribute("output", test.output);
			}
			testElement.setAttribute("active", String.valueOf(test.active));
			testsElements.addContent(testElement);
		}
		element.addContent(testsElements);

		super.writeExternal(element);
	}

	public void setFromTaskData(TaskData data) {
		setName(data.getName());
		className = data.getClassName();
		cppPath = data.getCppPath();
		input = data.getInput();
		output = data.getOutput();
		testType = data.getTestType();
		tests = data.getTests();
	}

	public void setTests(Test[] tests) {
		this.tests = Arrays.copyOf(tests, tests.length);
	}

	public Test[] getTests() {
		return Arrays.copyOf(tests, tests.length);
	}

	public String getCppPath() {
		return cppPath;
	}

	public String getClassName() {
		return className;
	}

	public TestType getTestType() {
		return testType;
	}

	public StreamConfiguration getInput() {
		return input;
	}

	public StreamConfiguration getOutput() {
		return output;
	}
}
