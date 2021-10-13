
package com.ilhsk.p4.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Auth ilhsk
 * @Description
 * 
 *              <pre></pre>
 */
public class FileUtil {
	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);


	public static boolean Write(String path, String contents) {
		FileUtil.Mkdirs(path);
		try (FileWriter fw = new FileWriter(new File(path))) {
			fw.write(contents);
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
			return false;
		}
		return true;

	}


	
	public static boolean Mkdirs(String path) {
		if (logger.isErrorEnabled())
			logger.debug("fileCreate fileName : {}", path);
		try {
			File file = new File(new File(path).getParentFile().toString());
			if (file.isDirectory() == false) {
				file.mkdirs();
			}
			return true;
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
			return false;
		}
	}

	public static boolean CreateFolder(String path) {
		if (logger.isErrorEnabled())
			logger.debug("fileCreate file Path : {}", path);
		System.out.println(path);
		try {
			File folder = new File(path);
			if (!folder.exists()) {
				System.out.println("create");
				folder.mkdirs();
			}
			return true;
		} catch (Exception e) {
			System.out.println(e);
			if (logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
			return false;
		}
	}

	public static String ReadFile(String filePath) {
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer(); // 테스트용 변수
		try {
			br = new BufferedReader(new FileReader(filePath));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			if (logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (Exception e) {
			}
		}

		return sb.toString();
	}



	public static void deleteFile(String path) {
		File deleteFolder = new File(path);
		if (deleteFolder.exists()) {
			if (deleteFolder.isDirectory()) {
				File[] deleteFolderList = deleteFolder.listFiles();

				for (int i = 0; i < deleteFolderList.length; i++) {
					if (deleteFolderList[i].isFile()) {
						deleteFolderList[i].delete();
					} else {
						deleteFile(deleteFolderList[i].getPath());
					}
					deleteFolderList[i].delete();
				}
				deleteFolder.delete();
			} else {
				deleteFolder.delete();
			}
		}
	}

	static boolean equalfiles(File f1, File f2) {

		try {
			byte[] b1 = FileUtil.getBytesFromFile(f1);
			byte[] b2 = FileUtil.getBytesFromFile(f2);

			if (b1.length != b2.length)
				return false;
			for (int i = 0; i < b1.length; i++) {
				if (b1[i] != b2[i])
					return false;
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	static int firstDiffBetween(File f1, File f2) {
		try {
		byte[] b1 = FileUtil.getBytesFromFile(f1);
		byte[] b2 = FileUtil.getBytesFromFile(f2);

		int shortest = b1.length;
		if (b2.length < shortest)
			shortest = b2.length;
		for (int i = 0; i < shortest; i++) {
			if (b1[i] != b2[i])
				return i;
		}
		} catch (IOException e) {
			return -1;
		}
		return -1;
	}

	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}
	
	public static boolean isMove(String filePath, String targetFile) {
		FileUtil.Mkdirs(targetFile);		
		String targetPath = targetFile.substring(0, targetFile.lastIndexOf(File.separator));
		
		Path file = Paths.get(filePath);
		Path movePath = Paths.get(targetPath);		 
		
		try {			
			Files.move(file , movePath .resolve(file.getFileName()));
			logger.debug("파일 이동함 : "+filePath +" >> "+targetFile );
		} catch (IOException e) {
			logger.error("filePath : {}, targetFile :{} , targetPath : {} , errorMessage:{}",filePath,targetFile,targetPath,e.getMessage(), e);
			return false;
		}
		return true;
	}

}
