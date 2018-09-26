package hp.hpfb.web.handler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class LocalErrorHandler implements ErrorHandler {
	public static String SCHEMA_ERROR_FILE = "schemaError.xml";
	private int errorCount = 0;
	private List<String> errors = new ArrayList<String>();
	

	/**
	 * Report a non-fatal error
	 * 
	 * @param ex
	 *            the error condition
	 */
	public void error(SAXParseException ex) {
		errors.add("At line " + ex.getLineNumber() + " Error: " + ex.getMessage());
		System.err.println("!!!Error:" + ex.getMessage());
		errorCount++;
	}

	/**
	 * Report a fatal error
	 * 
	 * @param ex
	 *            the error condition
	 */
	public void fatalError(SAXParseException ex) {
		errors.add("At line " + ex.getLineNumber() + " of " + ex.getMessage() + '\n');
		System.err.println("!!!Fatal: " + ex.getMessage());
		errorCount++;
	}

	/**
	 * Report a warning
	 * 
	 * @param ex
	 *            the warning condition
	 */
	public void warning(SAXParseException ex) {
		System.err.println("At line " + ex.getLineNumber() + " Warning:  " + ex.getMessage() + '\n');
		System.err.println("!!!Warning: " + ex.getMessage());
	}

	/**
	 * Get the error count
	 * 
	 * @return the number of errors reported, that is, the number of calls on
	 *         error() or fatalError()
	 */
	public int getErrorCount() {
		return errorCount;
	}
	public List<String> getErrors(){
		return errors;
	}
}

