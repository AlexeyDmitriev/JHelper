package name.admitriev.jhelper;

import com.intellij.openapi.project.Project;
import name.admitriev.jhelper.actions.AddTaskAction;
import name.admitriev.jhelper.exceptions.JHelperException;

import java.lang.reflect.InvocationTargetException;

public class IDEUtils {
	private IDEUtils() {
	}


	public static void reloadProjectInCLion(Project project) {
		String errorMessage = "Couldn't reload a CLion project. API changed?";
		try {
			Class<?> clz = AddTaskAction.class.getClassLoader().loadClass("com.jetbrains.cidr.cpp.cmake.CMakeWorkspace");
			Object instance = clz.getMethod("getInstance", Project.class).invoke(null, project);
			clz.getMethod("scheduleReload", boolean.class).invoke(instance, true);
		}
		catch (ClassNotFoundException ignored) {
			// Probably not a CLion, ignore
		}
		catch (InvocationTargetException e) {
			throw new JHelperException(errorMessage, e);
		}
		catch (NoSuchMethodException e) {
			throw new JHelperException(errorMessage, e);
		}
		catch (IllegalAccessException e) {
			throw new JHelperException(errorMessage, e);
		}
	}

}
