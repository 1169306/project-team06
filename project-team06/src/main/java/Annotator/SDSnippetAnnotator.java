package Annotator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import org.apache.uima.UimaContext;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;

import util.GenerateQueryString;
import util.SnippetRetreivalHelper;
import edu.cmu.lti.oaqa.type.retrieval.ComplexQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.retrieval.Passage;

import com.google.gson.JsonObject;

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
	/*	FSIterator<TOP> docIter = aJCas.getJFSIndexRepository()
				.getAllIndexedFS(Document.type);
		FSIterator<TOP> queryIter = aJCas.getJFSIndexRepository()
				.getAllIndexedFS(ComplexQueryConcept.type);
		while (docIter.hasNext()) {
			Document doc = (Document) docIter.next();
			JsonObject jsonObj = SnippetRetreivalHelper.getJsonFromPMID(doc
					.getDocId());

			if (jsonObj != null) {
				// Without Operator's query information
				// String queryWOOp = query.getWholeQueryWithoutOp();
				while (queryIter.hasNext()) {
					// Get queryContentString from the GenerateQueryString helper class
					String queryContentString = GenerateQueryString.complexQueryToString((ComplexQueryConcept)queryIter.next());
				}
				
				
				// Json Array information in section
				JsonArray secArr = jsonObj.getAsJsonArray("sections");
				String pmid = doc.getDocId();
				String sec0 = secArr.get(0).getAsString();

				Chunking chunking = SENTENCE_CHUNKER.chunk(sec0.toCharArray(),
						0, sec0.length());
				
				// snetences list
				List<Chunk> sentences = new ArrayList<Chunk>(
						chunking.chunkSet());

				TfIdfDistance tfIdf = new TfIdfDistance(REFINED_TKFACTORY);
				tfIdf.handle(queryWOOp);

				for (int i = 0; i < sentences.size(); ++i) {
					Chunk sentence = sentences.get(i);
					int start = sentence.start();
					int end = sentence.end();
					tfIdf.handle(sec0.substring(start, end));
				}

				List<RawSentence> rawSentences = new ArrayList<RawSentence>();

				for (int i = 0; i < sentences.size(); ++i) {
					int start = sentences.get(i).start();
					int end = sentences.get(i).end();

					RawSentence rawSent = new RawSentence();
					rawSent.startIdx = start;
					rawSent.endIdx = end;
					double sim = tfIdf.proximity(queryWOOp,
							sec0.substring(start, end));
					rawSent.score = sim;
					rawSentences.add(rawSent);
				}

				Collections.sort(rawSentences, new SenSimComparator());

				int threshold = Math.min(5, sentences.size());
				for (int i = 0; i < threshold; ++i) {
					Passage snippet = new Passage(aJCas);
					int startIdx = rawSentences.get(i).startIdx;
					int endIdx = rawSentences.get(i).endIdx;
					snippet.setDocId(pmid);
					snippet.setUri(doc.getUri());
					snippet.setText(sec0.substring(startIdx, endIdx));
					snippet.setBeginSection("sections.0");
					snippet.setEndSection("sections.0");
					snippet.setOffsetInBeginSection(startIdx);
					snippet.setOffsetInEndSection(endIdx);
					snippet.addToIndexes();
				}
			}
		}

	}

*/
	}

}
