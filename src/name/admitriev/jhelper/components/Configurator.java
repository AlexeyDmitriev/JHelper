package name.admitriev.jhelper.components;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;


@State(
		name = "Configurator",
		storages = {
				@Storage(id = "default", file = StoragePathMacros.PROJECT_FILE, scheme = StorageScheme.DEFAULT),
				@Storage(id = "dir", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/JHelper.xml", scheme = StorageScheme.DIRECTORY_BASED)
		}
)
public class Configurator implements ProjectComponent, PersistentStateComponent<Configurator.State> {
	@SuppressWarnings("UnusedParameters")
	public Configurator(Project project) {
		state = new Configurator.State();
	}

	@Override
	public void initComponent() {
	}

	@Override
	public void disposeComponent() {
	}

	@Override
	@NotNull
	public String getComponentName() {
		return "Configurator";
	}

	@Override
	public void projectOpened() {
	}

	@Override
	public void projectClosed() {
	}


	@NotNull
	@Override
	public Configurator.State getState() {
		return state;
	}

	@SuppressWarnings("ParameterHidesMemberVariable")
	@Override
	public void loadState(Configurator.State state) {
		this.state = state;
	}

	private Configurator.State state;
	public static class State {
		private String author;
		private String tasksDirectory;
		private String outputFile;

		public State(String author, String tasksDirectory, String outputFile) {
			this.author = author;
			this.tasksDirectory = tasksDirectory;
			this.outputFile = outputFile;
		}

		public State() {
			this("", "tasks", "output/main.cpp");
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
	}
}
