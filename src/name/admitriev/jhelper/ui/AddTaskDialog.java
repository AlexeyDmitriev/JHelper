package name.admitriev.jhelper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import name.admitriev.jhelper.task.TaskData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AddTaskDialog extends DialogWrapper {
    private TaskSettingsComponent component;

    public AddTaskDialog(@NotNull Project project) {
        super(project);
        setTitle("Add Task");
        component = new TaskSettingsComponent(
            project,
            true,
            this::pack
        );
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return component;
    }

    public TaskData getTask() {
        return component.getTask();
    }
}
