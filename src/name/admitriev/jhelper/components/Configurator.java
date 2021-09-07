package name.admitriev.jhelper.components;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;


@State(name = "Configurator", storages = @Storage("JHelper.xml"))
@Service(Service.Level.PROJECT)
public final class Configurator implements PersistentStateComponent<Configurator.State> {
	public Configurator() {
		state = new Configurator.State();
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

	private Configurator.State state;

	public static class State {
		private String author;
		private String tasksDirectory;
		private String outputFile;
		private String runFile;
		private String archiveDirectory;
		private boolean codeEliminationOn;
		private boolean codeReformattingOn;

		public State(
				String author,
				String tasksDirectory,
				String outputFile,
				String runFile,
				String archiveDirectory,
				boolean codeEliminationOn,
				boolean codeReformattingOn
		) {
			this.author = author;
			this.tasksDirectory = tasksDirectory;
			this.outputFile = outputFile;
			this.runFile = runFile;
			this.archiveDirectory = archiveDirectory;
			this.codeEliminationOn = codeEliminationOn;
			this.codeReformattingOn = codeReformattingOn;
		}

		public State() {
			this("", "tasks", "output/main.cpp", "testrunner/main.cpp", "archive", false, false);
		}

		public String getAuthor() {
			return author;
		}

		public String getTasksDirectory() {
			return tasksDirectory;
		}

		public String getOutputFile() {
			return outputFile;
		}

		public String getRunFile() {
			return runFile;
		}

		public String getArchiveDirectory() {
			return archiveDirectory;
		}

		public boolean isCodeEliminationOn() {
			return codeEliminationOn;
		}

		public boolean isCodeReformattingOn() {
			return codeReformattingOn;
		}

		@Deprecated
		public void setAuthor(String author) {
			this.author = author;
		}

		@Deprecated
		public void setTasksDirectory(String tasksDirectory) {
			this.tasksDirectory = tasksDirectory;
		}

		@Deprecated
		public void setOutputFile(String outputFile) {
			this.outputFile = outputFile;
		}

		@Deprecated
		public void setRunFile(String runFile) {
			this.runFile = runFile;
		}

		@Deprecated
		public void setCodeEliminationOn(boolean codeEliminationOn) {
			this.codeEliminationOn = codeEliminationOn;
		}

		@Deprecated
		public void setCodeReformattingOn(boolean codeReformattingOn) {
			this.codeReformattingOn = codeReformattingOn;
		}
	}
}
