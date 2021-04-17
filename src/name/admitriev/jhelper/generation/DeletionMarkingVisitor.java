package name.admitriev.jhelper.generation;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.jetbrains.cidr.lang.psi.OCCppNamespace;
import com.jetbrains.cidr.lang.psi.OCDeclaration;
import com.jetbrains.cidr.lang.psi.OCDeclarator;
import com.jetbrains.cidr.lang.psi.OCElement;
import com.jetbrains.cidr.lang.psi.OCFunctionDefinition;
import com.jetbrains.cidr.lang.psi.OCStructLike;
import com.jetbrains.cidr.lang.psi.OCTypeElement;
import com.jetbrains.cidr.lang.psi.impl.OCDefineDirectiveImpl;
import com.jetbrains.cidr.lang.psi.visitors.OCVisitor;

import java.util.Collection;

public class DeletionMarkingVisitor extends OCVisitor {
	private final Collection<PsiElement> toDelete;
	private final SearchScope searchScope;

	public DeletionMarkingVisitor(Collection<PsiElement> toDelete, SearchScope searchScope) {
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.toDelete = toDelete;
		this.searchScope = searchScope;
	}

	private static boolean isParentFor(OCElement potentialParent, PsiElement potentialChild) {
		while (potentialChild != null) {
			//noinspection ObjectEquality
			if (potentialChild == potentialParent) {
				return true;
			}
			potentialChild = potentialChild.getParent();
		}
		return false;
	}

	@Override
	public void visitFunctionDefinition(OCFunctionDefinition functionDefinition) {
		String name = functionDefinition.getName();
		if ("main".equals(name)) {
			return;
		}
		if (name != null && name.startsWith("operator")) {
			// To workaround a fact that no usages found for operators
			return;
		}
		super.visitFunctionDefinition(functionDefinition);
	}

	private void removeIfNoReference(OCElement element) {
		for (PsiReference reference : ReferencesSearch.search(element, searchScope)) {
			PsiElement referenceElement = reference.getElement();
			if (!isParentFor(element, referenceElement)) {
				return;
			}
		}
		toDelete.add(element);
	}

	@Override
	public void visitNamespace(OCCppNamespace namespace) {
		if (namespace.getChildren().length == 0) {
			toDelete.add(namespace);
		} else {
			namespace.acceptChildren(this);
		}
	}

	@Override
	public void visitDefineDirective(OCDefineDirectiveImpl directive) {
		removeIfNoReference(directive);
	}


	@Override
	public void visitDeclarator(OCDeclarator declarator) {
		removeIfNoReference(declarator);
	}

	@Override
	public void visitStructLike(OCStructLike structLike) {
		removeIfNoReference(structLike);
		structLike.acceptChildren(this);
	}

	@Override
	public void visitTypeElement(OCTypeElement typeElement) {
		typeElement.acceptChildren(this);
	}

	@Override
	public void visitDeclaration(OCDeclaration declaration) {
		declaration.acceptChildren(this);
	}
}
