package name.admitriev.jhelper.ui;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.ui.DocumentAdapter;
import com.jetbrains.cidr.lang.psi.OCBlockStatement;
import com.jetbrains.cidr.lang.psi.OCFile;
import com.jetbrains.cidr.lang.psi.OCFunctionDefinition;
import com.jetbrains.cidr.lang.psi.visitors.OCRecursiveVisitor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.util.concurrent.atomic.AtomicBoolean;

public class UIUtils {
	private UIUtils() {
	}

	/**
	 * Make two fields change simultaneously until the second one ({@code copy}) changed manually.
	 * Maintains equality of {@code main.getText()} and {@code copy.getText()}
	 * <p>
	 * Does nothing if this equality is wrong when method is called.
	 */
	public static void mirrorFields(JTextField main, JTextField copy) {
		mirrorFields(main, copy, "%s");
	}

	/**
	 * Make two fields change simultaneously until the second one ({@code copy}) changed manually.
	 * Maintains equality of {@code String.format(format, main.getText())} and {@code copy.getText()}
	 * <p>
	 * Does nothing if this equality is wrong when method is called.
	 *
	 * @param format format for {@link String#format}. Should contain exactly one format specifier equal to %s
	 */
	public static void mirrorFields(JTextField main, JTextField copy, String format) {
		if (!String.format(format, main.getText()).equals(copy.getText())) {
			// The copy is already changed.
			return;
		}
		AtomicBoolean changingFirst = new AtomicBoolean(false);
		AtomicBoolean secondChanged = new AtomicBoolean(false);
		main.getDocument().addDocumentListener(new DocumentAdapter() {
			@Override
			protected void textChanged(@NotNull DocumentEvent e) {
				if (secondChanged.get()) return;
				while (!changingFirst.compareAndSet(false, true)) {
					// intentionally empty
				}

				copy.setText(String.format(format, main.getText()));
				changingFirst.set(false);
			}
		});

		copy.getDocument().addDocumentListener(new DocumentAdapter() {
			@Override
			protected void textChanged(@NotNull DocumentEvent e) {
				if (!changingFirst.get()) {
					secondChanged.set(true);
				}
			}
		});
	}

	/**
	 * Finds method @{code methodName} in @{code file} and opens it in an editor.
	 */
	public static void openMethodInEditor(Project project, OCFile file, String methodName) {
		new OpenFileDescriptor(project, file.getVirtualFile(), findMethodBody(file, methodName).getTextOffset()).navigate(true);
	}

	private static PsiElement findMethodBody(OCFile file, @NotNull String method) {
		Ref<PsiElement> result = new Ref<>();
		file.accept(new OCRecursiveVisitor() {
			@Override
			public void visitBlockStatement(OCBlockStatement ocBlockStatement) {
				result.set(ocBlockStatement.getOpeningBrace().getNextSibling());
			}

			@Override
			public void visitFunctionDefinition(OCFunctionDefinition ocFunctionDefinition) {
				if (method.equals(ocFunctionDefinition.getName())) {
					// continue recursion
					super.visitFunctionDefinition(ocFunctionDefinition);
				}
			}
		});
		return result.get();
	}
}
