package name.admitriev.jhelper.generation;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import name.admitriev.jhelper.components.Configurator;
import name.admitriev.jhelper.exceptions.NotificationException;
import name.admitriev.jhelper.task.Task;
import net.egork.chelper.task.StreamConfiguration;
import net.egork.chelper.task.Test;
import net.egork.chelper.task.TestType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class CodeGenerationUtils {
	private CodeGenerationUtils() {
	}

	/**
	 * Generates main function for testing purposes.
	 *
	 * @param project Project to get configuration from
	 */
	public static void generateRunFile(Project project, @NotNull PsiFile inputFile, Task task) {
		if (!FileUtils.isCppFile(inputFile)) {
			throw new NotificationException("Not a cpp file", "Only cpp files are currently supported");
		}

		if (project == null) {
			throw new NotificationException("No project found", "Are you in any project?");
		}

		PsiFile psiOutputFile = getRunFile(project);

		FileUtils.writeToFile(
				psiOutputFile,
				generateRunFileContent(project, task, inputFile.getVirtualFile().getPath())
		);
	}

	/**
	 * Generates code for submission.
	 * Adds main function, inlines all used code except standard library and puts it to output file from configuration
	 *
	 * @param project Project to get configuration from
	 */
	public static void generateSubmissionFile(Project project, @NotNull PsiFile inputFile, Task task) {

		if (!FileUtils.isCppFile(inputFile)) {
			throw new NotificationException("Not a cpp file", "Only cpp files are currently supported");
		}

		if (project == null) {
			throw new NotificationException("No project found", "Are you in any project?");
		}

		String result = IncludesProcessor.process(inputFile);
		PsiFile psiOutputFile = getOutputFile(project);

		FileUtils.writeToFile(
				psiOutputFile,
				authorComment(project),
				generateSubmissionFileContent(project, result, task)
		);

		removeUnusedCode(psiOutputFile);
	}


	private static String generateRunFileContent(Project project, Task task, String path) {
		String template = TemplatesUtils.getTemplate(project, "run");
		template = template.replace(TemplatesUtils.TASK_FILE, path);
		template = template.replace(TemplatesUtils.TESTS, generateTestDeclaration(task.getTests()));
		template = template.replace(TemplatesUtils.CLASS_NAME, task.getClassName());
		template = template.replace(TemplatesUtils.SOLVER_CALL, generateSolverCall(task.getTestType()));
		return template;
	}

	private static String generateTestDeclaration(Test[] tests) {
		StringBuilder result = new StringBuilder();
		for (Test test : tests) {
			result.append("{" + quote(test.input) + ", " + quote(test.output) + "},");
		}
		return result.toString();
	}

	private static CharSequence quote(String input) {
		StringBuilder sb = new StringBuilder("");
		sb.append('"');
		for (char c : input.toCharArray()) {
			if (c == '\n') {
				sb.append("\\n");
				continue;
			}
			if (c == '"' || c == '\'' || c == '\\') {
				sb.append('\\');
			}
			sb.append(c);
		}
		sb.append('"');
		return sb;
	}

	private static String generateSubmissionFileContent(Project project, String code, Task task) {

		String template = TemplatesUtils.getTemplate(project, "submission");
		template = template.replace(TemplatesUtils.CODE, code);
		template = template.replace(TemplatesUtils.CLASS_NAME, task.getClassName());
		template = template.replace(TemplatesUtils.INPUT, getInputDeclaration(task));
		template = template.replace(TemplatesUtils.OUTPUT, getOutputDeclaration(task));
		template = template.replace(TemplatesUtils.SOLVER_CALL, generateSolverCall(task.getTestType()));
		return template;
	}

	private static String generateSolverCall(TestType testType) {
		switch (testType) {
			case SINGLE:
				return "solver.solve(in, out);";
			case MULTI_NUMBER:
				return "int n;\n" +
				       "in >> n;\n" +
				       "for(int i = 0; i < n; ++i) {\n" +
				       "\tsolver.solve(in, out);\n" +
				       "}\n";
			case MULTI_EOF:
				return "while(in.good()) {\n" +
				       "\tsolver.solve(in, out);\n" +
				       "}\n";
			default:
				throw new IllegalArgumentException("Unknown testType:" + testType);
		}
	}

	private static String getOutputDeclaration(Task task) {
		String outputFileName = task.getOutput().getFileName(task.getName(), ".out");
		if (outputFileName == null) {
			return "std::ostream& out(std::cout);";
		}
		else if (task.getOutput().type == StreamConfiguration.StreamType.LOCAL_REGEXP) {
			throw new NotificationException("Your task is in inconsistent state", "Can't output to local regexp");
		}
		else {
			return "std::ofstream out(\"" + outputFileName + "\");";
		}
	}

	private static String getInputDeclaration(Task task) {
		String inputFileName = task.getInput().getFileName(task.getName(), ".in");

		if (inputFileName == null) {
			return "std::istream& in(std::cin);";
		}
		else if (task.getInput().type == StreamConfiguration.StreamType.LOCAL_REGEXP) {
			throw new NotificationException("Local regexps aren't supported yet");
		}
		else {
			return "std::ifstream in(\"" + inputFileName + "\");";
		}
	}

	@NotNull
	private static PsiFile getOutputFile(Project project) {
		Configurator configurator = project.getComponent(Configurator.class);
		Configurator.State configuration = configurator.getState();

		VirtualFile outputFile = project.getBaseDir().findFileByRelativePath(configuration.getOutputFile());
		if (outputFile == null) {
			throw new NotificationException(
					"No output file found.",
					"You should configure output file to point to existing file"
			);
		}

		PsiFile psiOutputFile = PsiManager.getInstance(project).findFile(outputFile);
		if (psiOutputFile == null) {
			throw new NotificationException("Couldn't open output file as PSI");
		}
		return psiOutputFile;
	}


	@NotNull
	private static PsiFile getRunFile(Project project) {
		Configurator configurator = project.getComponent(Configurator.class);
		Configurator.State configuration = configurator.getState();

		VirtualFile outputFile = project.getBaseDir().findFileByRelativePath(configuration.getRunFile());
		if (outputFile == null) {
			throw new NotificationException(
					"No run file found.",
					"You should configure run file to point to existing file"
			);
		}

		PsiFile psiOutputFile = PsiManager.getInstance(project).findFile(outputFile);
		if (psiOutputFile == null) {
			throw new NotificationException("Couldn't open run file as PSI");
		}
		return psiOutputFile;
	}

	private static String authorComment(Project project) {
		Configurator configurator = project.getComponent(Configurator.class);
		Configurator.State configuration = configurator.getState();

		return "/**\n" +
		       " * code generated by JHelper\n" +
		       " * More info: https://github.com/AlexeyDmitriev/JHelper\n" +
		       " * @author " + configuration.getAuthor() + '\n' +
		       " */\n\n";
	}

	private static void removeUnusedCode(PsiFile file) {
		while (true) {
			final Collection<PsiElement> toDelete = new ArrayList<PsiElement>();
			Project project = file.getProject();
			SearchScope scope = new GlobalSearchScope.FilesScope(
					project,
					Collections.singletonList(file.getVirtualFile())
			);
			file.acceptChildren(new DeletionMarkingVisitor(toDelete, scope));
			if (toDelete.isEmpty()) {
				break;
			}
			new WriteCommandAction.Simple<Object>(project, file) {
				@Override
				public void run() {
					for (PsiElement element : toDelete) {
						element.delete();
					}
				}
			}.execute();
		}
	}
}
