package name.admitriev.jhelper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import net.egork.chelper.task.Test;
import net.egork.chelper.ui.VariableGridLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditTestsDialog extends DialogWrapper {
	private static final int HEIGHT = new JLabel("Test").getPreferredSize().height;
	private static final double LIST_PANEL_FRACTION = 0.35;
	private static final Dimension PREFERRED_SIZE = new Dimension(600, 400);

	private List<Test> tests;

	private int currentTest;
	private JBList testList;
	private JTextArea input;
	private JTextArea output;
	private List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
	private JPanel checkBoxesPanel;
	private JCheckBox knowAnswer;
	private JPanel outputPanel;
	private boolean updating = false;

	private JComponent component;

	public EditTestsDialog(Test[] tests, Project project) {
		super(project);
		setTitle("Tests");
		setResizable(false);
		this.tests = new ArrayList<Test>(Arrays.asList(tests));
		VariableGridLayout mainLayout = new VariableGridLayout(1, 2, 5, 5);
		mainLayout.setColFraction(0, LIST_PANEL_FRACTION);
		JPanel mainPanel = new JPanel(mainLayout);
		JPanel selectorAndButtonsPanel = new JPanel(new BorderLayout());

		selectorAndButtonsPanel.add(
				LabeledComponent.create(
						new JBScrollPane(
								generateListPanel(),
								ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
								ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
						),
						"Tests"
				)
		);

		selectorAndButtonsPanel.add(createButtonPanel(), BorderLayout.PAGE_END);
		mainPanel.add(selectorAndButtonsPanel);
		mainPanel.add(generateTestPanel());

		mainPanel.setPreferredSize(PREFERRED_SIZE);
		component = mainPanel;
		setSelectedTest(Math.min(0, tests.length - 1));

		init();
	}

	private JPanel generateListPanel() {
		JPanel checkBoxesAndSelectorPanel = new JPanel(new BorderLayout());
		checkBoxesPanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, false, false));
		for (Test test : tests) {
			JCheckBox checkBox = createCheckBox(test);
			checkBoxesPanel.add(checkBox);
		}
		checkBoxesAndSelectorPanel.add(checkBoxesPanel, BorderLayout.WEST);
		testList = new JBList(tests);
		testList.setFixedCellHeight(HEIGHT);
		testList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		testList.setLayoutOrientation(JList.VERTICAL);
		testList.addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						if (updating) {
							return;
						}
						int index = testList.getSelectedIndex();
						if (index >= 0 && index < testList.getItemsCount()) {
							saveCurrentTest();
							setSelectedTest(index);
						}
					}
				}
		);
		checkBoxesAndSelectorPanel.add(testList, BorderLayout.CENTER);
		return checkBoxesAndSelectorPanel;
	}

	private JPanel generateTestPanel() {
		JPanel testPanel = new JPanel(new GridLayout(2, 1, 5, 5));

		input = generateSavingTextArea();
		JPanel inputPanel = LabeledComponent.create(new JBScrollPane(input), "Input");

		output = generateSavingTextArea();
		outputPanel = LabeledComponent.create(new JBScrollPane(output), "Output");

		knowAnswer = new JCheckBox("Know answer?");
		knowAnswer.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						saveCurrentTest();
					}
				}
		);
		JPanel outputAndCheckBoxPanel = new JPanel(new BorderLayout());
		outputAndCheckBoxPanel.add(knowAnswer, BorderLayout.NORTH);
		outputAndCheckBoxPanel.add(outputPanel, BorderLayout.CENTER);
		testPanel.add(inputPanel);
		testPanel.add(outputAndCheckBoxPanel);
		return testPanel;
	}

	private JTextArea generateSavingTextArea() {
		JTextArea result = new JTextArea();
		result.setFont(Font.decode(Font.MONOSPACED));
		result.getDocument().addDocumentListener(
				new DocumentAdapter() {
					@Override
					protected void textChanged(DocumentEvent e) {
						saveCurrentTest();
					}
				}
		);
		return result;
	}

	private JPanel createButtonPanel() {
		JPanel buttonsPanel = new JPanel(new GridLayout(3, 1));
		JPanel upperButtonsPanel = new JPanel(new GridLayout(1, 2));
		JButton all = new JButton("All");
		all.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int index = 0;
						for (JCheckBox checkBox : checkBoxes) {
							checkBox.setSelected(true);
							tests.set(
									index,
									tests.get(index).setActive(true)
							);
							index++;
						}
						setSelectedTest(currentTest);
					}
				}
		);
		upperButtonsPanel.add(all);
		JButton none = new JButton("None");
		none.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int index = 0;
						for (JCheckBox checkBox : checkBoxes) {
							checkBox.setSelected(false);
							tests.set(
									index,
									tests.get(index).setActive(false)
							);
							index++;
						}
						setSelectedTest(currentTest);
					}
				}
		);
		upperButtonsPanel.add(none);
		buttonsPanel.add(upperButtonsPanel);
		JPanel middleButtonsPanel = new JPanel(new GridLayout(1, 2));
		JButton newTest = new JButton("New");
		newTest.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						saveCurrentTest();
						int index = tests.size();
						Test test = new Test("", "", index);
						tests.add(test);
						checkBoxesPanel.add(createCheckBox(test));
						setSelectedTest(index);
					}
				}
		);
		middleButtonsPanel.add(newTest);
		JButton remove = new JButton("Remove");
		remove.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (currentTest == -1) {
							return;
						}
						while (checkBoxes.size() > currentTest) {
							checkBoxesPanel.remove(checkBoxes.get(currentTest));
							checkBoxes.remove(currentTest);
						}
						tests.remove(currentTest);
						int size = tests.size();
						for (int i = currentTest; i < size; i++) {
							Test test = tests.get(i);
							test = new Test(test.input, test.output, i, test.active);
							tests.set(i, test);
							checkBoxesPanel.add(createCheckBox(test));
						}
						if (currentTest < size) {
							setSelectedTest(currentTest);
							return;
						}
						if (size > 0) {
							setSelectedTest(0);
							return;
						}
						setSelectedTest(-1);
					}
				}
		);
		middleButtonsPanel.add(remove);
		buttonsPanel.add(middleButtonsPanel);
		return buttonsPanel;
	}

	private JCheckBox createCheckBox(final Test test) {
		final JCheckBox checkBox = new JCheckBox("", test.active);
		Dimension preferredSize = new Dimension(checkBox.getPreferredSize().width, HEIGHT);
		checkBox.setPreferredSize(preferredSize);
		checkBox.setMaximumSize(preferredSize);
		checkBox.setMinimumSize(preferredSize);
		checkBox.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						tests.set(test.index, tests.get(test.index).setActive(checkBox.isSelected()));
						setSelectedTest(currentTest);
					}
				}
		);
		checkBoxes.add(checkBox);
		return checkBox;
	}

	private void setSelectedTest(int index) {
		updating = true;
		currentTest = -1;
		if (index == -1) {
			input.setVisible(false);
			output.setVisible(false);
		}
		else {
			input.setVisible(true);
			output.setVisible(true);
			input.setText(tests.get(index).input);
			knowAnswer.setSelected(tests.get(index).output != null);
			output.setText(knowAnswer.isSelected() ? tests.get(index).output : "");
			outputPanel.setVisible(knowAnswer.isSelected());
		}
		currentTest = index;
		testList.setListData(tests.toArray());
		if (testList.getSelectedIndex() != currentTest) {
			testList.setSelectedIndex(currentTest);
		}
		testList.repaint();
		checkBoxesPanel.repaint();
		updating = false;
	}

	private void saveCurrentTest() {
		if (currentTest == -1) {
			return;
		}
		tests.set(
				currentTest, new Test(
						input.getText(), knowAnswer.isSelected() ? output.getText() : null, currentTest,
						checkBoxes.get(currentTest).isSelected()
				)
		);
		outputPanel.setVisible(knowAnswer.isSelected());
	}

	public Test[] getTests() {
		return tests.toArray(new Test[tests.size()]);
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return component;
	}
}
