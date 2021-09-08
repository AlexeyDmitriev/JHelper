package name.admitriev.jhelper.actions;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiManager;
import com.jetbrains.cidr.lang.psi.OCFile;
import name.admitriev.jhelper.IDEUtils;
import name.admitriev.jhelper.configuration.TaskConfiguration;
import name.admitriev.jhelper.configuration.TaskConfigurationType;
import name.admitriev.jhelper.exceptions.NotificationException;
import name.admitriev.jhelper.ui.UIUtils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;

public class UnarchiveTaskAction extends BaseAction {
    @Override
    protected void performAction(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            throw new NotificationException("No project found", "Are you in any project?");
        }
        VirtualFile file = e.getDataContext().getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null) {
            throw new NotificationException("No task or directory with tasks selected", "To unarchive a solution or a directory you should select it first");
        }
        VirtualFile projectBaseDir = ProjectUtil.guessProjectDir(project);
        if (projectBaseDir == null) {
            throw new NotificationException("Unable to find base directory of project", "If you are in default project then switch to the normal one");
        }
        ApplicationManager.getApplication().runWriteAction(new RecursiveUnarchiver(project, projectBaseDir, file));
    }

    private static final class RecursiveUnarchiver implements Runnable {
        final Project project;
        final VirtualFile projectBaseDir;
        final VirtualFile selectedFile;
        VirtualFile lastGeneratedFile;

        public RecursiveUnarchiver(@NotNull Project project, @NotNull VirtualFile projectBaseDirPath, @NotNull VirtualFile selectedFile) {
            this.project = project;
            this.projectBaseDir = projectBaseDirPath;
            this.selectedFile = selectedFile;
        }

        @Override
        public void run() {
            VfsUtilCore.visitChildrenRecursively(selectedFile, new VirtualFileVisitor<>() {
                @Override
                public boolean visitFile(@NotNull VirtualFile file) {
                    if (file.isDirectory()) {
                        return true;
                    }
                    handleSourceFile(file);
                    return false;
                }
            });
            UIUtils.openMethodInEditor(project, (OCFile) Objects.requireNonNull(PsiManager.getInstance(project).findFile(lastGeneratedFile)), "solve");
            IDEUtils.reloadProject(project);
        }

        private void handleSourceFile(@NotNull VirtualFile file) {
            Pair<VirtualFile, VirtualFile> xmlAndCppToUnarchive = getXmlAndCppToUnarchive(file);
            if (xmlAndCppToUnarchive == null) {
                return;
            }
            VirtualFile xmlRC = xmlAndCppToUnarchive.first;
            VirtualFile cppFile = xmlAndCppToUnarchive.second;
            RunnerAndConfigurationSettings configuration = restoreTaskSettings(file, xmlRC);
            TaskConfiguration taskConfiguration = ((TaskConfiguration) configuration.getConfiguration());

            VirtualFile directory = getUnarchivedDirectoryOfTask(taskConfiguration.getCppPath());
            try {
                lastGeneratedFile = VfsUtil.copyFile(this, cppFile, directory, Paths.get(taskConfiguration.getCppPath()).getFileName().toString());
            } catch (IOException e) {
                throw new NotificationException("Restoring of task " + file.getNameWithoutExtension() + " failed", "Unable to restore source of the solution, caused by " + e.getMessage());
            }
            configuration.storeInDotIdeaFolder();
            RunManager manager = RunManager.getInstance(project);
            manager.addConfiguration(configuration);

            try {
                xmlRC.delete(this);
                cppFile.delete(this);
            } catch (IOException e) {
                throw new NotificationException("Restoring of task " + file.getNameWithoutExtension() + " failed",
                        "Unable to delete archived rc and solution, caused by " + e.getMessage());
            }
        }

        private RunnerAndConfigurationSettings restoreTaskSettings(@NotNull VirtualFile file, VirtualFile xmlRC) {
            TaskConfigurationType taskConfigurationType = new TaskConfigurationType();
            TaskConfiguration taskConfiguration = ((TaskConfiguration) taskConfigurationType.createTemplateConfiguration(project));
            SAXBuilder saxBuilder = new SAXBuilder();
            File inputFile = new File(xmlRC.getPath());
            Document document;
            try {
                document = saxBuilder.build(inputFile);
            } catch (JDOMException | IOException e) {
                throw new NotificationException("Restoring of task " + file.getNameWithoutExtension() + " failed", "Unable to restore RC, caused by " + e.getMessage());
            }
            RunManager manager = RunManager.getInstance(project);
            taskConfiguration.readExternal(document.getRootElement());
            ConfigurationFactory factory = taskConfigurationType.getConfigurationFactories()[0];
            return manager.createConfiguration(taskConfiguration, factory);
        }

        private VirtualFile getUnarchivedDirectoryOfTask(String relativeCppPath) {
            VirtualFile directory;
            try {
                directory = VfsUtil.createDirectoryIfMissing(Paths.get(projectBaseDir.getPath(), relativeCppPath).getParent().toString());
            } catch (IOException e) {
                throw new NotificationException("Unarchive of the task failed", "Unable to create directory for the task, cased by " + e.getMessage());
            }
            return directory == null ? Objects.requireNonNull(projectBaseDir.findFileByRelativePath(Paths.get(relativeCppPath).getParent().toString())) : directory;
        }

        /* file can be either xml with RC or cpp with solution */
        private static @Nullable Pair<VirtualFile, VirtualFile> getXmlAndCppToUnarchive(@NotNull VirtualFile file) {
            String prefix = file.getNameWithoutExtension();
            VirtualFile xml = null;
            VirtualFile cpp = null;
            for (VirtualFile otherFile : file.getParent().getChildren()) {
                if (otherFile.getNameWithoutExtension().equals(prefix)) {
                    String extension = otherFile.getExtension();
                    if (extension != null && extension.equals("cpp")) {
                        cpp = otherFile;
                    }
                    if (extension != null && extension.equals("xml")) {
                        xml = otherFile;
                    }
                }
            }
            if (xml == null || cpp == null) {
                return null;
            }
            return new Pair<>(xml, cpp);
        }
    }
}
