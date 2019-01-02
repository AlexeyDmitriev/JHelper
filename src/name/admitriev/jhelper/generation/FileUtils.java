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
import org.jetbrains.annotations.NotNull;
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

	@Nullable
	private static VirtualFile findChild(VirtualFile file, @NotNull String child) {
		if (child.equals(".")) {
			return file;
		}
		if (child.equals("..")) {
			return file.getParent();
		}
		return file.findChild(child);
	}

	public static VirtualFile findOrCreateByRelativePath(VirtualFile root, String localPath) {
		return ApplicationManager.getApplication().runWriteAction(
				new Computable<VirtualFile>() {
					@Override
					public VirtualFile compute() {
						String path = localPath;
						path = StringUtil.trimStart(path, "/");
						if (path.isEmpty()) {
							return root;
						}
						int index = path.indexOf('/');
						if (index < 0) {
							index = path.length();
						}
						String name = path.substring(0, index);

						@Nullable VirtualFile child = findChild(root, name);
						if (child == null) {
							try {
								if (index == path.length()) {
									child = root.createChildData(this, name);
								}
								else {
									child = root.createChildDirectory(this, name);
								}
							}
							catch (IOException e) {
								throw new NotificationException(
										"Couldn't create directory: " + root.getPath() + '/' + name,
										e
								);
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

		WriteCommandAction.writeCommandAction(project).run(
				() -> {
					document.deleteString(0, document.getTextLength());
					for (String string : strings) {
						document.insertString(document.getTextLength(), string);
					}
					FileDocumentManager.getInstance().saveDocument(document);
					PsiDocumentManager.getInstance(project).commitDocument(document);
				}
		);
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

	public static String getDirectory(String filePath) {
		int index = filePath.lastIndexOf('/');
		if (index < 0) {
			return ".";
		}
		return filePath.substring(0, index + 1);
	}

	public static String getFilename(String filePath) {
		int index = filePath.lastIndexOf('/');
		if (index < 0) {
			return filePath;
		}
		return filePath.substring(index + 1);
	}
}
