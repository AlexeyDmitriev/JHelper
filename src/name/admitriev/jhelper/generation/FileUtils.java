package name.admitriev.jhelper.generation;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import name.admitriev.jhelper.exceptions.NotificationException;
import net.egork.chelper.util.OutputWriter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class FileUtils {
	private FileUtils() {
	}

	public static OutputWriter getOutputWriter(VirtualFile virtualFile, Object requestor) {
		try {
			return new OutputWriter(virtualFile.getOutputStream(requestor));
		}
		catch (IOException e) {
			throw new NotificationException("Couldn't open virtual file to write", e);
		}
	}

	public static VirtualFile findOrCreateByRelativePath(VirtualFile file, String localPath) {
		return ApplicationManager.getApplication().runWriteAction(
				new Computable<VirtualFile>() {
					@Override
					public VirtualFile compute() {
						String path = localPath;
						if (path.isEmpty()) {
							return file;
						}
						path = StringUtil.trimStart(path, "/");
						int index = path.indexOf('/');
						if (index < 0) {
							index = path.length();
						}
						String name = path.substring(0, index);

						@Nullable VirtualFile child;
						if (name.equals(".")) {
							child = file;
						}
						else if (name.equals("..")) {
							child = file.getParent();
						}
						else {
							child = file.findChild(name);
							if (child == null) {
								try {
									if (index == path.length()) {
										child = file.createChildData(this, name);
									}
									else {
										child = file.createChildDirectory(this, name);
									}
								}
								catch (IOException e) {
									throw new NotificationException(
											"Couldn't create directory: " + file.getPath() + '/' + name,
											e
									);
								}
							}
						}

						assert child != null;

						if (index < path.length()) {
							return findOrCreateByRelativePath(child, path.substring(index + 1));
						}
						return child;
					}
				}
		);
	}

	/**
	 * Checks if given file is a C++ file.
	 * In other words checks if code may be generated for that file
	 */
	public static boolean isCppFile(PsiFile file) {
		return file.getName().endsWith(".cpp");
	}

	public static void writeToFile(PsiFile outputFile, String... strings) {
		Project project = outputFile.getProject();
		Document document = PsiDocumentManager.getInstance(project).getDocument(outputFile);
		if (document == null) {
			throw new NotificationException("Couldn't open output file as document");
		}

		new WriteCommandAction.Simple<Object>(outputFile.getProject(), outputFile) {
			@Override
			public void run() {
				document.deleteString(0, document.getTextLength());
				for (String string : strings) {
					document.insertString(document.getTextLength(), string);
				}
				FileDocumentManager.getInstance().saveDocument(document);
				PsiDocumentManager.getInstance(project).commitDocument(document);
			}
		}.execute();
	}

	public static String relativePath(String parentPath, String childPath) {
		if (!parentPath.endsWith("/")) {
			parentPath += "/";
		}
		if (!isChild(parentPath, childPath)) {
			throw new IllegalArgumentException("childPath should be inside a parentPath");
		}
		// Minimum is needed for case when childPath = parentPath and there's no / at the end of childPath
		return childPath.substring(Math.min(parentPath.length(), childPath.length()));
	}

	public static boolean isChild(String parentPath, String childPath) {
		if (!parentPath.endsWith("/")) {
			parentPath += "/";
		}
		if (!childPath.endsWith("/")) {
			childPath += "/";
		}
		return childPath.startsWith(parentPath);
	}
}
