package name.admitriev.jhelper.components;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.components.AbstractProjectComponent;
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
import net.egork.chelper.parser.AtCoderParser;
import net.egork.chelper.parser.BayanParser;
import net.egork.chelper.parser.CSAcademyParser;
import net.egork.chelper.parser.CodeChefParser;
import net.egork.chelper.parser.CodeforcesParser;
import net.egork.chelper.parser.FacebookParser;
import net.egork.chelper.parser.GCJParser;
import net.egork.chelper.parser.HackerEarthParser;
import net.egork.chelper.parser.HackerRankParser;
import net.egork.chelper.parser.KattisParser;
import net.egork.chelper.parser.Parser;
import net.egork.chelper.parser.UsacoParser;
import net.egork.chelper.parser.YandexParser;
import net.egork.chelper.task.Task;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A Component to monitor request from CHelper Chrome Extension and parse them to Tasks
 */
public class ChromeParser extends AbstractProjectComponent {
	private static final int PORT = 4243;
	private static final Map<String, Parser> PARSERS;

	static {
		Map<String, Parser> taskParsers = new HashMap<>();
		taskParsers.put("yandex", new YandexParser());
		taskParsers.put("codeforces", new CodeforcesParser());
		taskParsers.put("hackerrank", new HackerRankParser());
		taskParsers.put("facebook", new FacebookParser());
		taskParsers.put("usaco", new UsacoParser());
		taskParsers.put("gcj", new GCJParser());
		taskParsers.put("bayan", new BayanParser());
		taskParsers.put("kattis", new KattisParser());
		taskParsers.put("codechef", new CodeChefParser());
		taskParsers.put("hackerearth", new HackerEarthParser());
		taskParsers.put("atcoder", new AtCoderParser());
		taskParsers.put("csacademy", new CSAcademyParser());
		PARSERS = Collections.unmodifiableMap(taskParsers);
	}

	private SimpleHttpServer server = null;

	public ChromeParser(Project project) {
		super(project);
	}

	@Override
	public void projectOpened() {
		try {
			Configurator configurator = myProject.getComponent(Configurator.class);
			Configurator.State configuration = configurator.getState();

			String path = configuration.getTasksDirectory();

			server = new SimpleHttpServer(
					PORT,
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
							PsiElement generatedFile = TaskUtils.saveNewTask(task, myProject);
							UIUtils.openMethodInEditor(myProject, (OCFile) generatedFile, "solve");
						}

						IDEUtils.reloadProject(myProject);
					}
			);

			new Thread(server, "ChromeParserThread").start();
		}
		catch (IOException ignored) {
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
}
