package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

// main.java.edu.cmu.lti.deiis.project.annotator.Chunking;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.spell.TfIdfDistance;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.StopTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

/**
 * This helper class is used to get the snippet from the remote web service.
 * 
 * @author Victor Zhao <xinyunzh@andrew.cmu.edu>
 *
 */
public class SnippetRetreivalHelper {
	// The prefix for the retrieval web link
	private static final String PREFIX_LINK = "http://metal.lti.cs.cmu.edu:30002/pmc/";

	/**
	 * Given a Reader instance to the method that returns the String instance
	 * which contains all the text within this instance
	 * 
	 * @param reader
	 *            The input reader instance
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
	 * Given a String-typed url parameter, this method will return a JsonObject
	 * by using Gson package.
	 * 
	 * @param url
	 *            The given String-typed url
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
	 * The wrapped methoed that will concatenate the PMID with original URL
	 * prefix. Pass it to readJsonFromUrl method, and return a JsonObject.
	 * 
	 * @param pmid
	 *            The given String-typed pmid value
	 * @return
	 */
	public static JsonObject getJsonFromPMID(String pmid) {
		String url = PREFIX_LINK + pmid;
		return readJsonFromUrl(url);
	}

	public static void main(String[] args) {
		TokenizerFactory BASE_TKFACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
		SentenceModel SENTENCE_MODEL = new MedlineSentenceModel();
		SentenceChunker SENTENCE_CHUNKER;
		SENTENCE_CHUNKER = new SentenceChunker(BASE_TKFACTORY, SENTENCE_MODEL);
		
		// Test for PMID == 23193281
		JsonObject jsonObj = getJsonFromPMID("23193281");
		System.out.println(jsonObj.toString());
		// Test for extract different part from JSON
		JsonArray secArr = jsonObj.getAsJsonArray("sections");
		System.out.println(secArr);
		String sec0 = secArr.get(0).getAsString();
		System.out.println("*********");
		System.out.println(sec0);
		// Chunking chunking = SENTENCE_CHUNKER.chunk(sec0.toCharArray(), 0,
		// 		sec0.length());
		
	}
}
