package name.admitriev.jhelper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import name.admitriev.jhelper.components.Configurator;
import name.admitriev.jhelper.configuration.TaskConfiguration;
import org.jdesktop.swingx.VerticalLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;

public class ArchiveDialog extends DialogWrapper {
    private final JComponent component;

    private JTextField archiveFileName;
    private FileSelector archiveDirectory;
    private File baseFile;

    public ArchiveDialog(@NotNull Project project, Configurator.State configuration, TaskConfiguration runConfiguration) {
        super(project);
        setTitle("Archive Task");
        baseFile = new File(project.getBasePath());
        archiveDirectory = new FileSelector(
            project,
            configuration.getArchiveDirectory(),
            RelativeFileChooserDescriptor.directoryChooser(project.getBaseDir())
        );
        archiveFileName = new JTextField(runConfiguration.getName());

        JPanel panel = new JPanel(new VerticalLayout());
        panel.add(LabeledComponent.create(archiveDirectory, "Archive Directory"));
        panel.add(LabeledComponent.create(archiveFileName, "Archive file"));

        component = panel;
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return component;
    }

    public String getArchiveFile() {
        File file = new File(baseFile, archiveDirectory.getText());
        String fileName = archiveFileName.getText();
        if (!fileName.contains(".cpp")) {
            fileName += ".cpp";
        }
        file = new File(file, fileName);
        file.getParentFile().mkdirs();
        return file.getAbsolutePath();
    }
}
