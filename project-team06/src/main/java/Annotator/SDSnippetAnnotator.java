package Annotator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import edu.cmu.lti.oaqa.type.answer.CandidateAnswerVariant;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;

import util.GenerateQueryString;
import util.SnippetRetreivalHelper;
import util.TypeFactory;
import edu.cmu.lti.oaqa.type.retrieval.ComplexQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.retrieval.Passage;

import com.google.gson.JsonObject;
import java.io.IOException;

public class SDSnippetAnnotator extends JCasAnnotator_ImplBase {

	private SentenceChunker SENTENCE_CHUNKER;

	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
		super.initialize(aContext);
		TokenizerFactory BASE_TKFACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
		SentenceModel SENTENCE_MODEL = new MedlineSentenceModel();
		SENTENCE_CHUNKER = new SentenceChunker(BASE_TKFACTORY, SENTENCE_MODEL);

	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		System.out.println("Hi~Snippet!");
		FSIterator<TOP> docIter = aJCas.getJFSIndexRepository()
				.getAllIndexedFS(Document.type);
		// FSIterator<TOP> queryIter = aJCas.getJFSIndexRepository()
		// .getAllIndexedFS(ComplexQueryConcept.type);
		while (docIter.hasNext()) {
			Document doc = (Document) docIter.next();
			String url = doc.getUri();

			// store query sentence
			String query = doc.getQueryString();
			// System.out.println("query is" + query);
			String[] queryArray = query.split("\\s+");
			// System.out.println("QueryArray is" + queryArray);
			// Test for printing out queryArray
//			for (String str : queryArray) {
//				System.out.println(str);
//			}
			// storing query vector
			Map<String, Integer> queryVector = new HashMap<String, Integer>();
			// store
			for (String str : queryArray) {
				// System.out.println("queryVector = " + queryVector);
				// System.out.println("***************");
				if (queryVector.get(str) != null) {
					if (queryVector.get(str) != 0) {
						queryVector.put(str, queryVector.get(str) + 1);
					} else {
						queryVector.put(str, 1);
					}
				} else {
					queryVector.put(str, 1);
				}
			}
			JsonObject jsonObj = SnippetRetreivalHelper.getJsonFromPMID(doc
					.getDocId());
			/******* Snippet *******/
			if (jsonObj != null) {
				// Json Array information in section
				JsonArray secArr = jsonObj.getAsJsonArray("sections");
				String pmid = doc.getDocId();
				String sec0 = secArr.get(0).getAsString();
				System.out.println(sec0);
				// split the whole article into each sentence
				// replace ? ! with . to divide into different sentence
				String stopArticle = sec0.replace("!", ".").replace("?", ".").replace("\n", " ");
				System.out.println("The stopArticle is : " + stopArticle);
				String[] sentence = stopArticle.split("\\.");
				Map<Integer, Map<String, Integer>> vec = new HashMap<Integer, Map<String, Integer>>();
				Map<Integer, Double> similarityMap = new HashMap<Integer, Double>();
				// store the id of the sentence with max
				int maxId = 0;
				// max similarity
				double simi = 0.0;
				// calculate each vector of each sentence and store into map
				System.out.println("*** Length of sentence:" + sentence.length);
				for (int i = 0; i < sentence.length; i++) {
					String[] words = sentence[i].replace(",", "")
							.replace(":", "").replace("'s", "")
							.replace("\"", "").replace("--", " ")
							.replace("-", " ").replace(";", "").split("\\s+");
					// store the vector of each sentence in passage
					Map<String, Integer> docVector = new HashMap<String, Integer>();
					System.out.println("VVVVVVVVVVVVV");
					System.out.println("The stopArticle is : " + stopArticle);
					System.out.println("The query is:" + query);
					System.out.println("The " + i + "'s sentence is:" + sentence[i]);
					// store the vector
					for (String str : words) {
						if (docVector.get(str) != null) {
							if (docVector.get(str) != 0) {
								docVector.put(str, docVector.get(str) + 1);
							} else {
								docVector.put(str, 1);
							}
						} else {
							docVector.put(str, 1);
						}
					}
					double similarity = computeCosineSimilarity(queryVector,
							docVector);
					System.out.println("@@@@@@ Similarity is:" + similarity);
					similarityMap.put(i, similarity);

					if (similarity > simi) {
						simi = similarity;
						maxId = i;
					}
				}
				// calculate the start and the stop position of each passage
				System.out.println("maxId is" + maxId);
				int start = stopArticle.indexOf(sentence[maxId]);
				int end = sentence[maxId].length() + 1;

				Passage passage = TypeFactory.createPassage(aJCas, url,
						doc.getScore(), sentence[maxId], doc.getRank(), query,
						"", new ArrayList<CandidateAnswerVariant>(),
						doc.getTitle(), doc.getDocId(), start, end,
						"sections.0", "sections.0", "");

				passage.addToIndexes();
				
			}
		}
	}

	/**
	 * This method is used to calculate the cosine_similarity for two input
	 * vector, which are Query Vector and Doc Vector, respectively.
	 * 
	 * @param queryVector
	 *            Query Vector
	 * 
	 * @param docVector
	 *            Document Vector
	 * 
	 * @return cosine_similarity
	 */
	private double computeCosineSimilarity(Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		double cosine_similarity = 0.0;

		// TODO :: compute cosine similarity between two sentences
		double qVLen, dVLen = 0;
		qVLen = computeVectorLength(queryVector);
		dVLen = computeVectorLength(docVector);
		Set<Entry<String, Integer>> entrySetQV = queryVector.entrySet();
		for (Entry<String, Integer> entryQV : entrySetQV) {
			String commonString = entryQV.getKey();
			if (docVector.containsKey(commonString)) {
				cosine_similarity += entryQV.getValue()
						* docVector.get(commonString);
			}
		}
		cosine_similarity = cosine_similarity / (qVLen * dVLen);

		return (double) Math.round(cosine_similarity * 10000) / 10000;
	}

	private double computeVectorLength(Map<String, Integer> vector) {
		double vLen = 0;
		for (Integer freq : vector.values()) {
			vLen = freq * freq + vLen;
		}
		vLen = Math.sqrt(vLen);
		return vLen;
	}

}
