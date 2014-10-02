package name.admitriev.jhelper.generation;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import name.admitriev.jhelper.exceptions.JHelperException;
import name.admitriev.jhelper.exceptions.NotificationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utility class for customizing templates of code
 */
public class TemplatesUtils {

	public static final String CLASS_NAME = "%ClassName%";
	public static final String TASK_FILE = "%TaskFile%";
	public static final String TESTS = "%Tests%";
	public static final String SOLVER_CALL = "%SolverCall%";
	public static final String INPUT = "%Input%";
	public static final String OUTPUT = "%Output%";
	public static final String CODE = "%Code%";

	private TemplatesUtils() {
	}


	public static String getTemplate(Project project, String name) {
		String filename = name + ".template";
		VirtualFile file = project.getBaseDir().findFileByRelativePath(filename);
		if (file == null) {
			createTemplateFromDefault(project, name);
			file = project.getBaseDir().findFileByRelativePath(filename);
			if (file == null) {
				throw new JHelperException("Can't open template file(" + filename + ") after its creation");
			}

		}
		Document document = FileDocumentManager.getInstance().getDocument(file);
		if (document == null) {
			throw new NotificationException("Couldn't find template \"" + name + '"');
		}

		return document.getText();
	}

	private static void createTemplateFromDefault(Project project, String name) {
		String filename = name + ".template";
		VirtualFile file = FileUtils.findOrCreateByRelativePath(project.getBaseDir(), filename);
		PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
		String defaultTemplate;
		try {
			defaultTemplate = getResourceContent("/name/admitriev/jhelper/templates/" + filename);
		}
		catch (IOException e) {
			throw new NotificationException("Couldn't open default template " + filename, e);
		}

		FileUtils.writeToFile(psiFile, defaultTemplate);
	}

	/**
	 * Returns content of resource file (from resource folder) as a string.
	 */
	private static String getResourceContent(String name) throws IOException {
		InputStream stream = null;
		BufferedReader reader = null;
		try {
			stream = TemplatesUtils.class.getResourceAsStream(name);
			if (stream == null) {
				throw new IOException("Couldn't open a stream to resource " + name);
			}
			reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String line;
			//noinspection NestedAssignment
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
			return sb.toString();
		}
		finally {
			if (stream != null) {
				stream.close();
			}
			if (reader != null) {
				reader.close();
			}
		}
	}

}
