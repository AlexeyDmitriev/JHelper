package name.admitriev.jhelper.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import name.admitriev.jhelper.generation.FileUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class FileSelector extends TextFieldWithBrowseButton.NoPathCompletion {

    public FileSelector(Project project, String initialValue, FileChooserDescriptor descriptor) {
        super(new JTextField(initialValue));
        addBrowseFolderListener(
            new RelativePathBrowseListener(descriptor, project)
        );
        installPathCompletion(descriptor);
    }

    private static class RelativePathBrowseListener extends TextBrowseFolderListener {
        private final String basePath;

        private RelativePathBrowseListener(FileChooserDescriptor descriptor, Project project) {
            super(descriptor, project);
            basePath = project.getBasePath();
        }

        @NotNull
        @Override
        protected String chosenFileToResultingText(@NotNull VirtualFile chosenFile) {
            return FileUtils.relativePath(basePath, chosenFile.getPath());
        }

        @NotNull
        @Override
        protected String expandPath(@NotNull String path) {
            return basePath + '/' + path;
        }
    }
}
