package hp.hpfb.web.service.utils;

import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import ca.canada.ised.wet.cdts.components.wet.breadcrumbs.AbstractMockMvcTest;
import hp.hpfb.web.exception.SplException;
import hp.hpfb.web.model.Parameters;
import hp.hpfb.web.model.Report;
import hp.hpfb.web.model.ReportMessage;

public class TestReadDataFromXml  extends AbstractMockMvcTest {
	private static Pattern condition = Pattern.compile("^http.*\\.xsd$");
	@Autowired
	private Utilities utilities;
	
	@Test
	public void testGetXSD() {
		String result;
		try {
			result = utilities.getXSD("src/test/resources/testFile.xml");
			System.out.println("Result:" + result);
			assertTrue(condition.matcher(result).find());
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testGenerateParameters() throws SplException {
		utilities.renderXml(utilities.SRC_RULES_DIR + Utilities.PROPERTITIES + ".xslt", "src/test/resources/testFile.xml", utilities.UPLOADED_FOLDER + Utilities.PROPERTITIES + Utilities.XML, null);
		Parameters p = utilities.getParameters(utilities.UPLOADED_FOLDER);
		System.out.println("Display Language:" + p.getDisplayLanguage());
		System.out.println("ID:" + p.getId());
		System.out.println("Language:" + p.getLanguage());
		System.out.println("DocType:" + p.getDoctype());
		System.out.println("Template:" + p.getTemplate());
		System.out.println("Content Status:" + p.getContentStatus());
	}
	@Test
	public void testReadReport() {
		Report p = utilities.getReportMsgs(utilities.UPLOADED_FOLDER);
		if(p.getReportMessage() != null && p.getReportMessage().size() >0 ) {
			for(ReportMessage r : p.getReportMessage()) {
				System.out.println("Category:" + r.getCategory());
				System.out.println("Details:" + r.getDetails());
				System.out.println("Label:" + r.getLabel());
				System.out.println("Location:" + r.getLocation());
				System.out.println("Rule:" + r.getRule());
				System.out.println("Severity:" + r.getSeverity());
				System.out.println("Test:" + r.getTest());
			}
		}
	}

}
