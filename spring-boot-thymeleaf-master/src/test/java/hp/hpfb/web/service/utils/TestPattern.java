package hp.hpfb.web.service.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class TestPattern {
	String test = "C:\\Users\\hcuser\\Downloads\\test\\data\\input\\2.zip";
	public static Pattern zipPattern = Pattern.compile(".+\\.zip$");
	
	@Test
	public void testZip() {
		Matcher matcher = zipPattern.matcher(test);
		if (matcher.find()) {
            System.out.println("Start index: " + matcher.start());
            System.out.println(" End index: " + matcher.end() + " ");
            System.out.println(matcher.group());
            System.out.println("size: " + test.length());
        } else {
        	System.out.println("Not Match!!");
        }
	}
}
