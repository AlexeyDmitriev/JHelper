package name.admitriev.jhelper.generation;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.jetbrains.objc.psi.OCCppNamespace;
import com.jetbrains.objc.psi.OCElement;
import com.jetbrains.objc.psi.OCFunctionDefinition;
import com.jetbrains.objc.psi.visitors.OCVisitor;

import java.util.Collection;

class DeletionMarkingVisitor extends OCVisitor {
	private final Collection<PsiElement> toDelete;
	private SearchScope searchScope;

	public DeletionMarkingVisitor(Collection<PsiElement> toDelete, SearchScope searchScope) {
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.toDelete = toDelete;
		this.searchScope = searchScope;
	}

	private static boolean isParentFor(OCElement potentialParent, PsiElement potentialChild) {
		while (potentialChild != null) {
			//noinspection ObjectEquality
			if(potentialChild == potentialParent) {
				return true;
			}
			potentialChild = potentialChild.getParent();
		}
		return false;
	}

	@Override
	public void visitFunctionDefinition(OCFunctionDefinition functionDefinition) {
		if("main".equals(functionDefinition.getName())) {
			return;
		}
		System.err.println("function " + functionDefinition.getName());
		removeIfNoReference(functionDefinition);
	}

	private void removeIfNoReference(OCElement element) {
		for (PsiReference reference : ReferencesSearch.search(element, searchScope)) {
			PsiElement referenceElement = reference.getElement();
			System.err.println("reference found:" + referenceElement.getContainingFile());
			System.err.println("context:" + referenceElement.getParent().getParent().getText());
			if(!isParentFor(element, referenceElement)) {
				return;
			}
		}
		System.err.println("marking to remove");
		toDelete.add(element);
	}

	@Override
	public void visitElement(PsiElement element) {
		//System.err.println("ignoring " + element);
		super.visitElement(element);
	}

	@Override
	public void visitNamespace(OCCppNamespace namespace) {
		namespace.acceptChildren(this);
	}


}
