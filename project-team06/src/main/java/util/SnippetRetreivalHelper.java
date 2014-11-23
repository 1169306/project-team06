package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This helper class is used to get the snippet from the remote web service. 
 * @author Victor Zhao <xinyunzh@andrew.cmu.edu>
 *
 */
public class SnippetRetreivalHelper {
	// The prefix for the retrieval web link
	private static final String PREFIX_LINK = "http://metal.lti.cs.cmu.edu:30002/pmc/";

	/**
	 * Given a Reader instance to the method that returns the String instance
	 * which contains all the text within this instance
	 * @param reader
	 * 		The input reader instance
	 * @return
	 */
	private static String readAllContent(Reader reader) {
		StringBuilder strBuild = new StringBuilder();
		char[] buf = new char[4086];
		int numRead;

		try {
			while ((numRead = reader.read(buf)) != -1) {
				strBuild.append(buf, 0, numRead);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return strBuild.toString();
	}

	/**
	 * Given a String-typed url parameter, this method will return a JsonObject by
	 * using Gson package.
	 * @param url
	 * 		The given String-typed url
	 * @return
	 */
	public static JsonObject readJsonFromUrl(String url) {
		InputStream is = null;
		JsonObject jsonObj = null;
		try {
			is = new URL(url).openStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String jsonStr = readAllContent(rd);
		// eliminate leading and trailing spaces in the json text
		if (jsonStr.trim().length() == 0) {
			return null;
		}
		// Test for json content output.
		// System.out.println(jsonStr);
		// Use JasonParser class to convert original content to a JsonElement
		// instance
		JsonElement jsonEle = new JsonParser().parse(jsonStr);
		// Convert JsonElement into JsonObeject
		jsonObj = jsonEle.getAsJsonObject();

		return jsonObj;
	}

	/**
	 * The wrapped methoed that will concatenate the PMID with original URL prefix.
	 * Pass it to readJsonFromUrl method, and return a JsonObject.
	 * @param pmid
	 * 		The given String-typed pmid value
	 * @return
	 */
	public static JsonObject getJsonFromPMID(String pmid) {
		String url = PREFIX_LINK + pmid;
		return readJsonFromUrl(url);
	}

	public static void main(String[] args) {
		// Test for PMID == 23193281
		JsonObject jsonObj = getJsonFromPMID("23193281");
		System.out.println(jsonObj.toString());
	}
}
