package hp.hpfb.web.service.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
	
	@Test
	public void testFormat() {
		for(int i = 0 ; i < 10; i++) {
			System.out.println("i: " + String.format(".%03d", i));
		}
	}
	
	@Test
	public void testFileList() {
		File dir = new File("c:/temp/rules");
		String[] list = dir.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("business", 0);
			} 
			
		});
//		Arrays.asList(list).forEach(item -> System.out.println(item));
		List<String> result = Arrays.stream(list).map(item -> "/businessRule/".concat(item)).collect(Collectors.toList());
		result.forEach(item -> System.out.println(item));
	}
}
