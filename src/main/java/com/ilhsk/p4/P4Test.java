
package com.ilhsk.p4;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import com.ilhsk.p4.sample.FileSync;
import com.ilhsk.p4.util.PerforceClient;


public class P4Test {

	public static void main(String[] args) {
	
		PerforceClient.sync("//depot/test/...", "D:\\p4test", null, "ilhsk");
		
		String filePath2 = FileSync.fileCreate("D:\\p4test", "test7.txt", "test!");
		File file1 = new File("D://p4test/test7.txt");		
		List<String> addFile = new ArrayList<String>();
		addFile.add(file1.getAbsolutePath());		
		
		PerforceClient.commit("//depot//test/...", "D:\\p4test", "ilhsk",
				null,addFile,"--",false);

	}

	private  static List<String> getFiles(String path) {
		List<String> result = new ArrayList<>();
		FileFilter logFilefilter = new FileFilter() {
			public boolean accept(File file) {
				if (file.getName().endsWith(".xls")) {
					return true;
				}
				if (file.getName().endsWith("Quest") && file.isDirectory()) {
					return true;
				}
				return false;
			}
		};

		File filePath = new File(path);
		for (File file : filePath.listFiles(logFilefilter)) {
			if(file.isDirectory()) {
				result.addAll(getFiles(file.getPath()));
			}else {
				result.add(file.getPath());

			}
			
		}

		return result;

	}

	private static String getDowloadPath(String path) {

		return "D:\\\\fileTemp\\p4" + File.separator + "migration" + path.replaceAll("//", "/").replace("...", "").replaceAll("/", Matcher.quoteReplacement(File.separator));
		// return "D:\\\\fileTemp\\p4\\a/aa";
	}

}
