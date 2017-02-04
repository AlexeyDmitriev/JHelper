package name.admitriev.jhelper;

import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace;

public class IDEUtils {
	private IDEUtils() {
	}


	public static void reloadProject(Project project) {
		CMakeWorkspace.getInstance(project).scheduleReload(true);
	}

}
