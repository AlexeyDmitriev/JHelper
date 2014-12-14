package name.admitriev.jhelper.ui;

import com.intellij.ui.DocumentAdapter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.util.concurrent.atomic.AtomicBoolean;

public class UIUtils {
	private UIUtils() {
	}

	/**
	 * Make two fields change simultaneously until the second one ({@code copy}) changed manually.
	 * Maintains equality of {@code String.format(format, main.getText())} and {@code copy.getText()}
	 *
	 * Does nothing if this equality is wrong when method is called.
	 *
	 * @param format format for {@link java.lang.String#format}. Should contain exactly one format specifier equal to %s
	 */
	public static void mirrorFields(final JTextField main, final JTextField copy, final String format) {
		if (!String.format(format, main.getText()).equals(copy.getText())) {
			// The copy is already changed.
			return;
		}
		final AtomicBoolean changingFirst = new AtomicBoolean(false);
		final AtomicBoolean secondChanged = new AtomicBoolean(false);
		main.getDocument().addDocumentListener(

				new DocumentAdapter() {

					@Override
					protected void textChanged(DocumentEvent e) {
						if (secondChanged.get())
							return;
						while (!changingFirst.compareAndSet(false, true)) {
							// intentionally empty
						}

						copy.setText(String.format(format, main.getText()));

						changingFirst.set(false);
					}
				}
		);

		copy.getDocument().addDocumentListener(
				new DocumentAdapter() {
					@Override
					protected void textChanged(DocumentEvent e) {
						if (!changingFirst.get()) {
							secondChanged.set(true);
						}
					}
				}
		);
	}

	/**
	 * Make two fields change simultaneously until the second one ({@code copy}) changed manually.
	 * Maintains equality of {@code main.getText()} and {@code copy.getText()}
	 *
	 * Does nothing if this equality is wrong when method is called.
	 */
	public static void mirrorFields(JTextField main, JTextField copy) {
		mirrorFields(main, copy, "%s");
	}
}
