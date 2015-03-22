package name.admitriev.jhelper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import net.egork.chelper.task.Test;
import net.egork.chelper.ui.VariableGridLayout;
import net.egork.chelper.util.Utilities;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditTestsDialog extends DialogWrapper {
	private static int HEIGHT = new JLabel("Test").getPreferredSize().height;

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
		mainLayout.setColFraction(0, 0.35);
		mainLayout.setColFraction(1, 0.65);
		JPanel mainPanel = new JPanel(mainLayout);
		JPanel selectorAndButtonsPanel = new JPanel(new BorderLayout());
		selectorAndButtonsPanel.add(new JLabel("Tests:"), BorderLayout.NORTH);
		JPanel checkBoxesAndSelectorPanel = new JPanel(new BorderLayout());
		checkBoxesPanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, false, false));
		for (Test test : tests) {
			JCheckBox checkBox = createCheckBox(test);
			checkBoxesPanel.add(checkBox);
		}
		checkBoxesAndSelectorPanel.add(checkBoxesPanel, BorderLayout.WEST);
		testList = new JBList((Object[]) tests);
		testList.setFixedCellHeight(HEIGHT);
		testList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		testList.setLayoutOrientation(JList.VERTICAL);
		testList.addListSelectionListener(
				new ListSelectionListener() {
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
//		testList.addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseClicked(MouseEvent e) {
//				int index = testList.locationToIndex(e.getPoint());
//				if (index >= 0 && index < testList.getItemsCount()) {
//					saveCurrentTest();
//					setSelectedTest(index);
//				}
//			}
//		});
		checkBoxesAndSelectorPanel.add(testList, BorderLayout.CENTER);
		selectorAndButtonsPanel.add(
				new JBScrollPane(
						checkBoxesAndSelectorPanel,
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
				), BorderLayout.CENTER
		);
		JPanel buttonsPanel = new JPanel(new GridLayout(3, 1));
		JPanel upperButtonsPanel = new JPanel(new GridLayout(1, 2));
		JButton all = new JButton("All");
		all.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int index = 0;
						for (JCheckBox checkBox : checkBoxes) {
							checkBox.setSelected(true);
							EditTestsDialog.this.tests.set(
									index,
									EditTestsDialog.this.tests.get(index).setActive(true)
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
					public void actionPerformed(ActionEvent e) {
						int index = 0;
						for (JCheckBox checkBox : checkBoxes) {
							checkBox.setSelected(false);
							EditTestsDialog.this.tests.set(
									index,
									EditTestsDialog.this.tests.get(index).setActive(false)
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
					public void actionPerformed(ActionEvent e) {
						saveCurrentTest();
						int index = EditTestsDialog.this.tests.size();
						Test test = new Test("", "", index);
						EditTestsDialog.this.tests.add(test);
						checkBoxesPanel.add(createCheckBox(test));
						setSelectedTest(index);
					}
				}
		);
		middleButtonsPanel.add(newTest);
		JButton remove = new JButton("Remove");
		remove.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (currentTest == -1) {
							return;
						}
						while (checkBoxes.size() > currentTest) {
							checkBoxesPanel.remove(checkBoxes.get(currentTest));
							checkBoxes.remove(currentTest);
						}
						EditTestsDialog.this.tests.remove(currentTest);
						int size = EditTestsDialog.this.tests.size();
						for (int i = currentTest; i < size; i++) {
							Test test = EditTestsDialog.this.tests.get(i);
							test = new Test(test.input, test.output, i, test.active);
							EditTestsDialog.this.tests.set(i, test);
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
		selectorAndButtonsPanel.add(buttonsPanel, BorderLayout.SOUTH);
		mainPanel.add(selectorAndButtonsPanel);
		JPanel testPanel = new JPanel(new GridLayout(2, 1, 5, 5));
		JPanel inputPanel = new JPanel(new BorderLayout());
		inputPanel.add(new JLabel("Input:"), BorderLayout.NORTH);
		DocumentListener listener = new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				saveCurrentTest();
			}

			public void removeUpdate(DocumentEvent e) {
				saveCurrentTest();
			}

			public void changedUpdate(DocumentEvent e) {
				saveCurrentTest();
			}
		};
		input = new JTextArea();
		input.setFont(Font.decode(Font.MONOSPACED));
		input.getDocument().addDocumentListener(listener);
		inputPanel.add(new JBScrollPane(input), BorderLayout.CENTER);
		outputPanel = new JPanel(new BorderLayout());
		outputPanel.add(new JLabel("Output:"), BorderLayout.NORTH);
		output = new JTextArea();
		output.setFont(Font.decode(Font.MONOSPACED));
		output.getDocument().addDocumentListener(listener);
		outputPanel.add(new JBScrollPane(output), BorderLayout.CENTER);
		knowAnswer = new JCheckBox("Know answer?");
		knowAnswer.addActionListener(
				new ActionListener() {
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
		mainPanel.add(testPanel);
		mainPanel.setPreferredSize(new Dimension(600, 400));
		component = mainPanel;
		setSelectedTest(Math.min(0, tests.length - 1));

		setLocation(Utilities.getLocation(project, getSize()));

		init();
	}

	private JCheckBox createCheckBox(final Test test) {
		final JCheckBox checkBox = new JCheckBox("", test.active);
		Dimension preferredSize = new Dimension(checkBox.getPreferredSize().width, HEIGHT);
		checkBox.setPreferredSize(preferredSize);
		checkBox.setMaximumSize(preferredSize);
		checkBox.setMinimumSize(preferredSize);
		checkBox.addActionListener(
				new ActionListener() {
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
//        output.invalidate();
//        output.repaint();
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
