package name.admitriev.jhelper.ui;

import com.intellij.ui.DocumentAdapter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.util.concurrent.atomic.AtomicBoolean;

public class UIUtils {
	private UIUtils() {
	}

	/**
	 * Lock fields so that when the main of them changed, the copy is changed accordingly.
	 * Copy always has format {@code String.format(format, main.getText())} until a copy is changed.
	 * After change in a copy, this function does nothing.
	 *
	 * @param format format for {@link java.lang.String#format}. Should contain exactly one format specifier equal to %s
	 */
	public static void lockFields(final JTextField main, final JTextField copy, final String format) {
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
	 * Lock fields so that when the main of them changed, the copy is changed accordingly.
	 * Copy always equals to main until the copy is changed.
	 * After change in a copy, this function does nothing.
	 */
	public static void lockFields(JTextField main, JTextField copy) {
		lockFields(main, copy, "%s");
	}
}
