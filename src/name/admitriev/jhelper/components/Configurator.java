package name.admitriev.jhelper.components;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;


@State(name = "Configurator", storages = @Storage("/JHelper.xml"))
public class Configurator implements ProjectComponent, PersistentStateComponent<Configurator.State> {
	private Configurator.State state;

	public Configurator() {
		state = new Configurator.State();
	}

	@Override
	public @NotNull String getComponentName() {
		return "Configurator";
	}

	@Override
	public @NotNull Configurator.State getState() {
		return state;
	}

	@SuppressWarnings("ParameterHidesMemberVariable")
	@Override
	public void loadState(@NotNull Configurator.State state) {
		this.state = state;
	}

	public static class State {
		private final String archiveDirectory;
		private String tasksDirectory;
		private String outputFile;
		private String runFile;
		private boolean codeEliminationOn;
		private boolean codeReformattingOn;

		public State() {
			this("tasks",
				"archive",
				"output/main.cpp",
				"testrunner/main.cpp",
				false,
				false
			);
		}

		public State(
			String tasksDirectory,
			String archiveDirectory,
			String outputFile,
			String runFile,
			boolean codeEliminationOn,
			boolean codeReformattingOn
		) {
			this.tasksDirectory = tasksDirectory;
			this.archiveDirectory = archiveDirectory;
			this.outputFile = outputFile;
			this.runFile = runFile;
			this.codeEliminationOn = codeEliminationOn;
			this.codeReformattingOn = codeReformattingOn;
		}

		public String getTasksDirectory() {
			return tasksDirectory;
		}

		@Deprecated
		public void setTasksDirectory(String tasksDirectory) {
			this.tasksDirectory = tasksDirectory;
		}

		public String getArchiveDirectory() {
			return archiveDirectory;
		}

		public String getOutputFile() {
			return outputFile;
		}

		@Deprecated
		public void setOutputFile(String outputFile) {
			this.outputFile = outputFile;
		}

		public String getRunFile() {
			return runFile;
		}

		@Deprecated
		public void setRunFile(String runFile) {
			this.runFile = runFile;
		}

		public boolean isCodeEliminationOn() {
			return codeEliminationOn;
		}

		@Deprecated
		public void setCodeEliminationOn(boolean codeEliminationOn) {
			this.codeEliminationOn = codeEliminationOn;
		}

		public boolean isCodeReformattingOn() {
			return codeReformattingOn;
		}

		@Deprecated
		public void setCodeReformattingOn(boolean codeReformattingOn) {
			this.codeReformattingOn = codeReformattingOn;
		}
	}
}
