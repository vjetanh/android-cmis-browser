package de.fmaul.android.cmis.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import android.app.Application;
import android.os.Environment;

public class StorageUtils {

	public static final String TYPE_FEEDS = "feeds";
	public static final String TYPE_CONTENT = "content";

	public static boolean isFeedInCache(Application app, String url, String workspace) {
		File cacheFile = getFeedFile(app, workspace, md5(url));
		return cacheFile != null && cacheFile.exists();
	}

	public static Document getFeedFromCache(Application app, String url, String workspace) {
		File cacheFile = getFeedFile(app, workspace, md5(url));
		Document document = null;
		SAXReader reader = new SAXReader(); // dom4j SAXReader
		try {
			document = reader.read(cacheFile);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // dom4j Document

		return document;
	}

	private static File getFeedFile(Application app, String repoId, String feedHash) {
		return getStorageFile(repoId, TYPE_FEEDS, null, feedHash + ".xml");
	}

	public static void storeFeedInCache(Application app, String url, Document doc, String workspace) {
		File cacheFile = getFeedFile(app, workspace, md5(url));
		ensureOrCreatePathAndFile(cacheFile);

		try {
			XMLWriter writer = new XMLWriter(new FileOutputStream(cacheFile));
			writer.write(doc);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static File getStorageFile(String repoId, String storageType, String itemId, String filename) {
		StringBuilder builder = new StringBuilder();
		builder.append(Environment.getExternalStorageDirectory());
		builder.append("/");
		//builder.append(app.getPackageName());
		builder.append("android-cmis-browser");
		builder.append("/");
		builder.append(repoId);
		if (storageType != null) {
			builder.append("/");
			builder.append(storageType);
		}
		if (itemId != null) {
			builder.append("/");
			builder.append(itemId.replaceAll(":", "_"));
		}
		if (filename != null) {
			builder.append("/");
			builder.append(filename);
		}
		return new File(builder.toString());
	}

	public static File createStorageFile(Application app, String repoId, String storageType, String itemId, String filename) {

		File contentFile = getStorageFile(repoId, storageType, itemId, filename);
		ensureOrCreatePathAndFile(contentFile);
		return contentFile;
	}

	private static void ensureOrCreatePathAndFile(File contentFile) {
		try {
			contentFile.getParentFile().mkdirs();
			contentFile.createNewFile();
		} catch (IOException iox) {
			throw new RuntimeException(iox);
		}
	}

	public static String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++)
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static boolean deleteRepositoryFiles(Application app, String repoId) {
		File repoDir = getStorageFile(repoId, null, null, null);
		try {
			FileUtils.deleteDirectory(repoDir);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static boolean deleteRepositoryCacheFiles(Application app, String repoId) {
		File contentDir = getStorageFile(repoId, TYPE_CONTENT, null, null);
		File feedsDir = getStorageFile(repoId, TYPE_FEEDS, null, null);
		try {
			FileUtils.deleteDirectory(contentDir);
			FileUtils.deleteDirectory(feedsDir);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public static boolean deleteFeedFile(String repoId, String url) {
		File feedFile = getStorageFile(repoId, TYPE_FEEDS, null,  md5(url));
		try {
			FileUtils.deleteDirectory(feedFile);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

}
