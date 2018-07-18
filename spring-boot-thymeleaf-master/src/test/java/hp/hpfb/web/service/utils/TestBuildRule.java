package hp.hpfb.web.service.utils;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ca.canada.ised.wet.cdts.components.wet.breadcrumbs.AbstractMockMvcTest;

public class TestBuildRule extends AbstractMockMvcTest {
	@Autowired
	private Utilities utilities;

	@Test
	public void testRule() {
		utilities.buildRule("C:/TEMP/XML_Schema_Validator/iso_dsdl_include.xsl", "C:/TEMP/XML_Schema_Validator/business-rules.sch", "c:/TEMP/rule/test.xsl", true);
	}
}
