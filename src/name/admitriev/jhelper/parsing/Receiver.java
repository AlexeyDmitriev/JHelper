package name.admitriev.jhelper.parsing;

import net.egork.chelper.parser.Description;
import net.egork.chelper.parser.DescriptionReceiver;

import java.util.Collection;

/**
 * A class for receiving available contests and problems from parser asynchronously
 */
public abstract class Receiver implements DescriptionReceiver {
	private boolean stopped = false;

	@Override
	public boolean isStopped() {
		return stopped;
	}

	public void stop() {
		stopped = true;
	}

	public static class Empty extends Receiver {
		public Empty() {
			stop();
		}

		@Override
		public void receiveDescriptions(Collection<Description> descriptions) {

		}
	}
}
