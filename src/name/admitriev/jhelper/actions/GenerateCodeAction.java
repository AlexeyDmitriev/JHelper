package name.admitriev.jhelper.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import name.admitriev.jhelper.Util;
import name.admitriev.jhelper.components.Configurator;
import name.admitriev.jhelper.exception.NotificationException;
import name.admitriev.jhelper.generation.IncludesProcessor;
import name.admitriev.jhelper.generation.UnusedCodeRemover;


public class GenerateCodeAction extends BaseAction {
	@Override
	public void performAction(AnActionEvent e) {
		PsiFile file = e.getData(CommonDataKeys.PSI_FILE);

		if(file == null) {
			throw new NotificationException("File not found", "Do you editing any file?");
		}

		if(!Util.isCppFile(file)) {
			throw new NotificationException("Not a cpp file", "Only cpp files are currently supported");
		}

		Project project = e.getProject();
		if(project == null) {
			throw new NotificationException("No project found", "Are you in any project?");
		}


		Configurator configurator = project.getComponent(Configurator.class);
		Configurator.State configuration = configurator.getState();

		VirtualFile outputFile = findFileInProject(project, configuration.getOutputFile());
		if(outputFile == null) {
			throw new NotificationException("No output file found.", "You should configure output file to point to existing file");
		}
		String result = IncludesProcessor.process(file);
		PsiFile psiOutputFile = PsiManager.getInstance(project).findFile(outputFile);
		if(psiOutputFile == null) {
			throw new NotificationException("Can't open output file as PSI");
		}

		writeToFile(psiOutputFile, authorComment(project), result);

		UnusedCodeRemover.remove(psiOutputFile);
	}

	private static void writeToFile(PsiFile outputFile, final String... strings) {
		final Project project = outputFile.getProject();
		final Document document = PsiDocumentManager.getInstance(project).getDocument(outputFile);
		if(document == null) {
			throw new NotificationException("Can't open output file as document");
		}

		new WriteCommandAction.Simple<Object>(outputFile.getProject(), outputFile) {
			@Override
			public void run() {
				document.deleteString(0, document.getTextLength());
				for (String string : strings) {
					document.insertString(document.getTextLength() ,string);
				}
				FileDocumentManager.getInstance().saveDocument(document);
				PsiDocumentManager.getInstance(project).commitDocument(document);
			}
		}.execute();
	}

	private static String authorComment(Project project) {
		Configurator configurator = project.getComponent(Configurator.class);
		Configurator.State configuration = configurator.getState();

		StringBuilder sb = new StringBuilder();
		sb.append("/**\n");
		sb.append(" * code generated by JHelper\n");
		sb.append(" * @author ").append(configuration.getAuthor()).append('\n');
		sb.append(" */\n\n");
		return sb.toString();
	}

	private static VirtualFile findFileInProject(Project project, String path) {
		VirtualFile projectDirectory = project.getBaseDir();
		return projectDirectory.findFileByRelativePath(path);
	}
}
