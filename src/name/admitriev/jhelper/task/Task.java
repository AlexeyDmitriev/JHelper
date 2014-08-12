package name.admitriev.jhelper.task;

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
}
