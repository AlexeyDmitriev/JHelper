package name.admitriev.jhelper.exception;

public class ReportNeededException extends RuntimeException {

	public ReportNeededException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReportNeededException(String message) {
		super(message);
	}

	@Override
	public String getMessage() {
		return "Please, report that bug to issue tracker on https://github.com/AlexeyDmitriev/JHelper . Attach your code and this stack trace. " + super.getMessage();
	}
}
