package name.admitriev.jhelper.generation;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import name.admitriev.jhelper.exceptions.JHelperException;
import name.admitriev.jhelper.exceptions.NotificationException;

/**
 * Utility class for customizing templates of code
 */
public class TemplatesUtils {
	private static final String TASK_TEMPLATE = "task.template";

	private static final String DEFAULT_TASK_TEMPLATE = "#include <iostream>\n" +
	                                                    '\n' +
	                                                    "class %ClassName% {\n" +
	                                                    "public:\n" +
	                                                    "\tvoid solve(std::istream& in, std::ostream& out) {\n" +
	                                                    "\t\t\n" +
	                                                    "\t}\n" +
	                                                    "};\n";

	private TemplatesUtils() {
	}

	/**
	 * Generates task file content depending on custom user template
	 */
	public static String getTaskContent(Project project, String className) {
		VirtualFile file = project.getBaseDir().findFileByRelativePath(TASK_TEMPLATE);
		if (file == null) {
			createDefaultTaskTemplate(project);
			file = project.getBaseDir().findFileByRelativePath(TASK_TEMPLATE);
			if (file == null) {
				throw new JHelperException("Can't open template file(" + TASK_TEMPLATE + ") after its creation");
			}
		}

		Document document = FileDocumentManager.getInstance().getDocument(file);
		if (document == null) {
			throw new NotificationException("Couldn't find template for tasks");
		}
		String template = document.getText();
		template = template.replaceAll("%ClassName%", className);
		return template;
	}

	private static void createDefaultTaskTemplate(Project project) {
		VirtualFile file = FileUtils.findOrCreateByRelativePath(project.getBaseDir(), TASK_TEMPLATE);
		PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
		assert psiFile != null;
		FileUtils.writeToFile(psiFile, DEFAULT_TASK_TEMPLATE);
	}

}
