package name.admitriev.jhelper.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import name.admitriev.jhelper.generation.FileUtils;

public class RelativeFileChooserDescriptor extends FileChooserDescriptor {
	private String basePath;

	private RelativeFileChooserDescriptor(
			VirtualFile baseDir,
			boolean chooseFiles,
			boolean chooseFolders
	) {
		super(chooseFiles, chooseFolders, false, false, false, false);
		basePath = baseDir.getPath();
		withShowHiddenFiles(true);

		setRoots(baseDir);
	}

	@Override
	public boolean isFileSelectable(VirtualFile file) {
		return super.isFileSelectable(file) && FileUtils.isChild(
				basePath,
				file.getPath()
		);
	}

	@Override
	public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
		return super.isFileVisible(file, showHiddenFiles) && (
				FileUtils.isChild(basePath, file.getPath()) || FileUtils.isChild(file.getPath(), basePath)
		);
	}

	public static RelativeFileChooserDescriptor fileChooser(VirtualFile baseDir) {
		return new RelativeFileChooserDescriptor(baseDir, true, false);
	}

	public static RelativeFileChooserDescriptor directoryChooser(VirtualFile baseDir) {
		return new RelativeFileChooserDescriptor(baseDir, false, true);
	}
}
