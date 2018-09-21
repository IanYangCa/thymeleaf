package hp.hpfb.web.service.utils;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import ca.canada.ised.wet.cdts.components.wet.breadcrumbs.AbstractMockMvcTest;
import hp.hpfb.web.exception.SplException;

public class TestBuildBusinessRule extends AbstractMockMvcTest {
	@Autowired
	private Utilities utilities;
	@Autowired
	public Environment env;
	@Test
	public void testBuildBusinessRule() throws SplException {
		System.out.println("test:" + utilities.UPLOADED_FOLDER);
		utilities.rebuildBusinessRule();
		String testFile = getClass().getClassLoader().getResource("testFile.xml").getPath();
		utilities.renderXml(utilities.DEST_RULE_DIR + Utilities.TARGET_BUSINESS_RULE_FILE + ".xsl", testFile, utilities.UPLOADED_FOLDER + "report.xml", null);
	}
}
