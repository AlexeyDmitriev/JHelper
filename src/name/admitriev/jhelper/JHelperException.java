package name.admitriev.jhelper;

@SuppressWarnings("UncheckedExceptionClass")
public class JHelperException extends RuntimeException {

	public JHelperException(String message) {
		super(message);
	}

	public JHelperException(String message, Throwable cause) {
		super(message, cause);
	}

}
