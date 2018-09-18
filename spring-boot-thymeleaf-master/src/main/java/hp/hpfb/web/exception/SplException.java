package hp.hpfb.web.exception;

public class SplException extends Throwable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8310359799695354996L;
	private String errorMsg;

	public SplException(String errorMsg) {
		super();
		this.errorMsg = errorMsg;
	}

	public String getErrorMsg() {
		return errorMsg;
	}
}
