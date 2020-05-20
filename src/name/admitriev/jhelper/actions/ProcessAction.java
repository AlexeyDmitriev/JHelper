package name.admitriev.jhelper.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import name.admitriev.jhelper.exceptions.NotificationException;
import name.admitriev.jhelper.generation.FileUtils;
import name.admitriev.jhelper.generation.IncludesProcessor;

/**
 * @author egor@egork.net
 */
public class ProcessAction extends BaseAction {
    @Override
    protected void performAction(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            throw new NotificationException("No project found", "Are you in any project?");
        }
        FileEditorManager manager = FileEditorManager.getInstance(project);
        if (manager == null) {
            throw new NotificationException("This is unexpected", "File editor manager is null");
        }
        VirtualFile[] files = manager.getSelectedFiles();
        if (files.length == 0) {
            throw new NotificationException("No file found", "Do you have opened file?");
        }
        PsiFile file = PsiManager.getInstance(project).findFile(files[0]);
        if (file == null) {
            throw new NotificationException("This is unexpected", "No associated PsiFile");
        }
        if (!FileUtils.isCppFile(file)) {
            throw new NotificationException("Not a cpp file", "Only cpp files are currently supported");
        }
        String result = IncludesProcessor.process(file);
        FileUtils.writeToFile(file, result);
    }
}
