package name.admitriev.jhelper.task

import com.intellij.openapi.project.Project
import name.admitriev.jhelper.components.Configurator
import net.egork.chelper.task.StreamConfiguration
import net.egork.chelper.task.Test
import net.egork.chelper.task.TestType

class TaskData(
    val name: String,
    val className: String,
    val cppPath: String,
    val input: StreamConfiguration,
    val output: StreamConfiguration,
    val testType: TestType,
    tests: Array<Test?>
) {
    private val tests: Array<Test?>

    init {
        this.tests = tests.copyOf(tests.size)
    }

    fun getTests(): Array<Test?> {
        return tests.copyOf(tests.size)
    }

    companion object {
        fun emptyTaskData(project: Project): TaskData {
            return TaskData(
                "",
                "", String.format(defaultCppPathFormat(project), ""),
                StreamConfiguration.STANDARD,
                StreamConfiguration.STANDARD,
                TestType.SINGLE, arrayOfNulls(0)
            )
        }

        fun defaultCppPathFormat(project: Project): String {
            val configurator = project.getComponent(Configurator::class.java)
            return "${configurator.state.tasksDirectory}/%s.cpp"
        }
    }
}