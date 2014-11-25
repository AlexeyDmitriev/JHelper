package name.admitriev.jhelper.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import name.admitriev.jhelper.components.Configurator;
import name.admitriev.jhelper.exceptions.NotificationException;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class CopySourceAction extends BaseAction {
	@Override
	protected void performAction(AnActionEvent e) {
		Project project = e.getProject();
		if (project == null)
			throw new NotificationException("No project found", "Are you in any project?");

		Configurator configurator = project.getComponent(Configurator.class);
		Configurator.State configuration = configurator.getState();

		VirtualFile file = project.getBaseDir().findFileByRelativePath(configuration.getOutputFile());
		if (file == null)
			throw new NotificationException("Couldn't find output file");
		Document document = FileDocumentManager.getInstance().getDocument(file);
		if (document == null)
			throw new NotificationException("Couldn't open output file");
		StringSelection selection = new StringSelection(document.getText());
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
	}
}
