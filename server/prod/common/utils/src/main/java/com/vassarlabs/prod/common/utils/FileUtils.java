package com.vassarlabs.prod.common.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;


public class FileUtils {

	/**
	 * Get Files in given path.
	 * 
	 * @param filePath
	 * @param fileFilter
	 * @return
	 */
	public static File[] getFilesInPath(String filePath, FileFilter fileFilter, final String fileExtFilter){
		
		File dir = new File(filePath);
		
		if (fileFilter != null) {
			return dir.listFiles(fileFilter);
		}
		
		if (StringUtils.isNullOrEmpty(fileExtFilter)) {
			return dir.listFiles();
		}
		
		// create new filename filter to get fileFilterStr
		FilenameFilter fileNameFilter = new FilenameFilter() {
   
            @Override
            public boolean accept(File dir, String name) {
            	if (name.endsWith(fileExtFilter)) {
            		return true;
            	}
            	return false;
            }
		};
		
		return dir.listFiles(fileNameFilter);
	}
	
	/**
	 * Renames File.
	 * 
	 * @param oldName
	 * @param newName
	 */
	public static void renameFile(File oldName, File newName) {
		try {
			oldName.renameTo(newName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Renames File.
	 * 
	 * @param oldName
	 * @param newName
	 */
	public static boolean renameFile(String oldName, String newName) {
		
		File oFile = new File(oldName);
		File nFile = new File(newName);
		
		if (!oFile.exists()) {
			System.out.println("Failed to rename file : " + oldName + " TO " + newName);
			return false;
		}
		
		int fileCounter = 0;
		while (true) {
			if (!nFile.exists()) {
				break;
			}
			++fileCounter;
			nFile = new File(newName + "_" + fileCounter);
		}

		String newNameTmp = null;
		if (fileCounter == 0) {
			newNameTmp = newName;
		} else {
			newNameTmp = newName + "_" + fileCounter;
		}

		try {
			oFile.renameTo(new File(newNameTmp));
			return true;
		} catch (Exception e) {
			// TODO: Log Error
			System.out.println("Error renaming file : " + oldName + " to " + newName + " [" + newNameTmp + "]");
			e.printStackTrace();
		}
		return false;
	}
	
	public static void removeFilesInPath(List<String> filePaths) {
		
		for(String filePath : filePaths) {
			File file = new File(filePath);
			if(file.exists())
				file.delete();
			else
				System.out.println("File not found : " + filePath);
		}
		
	}

	public static boolean fileExists(String filePath) throws IOException {
		File file = new File(filePath);
		if(file.exists())
			return true;
		else
			return false;
	}
	
	public static File createFileIfNotExists(String filePath) throws IOException {

		File file = new File(filePath);
		if(!file.exists() && !file.isDirectory()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		} else if(!file.exists());
		{
			file.createNewFile();
		}
		return file;
	}
}