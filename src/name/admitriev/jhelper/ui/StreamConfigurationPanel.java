package name.admitriev.jhelper.ui;

import com.intellij.openapi.ui.ComboBox;
import net.egork.chelper.task.StreamConfiguration;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panel for configuration input or output for Task.
 */
public class StreamConfigurationPanel extends JPanel {
	private ComboBox type;
	private JTextField fileName;

	public StreamConfigurationPanel(
			StreamConfiguration configuration,
			StreamConfiguration.StreamType[] allowedTypes,
			String defaultFileName,
			final SizeChangedListener listener
	) {
		super(new VerticalLayout());
		type = new ComboBox(allowedTypes);
		type.setSelectedItem(configuration.type);
		type.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						fileName.setVisible(((StreamConfiguration.StreamType) type.getSelectedItem()).hasStringParameter);
						if (listener != null) {
							listener.sizeChanged();
						}
					}
				}
		);
		fileName = new JTextField(configuration.type.hasStringParameter ? configuration.fileName : defaultFileName);
		fileName.setVisible(((StreamConfiguration.StreamType) type.getSelectedItem()).hasStringParameter);

		add(type);
		add(fileName);
	}

	public StreamConfiguration getStreamConfiguration() {
		return new StreamConfiguration((StreamConfiguration.StreamType) type.getSelectedItem(), fileName.getText());
	}

	public interface SizeChangedListener {
		void sizeChanged();
	}
}
