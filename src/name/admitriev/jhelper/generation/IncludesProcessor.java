package name.admitriev.jhelper.generation;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.objc.psi.OCImportDirective;
import com.jetbrains.objc.psi.OCPragma;

import java.util.HashSet;
import java.util.Set;

public class IncludesProcessor {
	private Set<PsiFile> usedFiles = null;
	@SuppressWarnings("StringBufferField")
	private StringBuilder result = null;


	public String process(PsiFile file) {
		usedFiles = new HashSet<PsiFile>();
		result = new StringBuilder();
		processFile(file);
		return result.toString();
	}

	private void processFile(PsiFile file) {
		if(usedFiles.contains(file)) {
			return;
		}
		usedFiles.add(file);
		for (PsiElement element : file.getChildren()) {
			System.err.println(element);
			boolean processed = false;
			if(element instanceof OCImportDirective) {
				OCImportDirective include = (OCImportDirective)element;
				if(!include.isAngleBrackets()) {
					processFile(include.getImportedFile());
					processed = true;
				}
			}
			else if(element instanceof OCPragma) {
				OCPragma pragma = (OCPragma) element;
				if("once".equals(pragma.getContent().trim()))
					processed = true;
			}
			if(!processed)
				result.append(element.getText()).append('\n');
		}
	}
}
