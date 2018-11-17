package name.admitriev.jhelper.generation;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.cidr.lang.psi.OCFile;
import com.jetbrains.cidr.lang.psi.OCIncludeDirective;
import com.jetbrains.cidr.lang.psi.OCPragma;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class IncludesProcessor {
	private Set<PsiFile> processedFiles = new HashSet<>();
	@SuppressWarnings("StringBufferField")
	private StringBuilder result = new StringBuilder();


	private IncludesProcessor() {
	}

	private void processFile(PsiFile file) {
		if (processedFiles.contains(file)) {
			return;
		}
		processedFiles.add(file);
		for (PsiElement element : file.getChildren()) {
			if (element instanceof OCIncludeDirective) {
				OCIncludeDirective include = (OCIncludeDirective) element;
				if (isInternalInclude(include)) {
					processFile(include.getIncludedFile());
				}
				else {
					processAngleBracketsInclude(include);
				}
				continue;
			}
			if (element instanceof OCPragma) {
				OCPragma pragma = (OCPragma) element;
				if (pragma.getContent(true).first.equals("once")) {
					continue;
				}
			}
			result.append(element.getText());
		}
	}

	private static boolean isInternalInclude(OCIncludeDirective include) {
		PsiFile file = include.getIncludedFile();
		return file != null && ((OCFile) file).isInProjectSources();
	}

	private void processAngleBracketsInclude(OCIncludeDirective include) {
		PsiFile file = include.getIncludedFile();
		if (processedFiles.contains(file)) {
			return;
		}
		processedFiles.add(file);
		result.append(include.getText());
	}

	public static @NotNull String process(PsiFile file) {
		IncludesProcessor processor = new IncludesProcessor();
		processor.processFile(file);
		return processor.result.toString();
	}
}
