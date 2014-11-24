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
	private metrics metr = new metrics();
	private Writer fileWriter = null;
	
	private List<Question> gold;
	private List<Double[]> precision;
	private List<Double[]> recall;
	private List<Double[]> avrPre;
	private double meanAvrPre;
	private double geomMAP;
	double pre;
	double rec;
	double fMeasure ;

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
		System.out.println("XXXXXXXXX");
	}
	

	public void processCas(CAS aCAS) throws ResourceProcessException {
		System.out.println("God Damn Pipeline - consumer");
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
//		 double hit = 0;
//		 double miss = 0;
//		 double totalRel = 0;	
		 

		for(int i = 0; i < gold.size(); i++){
			Question q = gold.get(i);
			if(qid.equals(q.getId())){
				System.out.println("Metrics on question:" + questionText + "?");
			    ArrayList<String> goldenConcepts = new ArrayList<String>();
				goldenConcepts = (ArrayList<String>) q.getConcepts();
//				totalRel = goldenConcepts.size();
				ArrayList<String> filer = new ArrayList<String>();
				for(int j = 0; j < goldenConcepts.size(); j++){
					String gc = goldenConcepts.get(j);
					String[] gcarray = gc.split("&");
					ss.add(gcarray[gcarray.length - 1]);
					//golden standard array
					filer.add(gcarray[gcarray.length - 1]);
				}
//				for(int z = 0; z < resultConcepts.size(); z++){
//					if(ss.contains(resultConcepts.get(z))){
//						hit++;
//					}else{
//						miss++;	
//					}
//				}
				//call precision function
				pre = metr.precision(resultConcepts, filer); 
				rec = metr.recall(resultConcepts, filer);
				fMeasure = metr.fMeas(pre, rec);
				break;	
			}
		 } 	 		
		 
		 //double pre = hit / (hit + miss);
		 //double rec = hit / totalRel;
		 System.out.println("Precision = " +  pre); 
		 // System.out.println("Replaced Precision = " + precision(goldenConcepts, resultConcepts));
		 System.out.println("Recall = " + rec);
		 System.out.println("F-measure = " +  fMeasure);	
	}

}
