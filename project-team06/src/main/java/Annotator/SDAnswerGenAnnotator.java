package Annotator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import json.gson.Triple;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import edu.cmu.lti.oaqa.type.answer.Answer;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.retrieval.Passage;
import util.SimilarityCalculation;

/**
 * This annotator produces exact answer for each question.
 * @author Victor Zhao 
 */
public class SDAnswerGenAnnotator extends JCasAnnotator_ImplBase {
    private Map<Double,String> ansMap;
    
	/**
	 * initialization function
	 * @param aContext
	 * 	
	 */
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
		super.initialize(aContext);
	    ansMap = new TreeMap<Double,String>(Collections.reverseOrder());
	}
	
	  /**
	   * This method produces exact answer.
	   * 
	   * @param aJCas
	   * 	UIMA index, provides access to data
	   * @throws AnalysisEngineProcessException
	   */
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		
		FSIterator<TOP> passIter = aJCas.getJFSIndexRepository()
				.getAllIndexedFS(Passage.type);
		while (passIter.hasNext()) {
			System.out.println ("Hi , Answer!!!!!");
			Passage passage = (Passage)passIter.next();
			//Query
			String query = passage.getQueryString();
			String[] queryArray = query.split("\\s+");
			Map<String, Integer> queryVector = new HashMap<String, Integer>();
			// store
			for (String str : queryArray) {
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
			//Answer
			String ans = passage.getText();
			String[] ansArray = ans.split("\\s+");
			Map<String, Integer> ansVector = new HashMap<String, Integer>();
			// store
			for (String str : ansArray) {
				if (ansVector.get(str) != null) {
					if (ansVector.get(str) != 0) {
						ansVector.put(str, ansVector.get(str) + 1);
					} else {
						ansVector.put(str, 1);
					}
				} else {
					ansVector.put(str, 1);
				}
			}
			//Evaluate
			double similarity = SimilarityCalculation.computeCosineSimilarity(queryVector,
					ansVector);
			ansMap.put(similarity, ans);
		}
		Iterator<Entry<Double, String>> treMapItr = ansMap.entrySet().iterator();
		int rank = 0;
//		while(treMapItr.hasNext()) {
//			Entry<Double, String> simiAns = treMapItr.next();
//			Answer ans = new Answer(aJCas);
//			ans.setText(simiAns.getValue());
//	        ans.setRank(rank++);
//	        ans.addToIndexes();
//	        System.out.println("Answer" + ans.getRank() + ans.getText());
//	        if (rank >= 5)
//	        	break;
//		}
		while(treMapItr.hasNext()){	
			Entry<Double, String> simiAns = treMapItr.next();
			if(simiAns.getKey() > 0.1){
				Answer ans = new Answer(aJCas);
				ans.setText("YES");
				ans.setRank(1);
				ans.addToIndexes();
				return;
			}
		}
		Answer ans = new Answer(aJCas);
		ans.setText("NO");
		ans.setRank(1);
		ans.addToIndexes();
	}

}
