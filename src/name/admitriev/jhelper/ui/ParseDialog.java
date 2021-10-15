package name.admitriev.jhelper.ui;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import name.admitriev.jhelper.components.Configurator;
import name.admitriev.jhelper.parsing.Receiver;
import name.admitriev.jhelper.task.TaskData;
import net.egork.chelper.parser.Description;
import net.egork.chelper.parser.Parser;
import net.egork.chelper.parser.ParserTask;
import net.egork.chelper.task.Task;
import net.egork.chelper.task.TestType;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ParseDialog extends DialogWrapper {
	private final JComponent component;

	private final ComboBox<Parser> parserComboBox;
	private final ComboBox<TestType> testType;

	private final JBList<Description> contestList;
	private final ParseListModel<Description> contestModel = new ParseListModel<>();
	private final JBList<Description> problemList;
	private final ParseListModel<Description> problemModel = new ParseListModel<>();
	private final Project project;
	private Receiver contestReceiver = new Receiver.Empty();
	private Receiver problemReceiver = new Receiver.Empty();

	public ParseDialog(@Nullable Project project) {
		super(project);
		this.project = project;
		setTitle("Parse Contest");
		JPanel panel = new JPanel(new VerticalLayout());

		parserComboBox = new ComboBox<>(Parser.PARSERS);
		parserComboBox.setRenderer(
			new SimpleListCellRenderer<>() {
				@Override
				public void customize(@NotNull JList list, Parser parser, int index, boolean selected, boolean hasFocus) {
					setText(parser.getName());
					setIcon(parser.getIcon());
				}
			}
		);
		parserComboBox.addActionListener(e -> refresh());

		testType = new ComboBox<>(TestType.values());

		contestList = new JBList<>(contestModel);
		contestList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		contestList.addListSelectionListener(
			e -> {
				problemReceiver.stop();
				problemModel.removeAll();

				Parser parser = (Parser) parserComboBox.getSelectedItem();
				Description contest = contestList.getSelectedValue();

				problemReceiver = generateProblemReceiver();

				if (contest != null) {
					new ParserTask(contest.id, problemReceiver, parser);
				}
			}
		);

		problemList = new JBList<>(problemModel);
		problemList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);


		JPanel contestsTasksPanel = new JPanel(new HorizontalLayout());
		contestsTasksPanel.add(
			new JBScrollPane(
				contestList,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
			)
		);
		contestsTasksPanel.add(
			new JBScrollPane(
				problemList,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
			)
		);

		panel.add(LabeledComponent.create(parserComboBox, "Parser"));
		panel.add(contestsTasksPanel);
		panel.add(LabeledComponent.create(testType, "Test type"));

		component = panel;

		init();
	}

	private void refresh() {
		Parser parser = (Parser) parserComboBox.getSelectedItem();
		Description chosenDescription = contestList.getSelectedValue();
		contestReceiver.stop();
		contestModel.removeAll();

		contestReceiver = generateContestReceiver(chosenDescription);

		new ParserTask(null, contestReceiver, parser);
	}

	private Receiver generateContestReceiver(Description chosenDescription) {
		return new Receiver() {
			@Override
			public void receiveDescriptions(Collection<Description> descriptions) {
				Receiver thisReceiver = this;
				SwingUtilities.invokeLater(
					() -> {
						//noinspection ObjectEquality
						if (contestReceiver != thisReceiver) {
							return;
						}
						boolean shouldMark = contestModel.getSize() == 0;
						contestModel.addAll(descriptions);
						if (shouldMark) {
							for (Description contest : descriptions) {
								if (chosenDescription != null && chosenDescription.id.equals(contest.id)) {
									contestList.setSelectedValue(contest, true);
									return;
								}
							}
							if (contestModel.getSize() > 0) {
								contestList.setSelectedIndex(0);
							}
						}
					}
				);
			}
		};
	}

	private Receiver generateProblemReceiver() {
		return new Receiver() {
			@Override
			public void receiveDescriptions(Collection<Description> descriptions) {
				Receiver thisReceiver = this;
				SwingUtilities.invokeLater(
					() -> {
						//noinspection ObjectEquality
						if (problemReceiver != thisReceiver) {
							return;
						}
						boolean shouldMark = problemModel.getSize() == 0;
						problemModel.addAll(descriptions);
						if (shouldMark) {
							problemList.setSelectionInterval(0, problemModel.getSize() - 1);
						}
					}
				);
			}
		};
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		refresh();
		return component;
	}

	public Collection<TaskData> getResult() {
		List<TaskData> list = new ArrayList<>();
		List<Description> selectedTasks = problemList.getSelectedValuesList();
		Parser parser = (Parser) parserComboBox.getSelectedItem();

		Configurator configurator = project.getComponent(Configurator.class);
		Configurator.State configuration = configurator.getState();

		String path = configuration.getTasksDirectory();

		for (Description taskDescription : selectedTasks) {
			assert parser != null;
			Task rawTask = parser.parseTask(taskDescription);
			if (rawTask == null) {
				Notificator.showNotification(
					"Unable to parse task " + taskDescription.description,
					"Connection problems or format change",
					NotificationType.ERROR
				);
				continue;
			}
			TaskData myTask = new TaskData(
				rawTask.name,
				rawTask.taskClass,
				String.format("%s/%s.cpp", path, rawTask.taskClass),
				rawTask.input,
				rawTask.output,
				(TestType) testType.getSelectedItem(),
				rawTask.tests
			);
			list.add(myTask);
		}
		return list;
	}
}


