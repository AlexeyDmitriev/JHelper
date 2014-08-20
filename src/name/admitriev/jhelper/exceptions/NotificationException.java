package name.admitriev.jhelper.exceptions;

public class NotificationException extends RuntimeException {
	private final String title;
	private final String content;

	public NotificationException(String content) {
		this(content, (Throwable)null);
	}

	public NotificationException(String content, Throwable cause) {
		super(content, cause);
		title = "";
		this.content = content;
	}

	public NotificationException(String title, String content) {
		this(title, content, null);
	}

	public NotificationException(String title, String content, Throwable cause) {
		super(title + ": " + content, cause);
		this.title = title;
		this.content = content;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}
}
