package name.admitriev.jhelper.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.PathChooserDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import net.egork.chelper.util.FileUtilities;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class FileSelector extends JPanel {
	private final JTextField textField;
	private JButton button;

	public FileSelector(final Project project, String initialValue) {
		super(new BorderLayout());
		textField = new JTextField(initialValue);
		button = new JButton("...") {
			@Override
			public Dimension getPreferredSize() {
				Dimension dimension = super.getPreferredSize();
				//Make it square
				//noinspection SuspiciousNameCombination
				dimension.width = dimension.height;
				return dimension;
			}
		};
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PathChooserDialog dialog = FileChooserFactory.getInstance().createPathChooser(new FileChooserDescriptor(true, false, false, false, false, false) {
					@Override
					public boolean isFileSelectable(VirtualFile file) {
						return super.isFileSelectable(file) && FileUtilities.isChild(project.getBaseDir(), file);
					}

					@Override
					public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
						return super.isFileVisible(file, showHiddenFiles) && (FileUtilities.isChild(project.getBaseDir(), file) || FileUtilities.isChild(file, project.getBaseDir()));
					}
				}, project, FileSelector.this);
				VirtualFile toSelect = project.getBaseDir().findFileByRelativePath(textField.getText());
				if (toSelect == null)
					toSelect = project.getBaseDir();
				dialog.choose(toSelect, new Consumer<List<VirtualFile>>() {
					@Override
					@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
					public void consume(List<VirtualFile> files) {
						if (files.size() == 1) {
							String path = FileUtilities.getRelativePath(project.getBaseDir(), files.get(0));
							if (path != null)
								textField.setText(path);
						}
					}
				});
			}
		});
		add(textField, BorderLayout.CENTER);

		add(button, BorderLayout.EAST);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		textField.setEnabled(enabled);
		button.setEnabled(enabled);
	}

	public String getText() {
		return textField.getText();
	}
}
