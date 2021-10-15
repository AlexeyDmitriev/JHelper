package name.admitriev.jhelper.components;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.text.StringTokenizer;
import com.jetbrains.cidr.lang.psi.OCFile;
import name.admitriev.jhelper.IDEUtils;
import name.admitriev.jhelper.network.SimpleHttpServer;
import name.admitriev.jhelper.task.TaskData;
import name.admitriev.jhelper.task.TaskUtils;
import name.admitriev.jhelper.ui.Notificator;
import name.admitriev.jhelper.ui.UIUtils;
import net.egork.chelper.parser.*;
import net.egork.chelper.task.Task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;

/**
 * A Component to monitor request from CHelper Chrome Extension and parse them to Tasks
 */
public class ChromeParser implements ProjectComponent {
	private static final int PORT = 4243;
	private static final Map<String, Parser> PARSERS;
	private final Project project;
	private SimpleHttpServer server = null;
	public ChromeParser(Project project) {
		this.project = project;
	}

	@Override
	public void projectOpened() {
		try {
			server = new SimpleHttpServer(
				new InetSocketAddress("localhost", PORT),
				request -> {
					StringTokenizer st = new StringTokenizer(request);
					String type = st.nextToken();
					Parser parser = PARSERS.get(type);
					if (parser == null) {
						Notificator.showNotification(
							"Unknown parser",
							"Parser " + type + " unknown, request ignored",
							NotificationType.INFORMATION
						);
						return;
					}
					String page = request.substring(st.getCurrentPosition());
					Collection<Task> tasks = parser.parseTaskFromHTML(page);
					if (tasks.isEmpty()) {
						Notificator.showNotification(
							"Couldn't parse any task",
							"Maybe format changed?",
							NotificationType.WARNING
						);
					}

					Configurator configurator = project.getComponent(Configurator.class);
					Configurator.State configuration = configurator.getState();
					String path = configuration.getTasksDirectory();
					for (Task rawTask : tasks) {
						TaskData task = new TaskData(
							rawTask.name,
							rawTask.taskClass,
							String.format("%s/%s.cpp", path, rawTask.taskClass),
							rawTask.input,
							rawTask.output,
							rawTask.testType,
							rawTask.tests
						);
						PsiElement generatedFile = TaskUtils.saveNewTask(task, project);
						UIUtils.openMethodInEditor(project, (OCFile) generatedFile, "solve");
					}

					IDEUtils.reloadProject(project);
				}
			);

			new Thread(server, "ChromeParserThread").start();
		} catch (IOException ignored) {
			Notificator.showNotification(
				"Could not create serverSocket for Chrome parser",
				"Probably another CHelper or JHelper project is running?",
				NotificationType.ERROR
			);
		}
	}

	@Override
	public void projectClosed() {
		if (server != null) {
			server.stop();
		}
	}

	static {
		PARSERS = Map.ofEntries(
			Map.entry("yandex", new YandexParser()),
			Map.entry("codeforces", new CodeforcesParser()),
			Map.entry("hackerrank", new HackerRankParser()),
			Map.entry("facebook", new FacebookParser()),
			Map.entry("usaco", new UsacoParser()),
			Map.entry("gcj", new GCJParser()),
			Map.entry("bayan", new BayanParser()),
			Map.entry("kattis", new KattisParser()),
			Map.entry("codechef", new CodeChefParser()),
			Map.entry("hackerearth", new HackerEarthParser()),
			Map.entry("atcoder", new AtCoderParser()),
			Map.entry("csacademy", new CSAcademyParser()),
			Map.entry("new-gcj", new NewGCJParser()),
			Map.entry("json", new JSONParser())
		);
	}
}
