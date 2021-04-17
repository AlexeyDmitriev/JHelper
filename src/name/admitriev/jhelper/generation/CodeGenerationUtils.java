package name.admitriev.jhelper.generation;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import name.admitriev.jhelper.components.Configurator;
import name.admitriev.jhelper.configuration.TaskConfiguration;
import name.admitriev.jhelper.exceptions.NotificationException;
import net.egork.chelper.task.StreamConfiguration;
import net.egork.chelper.task.Test;
import net.egork.chelper.task.TestType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class CodeGenerationUtils {
	private CodeGenerationUtils() {
	}

	/**
	 * Generates main function for testing purposes.
	 *
	 * @param project Project to get configuration from
	 */
	public static void generateRunFile(
		@NotNull Project project,
		@NotNull PsiFile inputFile,
		@NotNull TaskConfiguration task
	) {
		if (FileUtils.isNotCppFile(inputFile)) {
			throw new NotificationException("Not a cpp file", "Only cpp files are currently supported");
		}

		PsiFile psiOutputFile = getRunFile(project);

		FileUtils.writeToFile(
			psiOutputFile,
			generateRunFileContent(
				project,
				task,
				FileUtil.getRelativePath(
					psiOutputFile.getVirtualFile().getParent().getPath(),
					inputFile.getVirtualFile().getPath(),
					'/'
				)
			)
		);
	}

	private static void generateSubmissionFile(
		@NotNull Project project,
		@NotNull PsiFile inputFile,
		@NotNull TaskConfiguration task
	) {
		if (FileUtils.isNotCppFile(inputFile)) {
			throw new NotificationException("Not a cpp file", "Only cpp files are currently supported");
		}

		String result = IncludesProcessor.process(inputFile);
		PsiFile psiOutputFile = getOutputFile(project);

		FileUtils.writeToFile(
			psiOutputFile,
			generateSubmissionFileContent(project, result, task)
		);

		Configurator configurator = project.getComponent(Configurator.class);
		Configurator.State configuration = configurator.getState();

		if (configuration.isCodeEliminationOn()) {
			removeUnusedCode(psiOutputFile);
		}

		if (configuration.isCodeReformattingOn()) {
			new ReformatCodeProcessor(psiOutputFile, false).run();
		}
	}


	private static String generateRunFileContent(Project project, TaskConfiguration task, String path) {
		String template = TemplatesUtils.getTemplate(project, "run");
		template = TemplatesUtils.replaceAll(template, TemplatesUtils.TASK_FILE, path);
		template = TemplatesUtils.replaceAll(template, TemplatesUtils.TESTS, generateTestDeclaration(task.getTests()));
		template = TemplatesUtils.replaceAll(template, TemplatesUtils.CLASS_NAME, task.getClassName());
		template = TemplatesUtils.replaceAll(
			template,
			TemplatesUtils.SOLVER_CALL,
			generateSolverCall(task.getTestType())
		);
		return template;
	}

	private static @NotNull String generateTestDeclaration(Test[] tests) {
		StringBuilder result = new StringBuilder();
		for (Test test : tests) {
			result.append("{")
				.append(quote(test.input))
				.append(", ")
				.append(quote(test.output != null ? test.output : ""))
				.append(", ")
				.append(test.active)
				.append(", ")
				.append(test.output != null)
				.append("},");
		}
		return result.toString();
	}

	private static CharSequence quote(String input) {
		StringBuilder sb = new StringBuilder();
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

	private static String generateSubmissionFileContent(Project project, String code, TaskConfiguration task) {
		String template = TemplatesUtils.getTemplate(project, "submission");
		if (task.getInput().type == StreamConfiguration.StreamType.LOCAL_REGEXP) {
			code = code + '\n' + generateFileNameGetter();
		}
		template = TemplatesUtils.replaceAll(template, TemplatesUtils.CODE, code);
		template = TemplatesUtils.replaceAll(template, TemplatesUtils.CLASS_NAME, task.getClassName());
		template = TemplatesUtils.replaceAll(template, TemplatesUtils.INPUT, getInputDeclaration(task));
		template = TemplatesUtils.replaceAll(template, TemplatesUtils.OUTPUT, getOutputDeclaration(task));
		template = TemplatesUtils.replaceAll(
			template,
			TemplatesUtils.SOLVER_CALL,
			generateSolverCall(task.getTestType())
		);
		return template;
	}

	private static String generateFileNameGetter() {
		return "#include <dirent.h>\n" +
			"#include <stdexcept>\n" +
			"#include <regex>\n" +
			"#include <sys/stat.h>\n" +
			"#include <cstdint>\n" +
			"\n" +
			"std::string getLastFileName(const std::string& regexString) {\n" +
			"\tDIR* dir;\n" +
			"\tdirent* entry;\n" +
			"\tstd::string result = \"\";\n" +
			"\tint64_t resultModificationTime = 0;\n" +
			"\tstd::regex regex(regexString);\n" +
			"\tif ((dir = opendir (\".\")) != NULL) {\n" +
			"\t\twhile ((entry = readdir (dir)) != NULL) {\n" +
			"\t\t\tif (std::regex_match(entry->d_name, regex)) {\n" +
			"\t\t\t\tstruct stat buffer;\n" +
			"\t\t\t\tstat(entry->d_name, &buffer);\n" +
			"\t\t\t\tint64_t modificationTime = static_cast<int64_t>(buffer.st_mtimespec.tv_sec) * 1000000000 +\n" +
			"\t\t\t\t\t\tstatic_cast<int64_t>(buffer.st_mtimespec.tv_nsec);\n" +
			"\n" +
			"\t\t\t\tif (modificationTime > resultModificationTime) {\n" +
			"\t\t\t\t\tresultModificationTime = modificationTime;\n" +
			"\t\t\t\t\tresult = entry->d_name;\n" +
			"\t\t\t\t}\n" +
			"\t\t\t}\n" +
			"\t\t}\n" +
			"\t\tclosedir (dir);\n" +
			"\t} else {\n" +
			"\t\tthrow std::runtime_error(\"Couldn't open current directory\");\n" +
			"\t}\n" +
			"\tif (result.empty()) {\n" +
			"\t\tthrow std::runtime_error(\"No file found\");\n" +
			"\t}" +
			"\treturn result;\n" +
			"}";
	}

	private static String generateSolverCall(TestType testType) {
		switch (testType) {
			case SINGLE:
				return "solver.solve();";
			case MULTI_NUMBER:
				return "int n;\n" +
					"cin >> n;\n" +
					"for(int i = 0; i < n; ++i) {\n" +
					"\tsolver.solve();\n" +
					"}\n";
			case MULTI_EOF:
				return "while(in.good()) {\n" +
					"\tsolver.solve(in, out);\n" +
					"}\n";
			default:
				throw new IllegalArgumentException("Unknown testType:" + testType);
		}
	}

	private static String getOutputDeclaration(TaskConfiguration task) {
		String outputFileName = task.getOutput().getFileName(task.getName(), ".out");
		if (outputFileName == null) {
			return "";
		} else if (task.getOutput().type == StreamConfiguration.StreamType.LOCAL_REGEXP) {
			return "freopen(\"" + outputFileName + "\", \"w\", stdout);";
		}
		throw new NotificationException("Your task is in inconsistent state", "Can't output to local regexp");
	}

	private static String getInputDeclaration(TaskConfiguration task) {
		if (task.getInput().type == StreamConfiguration.StreamType.LOCAL_REGEXP) {
			return "freopen(getLastFileName(" + quote(task.getInput().fileName) + ").c_str(), \"r\", stdin);";
		} else if (task.getInput().type == StreamConfiguration.StreamType.STANDARD) {
			return "";
		} else {
			return "freopen(\"" + task.getInput().getFileName(task.getName(), ".in") + "\", \"r\", stdin);";
		}
	}

	private static @NotNull PsiFile getOutputFile(Project project) {
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

	private static void removeUnusedCode(PsiFile file) {
		while (true) {
			Collection<PsiElement> toDelete = new ArrayList<>();
			Project project = file.getProject();
			SearchScope scope = GlobalSearchScope.fileScope(project, file.getVirtualFile());
			file.acceptChildren(new DeletionMarkingVisitor(toDelete, scope));
			if (toDelete.isEmpty()) {
				break;
			}
			WriteCommandAction.writeCommandAction(project).run(
				() -> toDelete.forEach(PsiElement::delete)
			);
		}
	}

	/**
	 * Generates code for submission.
	 * Adds main function, inlines all used code except standard library and puts it to output file from configuration
	 *
	 * @param project Project to get configuration from
	 */
	public static void generateSubmissionFileForTask(
		@NotNull Project project,
		@NotNull TaskConfiguration taskConfiguration
	) {
		String pathToClassFile = taskConfiguration.getCppPath();
		VirtualFile virtualFile = project.getBaseDir().findFileByRelativePath(pathToClassFile);
		if (virtualFile == null) {
			throw new NotificationException("Task file not found", "Seems your task is in inconsistent state");
		}

		PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
		if (psiFile == null) {
			throw new NotificationException("Couldn't get PSI file for input file");
		}
		generateSubmissionFile(project, psiFile, taskConfiguration);
	}
}
