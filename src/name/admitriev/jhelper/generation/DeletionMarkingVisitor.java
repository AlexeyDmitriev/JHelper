package name.admitriev.jhelper.generation;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.jetbrains.objc.psi.OCCppNamespace;
import com.jetbrains.objc.psi.OCDeclaration;
import com.jetbrains.objc.psi.OCDeclarator;
import com.jetbrains.objc.psi.OCElement;
import com.jetbrains.objc.psi.OCFunctionDefinition;
import com.jetbrains.objc.psi.OCFunctionPredefinition;
import com.jetbrains.objc.psi.OCStruct;
import com.jetbrains.objc.psi.impl.OCDefineDirectiveImpl;
import com.jetbrains.objc.psi.visitors.OCVisitor;
import name.admitriev.jhelper.JHelperException;

import java.util.Collection;
import java.util.List;

public class DeletionMarkingVisitor extends OCVisitor {
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
		removeIfNoReference(functionDefinition);
	}

	private void removeIfNoReference(OCElement element) {
		for (PsiReference reference : ReferencesSearch.search(element, searchScope)) {
			PsiElement referenceElement = reference.getElement();
			if(!isParentFor(element, referenceElement)) {
				return;
			}
		}
		toDelete.add(element);
	}

	@Override
	public void visitElement(PsiElement element) {
		super.visitElement(element);
	}

	@Override
	public void visitNamespace(OCCppNamespace namespace) {
		if(namespace.getChildren().length == 0) {
			toDelete.add(namespace);
		}
		else {
			namespace.acceptChildren(this);
		}
	}

	@Override
	public void visitDefineDirective(OCDefineDirectiveImpl directive) {
		removeIfNoReference(directive);
	}

	@Override
	public void visitFunctionPredefinition(OCFunctionPredefinition predefinition) {
		removeIfNoReference(predefinition);
	}

	@Override
	public void visitDeclaration(OCDeclaration declaration) {
		List<OCDeclarator> variables = declaration.getDeclarators();
		if(variables.isEmpty()) {
			PsiElement[] types = declaration.getTypeElement().getChildren();
			switch (types.length) {
				case 0:
					break;
				case 1:
					if(types[0] instanceof OCStruct) {
						OCStruct struct = (OCStruct) types[0];
						removeIfNoReference(struct);
						struct.acceptChildren(this);
					}
					else {
						throw new JHelperException("Type is not a OCStruct. Please file a bug at https://github.com/AlexeyDmitriev/JHelper/issues with stack trace and your code");
					}
					break;
				default:
					throw new JHelperException("2 or more children in declaration. Please file a bug at https://github.com/AlexeyDmitriev/JHelper/issues with stack trace and your code");
			}
		}
		else {
			for (OCDeclarator variable : variables) {
				removeIfNoReference(variable);
			}
		}

	}
}
