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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for customizing templates of code
 */
public class TemplatesUtils {

    public static final Pattern CLASS_NAME = Pattern.compile("%ClassName%", Pattern.LITERAL);
    public static final Pattern TASK_FILE = Pattern.compile("%TaskFile%", Pattern.LITERAL);
    public static final Pattern TESTS = Pattern.compile("%Tests%", Pattern.LITERAL);
    public static final Pattern SOLVER_CALL = Pattern.compile("%SolverCall%", Pattern.LITERAL);
    public static final Pattern INPUT = Pattern.compile("%Input%", Pattern.LITERAL);
    public static final Pattern OUTPUT = Pattern.compile("%Output%", Pattern.LITERAL);
    public static final Pattern CODE = Pattern.compile("%Code%", Pattern.LITERAL);

    private TemplatesUtils() {
    }

    public static String replaceAll(String text, Pattern pattern, String replacement) {
        return pattern.matcher(text).replaceAll(Matcher.quoteReplacement(replacement));
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
        } catch (IOException e) {
            throw new NotificationException("Couldn't open default template " + filename, e);
        }

        FileUtils.writeToFile(psiFile, defaultTemplate);
    }

    /**
     * Returns content of resource file (from resource folder) as a string.
     */
    private static String getResourceContent(String name) throws IOException {
        try (InputStream stream = TemplatesUtils.class.getResourceAsStream(name)) {
            if (stream == null) {
                throw new IOException("Couldn't open a stream to resource " + name);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                return sb.toString();
            }
        }
    }

}
