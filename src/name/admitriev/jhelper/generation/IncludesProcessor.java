package name.admitriev.jhelper.generation;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.objc.psi.OCImportDirective;
import com.jetbrains.objc.psi.OCPragma;

import java.util.HashSet;
import java.util.Set;

public class IncludesProcessor {
	private Set<PsiFile> processedFiles = new HashSet<PsiFile>();
	@SuppressWarnings("StringBufferField")
	private StringBuilder result = new StringBuilder();


	private IncludesProcessor() {
	}

	private void processFile(PsiFile file) {
		if(processedFiles.contains(file)) {
			return;
		}
		processedFiles.add(file);
		for (PsiElement element : file.getChildren()) {
			if(element instanceof OCImportDirective) {
				OCImportDirective include = (OCImportDirective)element;
				if(include.isAngleBrackets()) {
					processAngleBracketsInclude(include);
				}
				else {
					processFile(include.getImportedFile());
				}
				continue;
			}
			if(element instanceof OCPragma) {
				OCPragma pragma = (OCPragma) element;
				if(pragma.getContent().trim().equals("once"))
					continue;
			}
			result.append(element.getText());
		}
	}

	private void processAngleBracketsInclude(OCImportDirective include) {
		PsiFile file = include.getImportedFile();
		if(processedFiles.contains(file)) {
			return;
		}
		processedFiles.add(file);
		result.append(include.getText());
	}

	public static String process(PsiFile file) {
		IncludesProcessor processor = new IncludesProcessor();
		processor.processFile(file);
		return processor.result.toString();
	}
}
