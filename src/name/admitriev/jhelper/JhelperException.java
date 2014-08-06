package name.admitriev.jhelper;

@SuppressWarnings("UncheckedExceptionClass")
public class JhelperException extends RuntimeException {

	public JhelperException(String message) {
		super(message);
	}

	public JhelperException(String message, Throwable cause) {
		super(message, cause);
	}

}
