package name.admitriev.jhelper.exceptions;

/**
 * An exception representing a problem of JHelper.
 */
public class JHelperException extends RuntimeException {

	public JHelperException(String message, Throwable cause) {
		super(message, cause);
	}

	public JHelperException(String message) {
		super(message);
	}

	@Override
	public String getMessage() {
		return "Please, report that bug to issue tracker on https://github.com/AlexeyDmitriev/JHelper . Attach your code and this stack trace. "
		       + super.getMessage();
	}
}
