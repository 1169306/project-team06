package Consumer;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;
import java.io.File;

import json.gson.TrainingSet;
import json.gson.Question;
import json.gson.TestSet;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;

import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;

public class SDConsumer extends CasConsumer_ImplBase {

	public static final String PATH = "Output";
	public static final String Standard = "goldenstandard";
	public String filePath;
	public String standardPath;
	private Writer fileWriter = null;
	private List<Question> gold;

	public void initialize() throws ResourceInitializationException {
		filePath = (String) getConfigParameterValue(PATH);

		if (filePath == null) {
			throw new ResourceInitializationException(
					ResourceInitializationException.CONFIG_SETTING_ABSENT,
					new Object[] { "output file initialization fail" });
		}

		try {
			fileWriter = new FileWriter(new File(filePath));

		} catch (IOException e) {
			e.printStackTrace();
		}

		// get gloden standard file
		standardPath = (String) getConfigParameterValue(Standard);
		gold = TestSet.load(getClass().getResourceAsStream(standardPath))
				.stream().collect(toList());
		;
		gold.stream()
				.filter(input -> input.getBody() != null)
				.forEach(
						input -> input.setBody(input.getBody().trim()
								.replaceAll("\\s+", " ")));
	}

	public void processCas(CAS aCAS) throws ResourceProcessException {

		JCas jcas = null;
		FSIterator<Annotation> ita  = null;
		try {
			jcas = aCAS.getJCas();
			ita = jcas.getAnnotationIndex(edu.cmu.lti.oaqa.type.input.Question.type).iterator();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		String qid = "";
		while(ita.hasNext()){
			edu.cmu.lti.oaqa.type.input.Question curQuestion = (edu.cmu.lti.oaqa.type.input.Question)ita.next();
			qid = curQuestion.getId();					
		}
		
		FSIterator<TOP> it = jcas.getJFSIndexRepository().getAllIndexedFS(
				AtomicQueryConcept.type);
		String questionText = null;
		while (it.hasNext()){
			AtomicQueryConcept con = (AtomicQueryConcept) it.next();
			questionText = con.getText();
	    }
	   
		 ArrayList<String> resultConcepts = new ArrayList<String>();   
		 it = jcas.getJFSIndexRepository().getAllIndexedFS(
		    		ConceptSearchResult.type);
		 while(it.hasNext()){
			ConceptSearchResult result = (ConceptSearchResult)it.next();
			String gc = result.getUri();
			String[] gcarray = gc.split("&");
			resultConcepts.add(gcarray[gcarray.length - 1]);		
		}
		
		 HashSet<String> ss = new HashSet<String>();
		 double hit = 0;
		 double miss = 0;
		 double totalRel = 0;	
		 for(int i = 0; i < gold.size(); i++){
			Question q = gold.get(i);
			//System.out.println(q.getId() + "  qid " + qid);
			if(qid.equals(q.getId())){
				System.out.println("Metrics on question:" + questionText + "?");
			    ArrayList<String> goldenConcepts = new ArrayList<String>();
				goldenConcepts = (ArrayList<String>) q.getConcepts();
				totalRel = goldenConcepts.size();
				//System.out.println("Golden start-------------------");
				for(int j = 0; j < goldenConcepts.size(); j++){
					//System.out.println(goldenConcepts.get(j));
					String gc = goldenConcepts.get(j);
					String[] gcarray = gc.split("&");
					//ss.add(goldenConcepts.get(j));
					ss.add(gcarray[gcarray.length - 1]);
				}
				//System.out.println("Golden end-------------------");
				//System.out.println("System start-------------------");
				for(int z = 0; z < resultConcepts.size(); z++){
					//System.out.println(resultConcepts.get(z));
					if(ss.contains(resultConcepts.get(z))){
						hit++;
					}else{
						miss++;	
					}
				}
				//System.out.println("System end-------------------" + hit + "  " + miss);
				break;	
			}
		 } 	 		
		 
		 double pre = hit / (hit + miss);
		 double rec = hit / totalRel;
		 System.out.println("Precision = " +  pre); 
		 // System.out.println("Replaced Precision = " + precision(goldenConcepts, resultConcepts));
		 System.out.println("Recall = " + rec);
		 System.out.println("F-measure = " +  2 * pre * rec / (pre + rec));	
	}

	
	/**
	 * Return precision value.
	 * 
	 * @param trueValue
	 * 				True dataset
	 * @param retrValue
	 * 				Retrieval dataset 
	 * @return
	 */
	private <T> double precision(List<T> trueValue, List<T>retrValue) {
		// If retrValue is empty, precision is zero
		if (retrValue.size() == 0) {
			return 0;
		}
		Set<T> trueSet = new HashSet<T>(trueValue);
		Set<T> retrSet  = new HashSet<T>(retrValue);
		// Retain only the elements in goldSet, which excludes all of the false value
		retrSet.retainAll(trueSet);
		int truePositive  = retrSet.size();
		return ((double)truePositive)/((double)retrValue.size());
	}
	
	/**
	 * Return recall value.
	 * @param trueValue
	 * 				True dataset
	 * @param retrValue
	 * 				Retrieval dataset
	 * @return
	 */
	private <T> double recall(List<T> trueValue, List<T> retrValue) {
		// If retrValue is empty, recall is zero
		if (trueValue.size() == 0){
		      return 0;
		}
	    Set<T> trueSet = new HashSet<T>(trueValue);
	    Set<T> retrSet  = new HashSet<T>(retrValue);
	    // Retain only the elements in goldSet, which excludes all of the false value
	    retrSet.retainAll(trueSet);
	    int truePositive  = retrSet.size();
	    return ((double)truePositive) / ((double)trueValue.size());    
	}
	
	/**
	 * Return f-measure value.
	 * @param precision
	 * 				Precision value
	 * @param recall
	 * 				Recall value
	 * @return
	 */
	private double fMeas(double precision, double recall) {
		// If either precision or recall equals to zero, return zero
		if ((precision == 0) || (recall == 0)) {
		     return 0;
		}
		return (2 * precision * recall) / (precision + recall);       
	}
	
	/**
	 * Return average precision
	 * @param trueValue
	 * 				True dataset
	 * @param retrValue
	 * 				Retrieval dataset
	 * @return
	 */
	private <T> double avrPrec(List<T> trueValue, List<T> retrValue){
		// number of positive item so far
		int posiCount = 0;
		double ap = 0.0;
		// the size of the list containing the first r items.
		int numItem = 0;
		for (T item : retrValue) {   
			if (trueValue.contains(item)) {
		       posiCount += 1;
		       ap += (posiCount / ((double)(numItem + 1)));
		    }
		    numItem = numItem + 1;
		}
		// posiCount doesn't increase or ap doesn't increase
		if ((ap == 0) || (posiCount == 0)) {
			return 0;
		}
		return ap / posiCount;       
	}
	
	/**
	 * Return the average value of precision
	 * @param apList
	 * 			Input average precision list
	 * @return
	 */
	private double meanAvrPrec(List<Double> apList) {
		double map = 0;
		for (Double item : apList) {
			map += item;
		}
		return map / (double)apList.size();
	}
	
	/**
	 * Return Geometric Mean Average Precision of the given average precision list
	 * @param apList
	 * 			Input average precision list
	 * @return
	 */
	private double geomMAP(List<Double> apList) {
		double epsilon = Math.pow(10,-15);
		double gmap = 0;
		for (Double item : apList) {
			gmap *= (item + epsilon); 
		}
		gmap = Math.pow(gmap, 1 / apList.size());
		return gmap;
	}
}
