package Consumer;

import static java.util.stream.Collectors.toList;

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
		try {
			jcas = aCAS.getJCas();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		FSIterator<Annotation> it = aJCas.getAnnotationIndex(Question.type)
                .iterator();
		string qid;
		while(it.hasNext()){
			Question curQuestion = (Question)it.next();
			qid = curQuestion.getId();					
		}
		
		FSIterator<TOP> it = jcas.getJFSIndexRepository().getAllIndexedFS(
				AtomicQueryConcept.type);
		String questionText;
		while (it.hasNext()){
			AtomicQueryConcept con = (AtomicQueryConcept) it.next();
			questionText = con.getText();
	    }
	   
		 ArrayList<String> resultConcepts = new ArrayList<String>();   
		 it = jcas.getJFSIndexRepository().getAllIndexedFS(
		    		ConceptSearchResult.type);
		 while(it.hasNext()){
			ConceptSearchResult result = (ConceptSearchResult)it.next();
			resultConcepts.add(result.getText());		
		}
		
		 HashSet<String> set = new HashSet<String>();
		 double hit = 0;
		 double miss = 0;
		 double totalRel;	
		 for(int i = 0; i < gold.size(); i++){
			Question q = gold.get(i);
<<<<<<< HEAD
			System.out.println("-___________");
			System.out.println(q.getId());
			System.out.println(q.getConcepts());
			System.out.println(q.getDocuments());
=======
			if(qid.equal(q.getId())){
				System.out.println("Metrics on question:" + questionText + "?");
			    ArrayList<String> goldenConcepts = new ArrayList<String>();
				goldenConcepts = q.getConcepts();
				totalRel = goldenConcepts.size();
				for(int j = 0; j < goldenConcepts.size(); j++){
					set.put(goldenConcpets.get(j));	
				}
				for(int z = 0; z < resultConcepts.size(); z++){
					if(set.contains(resultConcepts.get(z))){
						hit++;
					}else{
						miss++;	
					}
				}
				break;	
			}
<<<<<<< HEAD
			//System.out.println(q.getConcepts());
			//System.out.println(q.getDocuments());
>>>>>>> 946de4e8a8da2831090c9a98a167785d02998d13
										
		 } 	 		
=======
		 }
		 double pre = hit / (hit + miss);
		 double rec = hit / totalRel;
		 System.out.println("Precision = ", pre); 
		 System.out.println("Recall = ", rec);
		 System.out.println("F-measure = ", 2 * pre * rec / (pre + rec));	
>>>>>>> 6a7aac75c8869657ac2e593a69e98c80a2b1244a
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
		return (2 * precision * recall) / (precision + rec);       
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
		double map;
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
		double gmap;
		for (Double item : apList) {
			gmap *= (item + epsilon); 
		}
		gmap = Math.pow(gmap, 1 / apList.size());
		return gmap;
	}
	
}
