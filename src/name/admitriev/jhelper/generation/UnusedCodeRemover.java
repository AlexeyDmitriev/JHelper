package name.admitriev.jhelper.generation;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class UnusedCodeRemover {
	private UnusedCodeRemover() {
	}

	public static void remove(PsiFile file) {
		while (true) {
			final Collection<PsiElement> toDelete = new ArrayList<PsiElement>();
			Project project = file.getProject();
			SearchScope scope = new GlobalSearchScope.FilesScope(project, Collections.singletonList(file.getVirtualFile()));
			file.acceptChildren(new DeletionMarkingVisitor(toDelete, scope));
			if(toDelete.isEmpty()) {
				break;
			}
			new WriteCommandAction.Simple<Object>(project, file) {
				@Override
				public void run() {
					for (PsiElement element : toDelete) {
						element.delete();
					}
				}
			}.execute();

		}
	}

}