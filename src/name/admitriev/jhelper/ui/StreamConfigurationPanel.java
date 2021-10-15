package name.admitriev.jhelper.ui;

import com.intellij.openapi.ui.ComboBox;
import net.egork.chelper.task.StreamConfiguration;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.util.Objects;

/**
 * Panel for configuration input or output for Task.
 */
public class StreamConfigurationPanel extends JPanel {
	private final ComboBox<StreamConfiguration.StreamType> type;
	private JTextField fileName;

	public StreamConfigurationPanel(
		StreamConfiguration configuration,
		StreamConfiguration.StreamType[] allowedTypes,
		String defaultFileName,
		SizeChangedListener listener
	) {
		super(new VerticalLayout());
		type = new ComboBox<>(allowedTypes);
		type.setSelectedItem(configuration.type);
		type.addActionListener(
			e -> {
				fileName.setVisible(((StreamConfiguration.StreamType) Objects.requireNonNull(type.getSelectedItem())).hasStringParameter);
				if (listener != null) {
					listener.sizeChanged();
				}
			}
		);
		fileName = new JTextField(configuration.type.hasStringParameter ? configuration.fileName : defaultFileName);
		fileName.setVisible(((StreamConfiguration.StreamType) Objects.requireNonNull(type.getSelectedItem())).hasStringParameter);

		add(type);
		add(fileName);
	}

	public StreamConfiguration getStreamConfiguration() {
		return new StreamConfiguration((StreamConfiguration.StreamType) type.getSelectedItem(), fileName.getText());
	}

	@FunctionalInterface
	public interface SizeChangedListener {
		void sizeChanged();
	}
}
