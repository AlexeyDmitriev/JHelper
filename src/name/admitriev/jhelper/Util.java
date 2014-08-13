package name.admitriev.jhelper;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import net.egork.chelper.util.OutputWriter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class Util {
	private Util() {
	}

	public static OutputWriter getOutputWriter(VirtualFile virtualFile, Object requestor) {
		try {
			return new OutputWriter(virtualFile.getOutputStream(requestor));
		} catch (IOException e) {
			throw new JHelperException("Can't open virtual file to write", e);
		}
	}

	public static VirtualFile findOrCreateByRelativePath(final VirtualFile file, final String localPath) {
		return ApplicationManager.getApplication().runWriteAction(new Computable<VirtualFile>() {
			@Override
			public VirtualFile compute() {
				String path = localPath;
				if (path.isEmpty())
					return file;
				path = StringUtil.trimStart(path, "/");
				int index = path.indexOf('/');
				if (index < 0)
					index = path.length();
				String name = path.substring(0, index);

				@Nullable VirtualFile child;
				if (name.equals(".")) {
					child = file;
				} else if (name.equals("..")) {
					child = file.getParent();
				} else {
					child = file.findChild(name);
					if (child == null) {
						try {
							if (index == path.length()) {
								child = file.createChildData(this, name);
							} else {
								child = file.createChildDirectory(this, name);
							}
						} catch (IOException e) {
							throw new JHelperException("Can't create directory: " + file.getPath() + '/' + name, e);
						}
					}
				}

				assert child != null;

				if (index < path.length()) {
					return findOrCreateByRelativePath(child, path.substring(index + 1));
				}
				return child;
			}
		});
	}

	public static boolean isCppFile(PsiFile file) {
		return file.getName().endsWith(".cpp");
	}
}
