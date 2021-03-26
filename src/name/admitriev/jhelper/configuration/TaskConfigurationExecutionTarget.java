package name.admitriev.jhelper.configuration;

import com.intellij.execution.ExecutionTarget;
import com.intellij.execution.configurations.RunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class TaskConfigurationExecutionTarget extends ExecutionTarget {
    private final ExecutionTarget originalTarget;

    TaskConfigurationExecutionTarget(ExecutionTarget originalTarget) {
        this.originalTarget = originalTarget;
    }

    @Override
    public @NotNull String getId() {
        return "name.admitriev.jhelper.configuration.TaskConfigurationExecutionTarget" + originalTarget.getId();
    }

    @Override
    public @NotNull String getDisplayName() {
        return originalTarget.getDisplayName();
    }

    @Override
    public @Nullable Icon getIcon() {
        return originalTarget.getIcon();
    }

    @Override
    public boolean canRun(@NotNull RunConfiguration runConfiguration) {
        return runConfiguration instanceof TaskConfiguration;
    }

    public ExecutionTarget getOriginalTarget() {
        return originalTarget;
    }
}
