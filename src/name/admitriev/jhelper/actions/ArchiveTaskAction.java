package name.admitriev.jhelper.actions;

import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.*;
import name.admitriev.jhelper.components.Configurator;
import name.admitriev.jhelper.configuration.TaskConfiguration;
import name.admitriev.jhelper.exceptions.NotificationException;
import name.admitriev.jhelper.ui.Notificator;
import net.egork.chelper.util.FileUtilities;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

public class ArchiveTaskAction extends BaseAction {
    @Override
    public void performAction(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            throw new NotificationException("No project found", "Are you in any project?");
        }
        VirtualFile file = e.getDataContext().getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null) {
            throw new NotificationException("No task or directory with tasks selected", "To archive a solution or a directory you should select it first");
        }
        VirtualFile projectBaseDir = ProjectUtil.guessProjectDir(project);
        if (projectBaseDir == null) {
            throw new NotificationException("Unable to find base directory of project", "Make sure you are not in default project");
        }
        Configurator configurator = project.getService(Configurator.class);
        String archiveDirectory = configurator.getState().getArchiveDirectory();
        ApplicationManager.getApplication().runWriteAction(new RecursiveArchiver(project, projectBaseDir, archiveDirectory, file));
    }

    private static final class RecursiveArchiver implements Runnable {
        final Project project;
        final VirtualFile projectBaseDir;
        final String archiveDirectory;
        final VirtualFile selectedFile;
        final ArrayList<String> pathToFile;
        final String curDate;
        int archivedCount = 0;

        public RecursiveArchiver(@NotNull Project project, @NotNull VirtualFile projectBaseDir, @NotNull String archiveDirectoryRelativePath, @NotNull VirtualFile selectedFile) {
            this.project = project;
            this.projectBaseDir = projectBaseDir;
            this.archiveDirectory = archiveDirectoryRelativePath;
            this.selectedFile = selectedFile;
            this.pathToFile = new ArrayList<>();
            this.curDate = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH:mm:ss").format(LocalDateTime.now());
        }

        @Override
        public void run() {
            try {
                VfsUtil.createDirectoryIfMissing(archiveDirectory);
            } catch (IOException ex) {
                throw new NotificationException("Unable to create archive directory", "Root of archive directory does not exist and failed to be created");
            }
            VfsUtilCore.visitChildrenRecursively(selectedFile, new VirtualFileVisitor<>() {
                @Override
                public boolean visitFile(@NotNull VirtualFile file) {
                    if (file.isDirectory()) {
                        pathToFile.add(file.getName());
                        return true;
                    }
                    handleSourceFile(file);
                    return false;
                }

                @Override
                public void afterChildrenVisited(@NotNull VirtualFile file) {
                    pathToFile.remove(pathToFile.size() - 1);
                }
            });
            if (archivedCount > 0) {
                LocalFileSystem.getInstance().refresh(true); // to notify Vfs about new files created in archive
                Notificator.showNotification(
                        "All tasks in " + selectedFile.getName() + " were successfully archived",
                        NotificationType.INFORMATION
                );
                DeleteTaskAction.selectSomeTaskConfiguration(RunManagerEx.getInstanceEx(project));
            }
        }

        private void handleSourceFile(@NotNull VirtualFile file) {
            RunnerAndConfigurationSettings taskConfigurationAndSettings = findJHelperRCForFile(file, project);
            if (taskConfigurationAndSettings == null) {
                return;
            }
            TaskConfiguration taskConfiguration = ((TaskConfiguration) taskConfigurationAndSettings.getConfiguration());
            VirtualFile directory = getFinalArchiveDirectoryOfFile(file);
            saveCodeToArchive(file, directory, taskConfiguration.getClassName() + "_" + curDate + ".cpp");
            saveRCToArchive(taskConfiguration, directory, taskConfiguration.getClassName() + "_" + curDate + ".xml");
            deleteFileWithCode(file);
            deleteRC(taskConfigurationAndSettings);
            ++archivedCount;
        }

        private void deleteRC(RunnerAndConfigurationSettings taskConfigurationAndSettings) {
            RunManagerEx runManager = RunManagerEx.getInstanceEx(project);
            runManager.removeConfiguration(taskConfigurationAndSettings);
        }

        private void deleteFileWithCode(@NotNull VirtualFile file) {
            try {
                file.delete(this);
            } catch (IOException e) {
                throw new NotificationException("Archiving of the task failed", "Unable to delete sources of the task, caused by " + e.getMessage());
            }
        }

        private void saveCodeToArchive(@NotNull VirtualFile fileWithCode, VirtualFile directoryInArchiveForTheTask, String archiveTaskFileName) {
            try {
                VfsUtil.copyFile(this, fileWithCode, directoryInArchiveForTheTask, archiveTaskFileName);
            } catch (IOException e) {
                throw new NotificationException("Archiving of the task failed", "Archiving of the code of the solution failed, caused by " + e.getMessage());
            }
        }

        private void saveRCToArchive(TaskConfiguration taskConfiguration, VirtualFile directoryInArchiveForTheTask, String archiveRCFileName) {
            try {
                String rcArchiveFqn = directoryInArchiveForTheTask.getPath() + "/" + archiveRCFileName;
                Element rc = new Element("RC");
                Document doc = new Document(rc);
                taskConfiguration.writeExternal(rc);
                XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
                xmlOutputter.output(doc, new FileOutputStream(rcArchiveFqn));
            } catch (IOException e) {
                throw new NotificationException("Archiving of the task failed", "Unable to archive the task, caused by " + e.getMessage());
            }
        }

        @NotNull
        private VirtualFile getFinalArchiveDirectoryOfFile(@NotNull VirtualFile file) {
            String relativePathToParentInArchive;
            if (pathToFile.isEmpty()) {
                relativePathToParentInArchive = archiveDirectory + "/" + file.getParent().getName();
            } else {
                relativePathToParentInArchive = archiveDirectory + "/" + String.join("/", pathToFile);
            }
            VirtualFile parent;
            try {
                parent = VfsUtil.createDirectoryIfMissing(Paths.get(projectBaseDir.getPath(), relativePathToParentInArchive).toString());
            } catch (IOException e) {
                throw new NotificationException("Archiving of the task failed", "Unable to create directory in archive for the task, cased by " + e.getMessage());
            }
            return parent == null ? Objects.requireNonNull(projectBaseDir.findFileByRelativePath(relativePathToParentInArchive)) : parent;
        }
    }

    private static @Nullable RunnerAndConfigurationSettings findJHelperRCForFile(@NotNull VirtualFile file, @NotNull Project project) {
        RunManagerImpl runManager = RunManagerImpl.getInstanceImpl(project);
        for (RunnerAndConfigurationSettings configuration : runManager.getAllSettings()) {
            RunConfiguration rc = configuration.getConfiguration();
            if (rc instanceof TaskConfiguration) {
                TaskConfiguration task = (TaskConfiguration) rc;
                String pathToClassFile = task.getCppPath();
                VirtualFile expectedFie = FileUtilities.getFile(project, pathToClassFile);
                if (file.equals(expectedFie)) {
                    return configuration;
                }
            }
        }
        return null;
    }
}
