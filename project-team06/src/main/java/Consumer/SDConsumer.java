package Consumer;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;
import java.io.File;

import json.gson.TestQuestion;
import json.gson.TrainingSet;
import json.gson.Question;
import json.gson.TestSet;
import json.gson.Triple;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;



//import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.retrieval.TripleSearchResult;

public class SDConsumer extends CasConsumer_ImplBase {

	public static final String PATH = "Output";
	public static final String Standard = "goldenstandard";
	public String filePath;
	public String standardPath;
	private metrics metr = new metrics();
	private Writer fileWriter = null;
	
	private List<TestQuestion> gold;
	HashMap<String, TestQuestion> goldSet;
	private List<Double[]> avrPrecList;
	private List<Double[]> precList;
	private List<Double[]> recList;
	private List<Double[]> fMeasureList;
//	private String queryString = "";

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
		
		 goldSet = new HashMap<String, TestQuestion>();
		 for(TestQuestion que : gold){
			 goldSet.put(que.getId(), que);
		 }
		avrPrecList = new ArrayList<Double[]>();;
		precList = new ArrayList<Double[]>();;
		recList = new ArrayList<Double[]>();;
		fMeasureList = new ArrayList<Double[]>();;
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
		
		edu.cmu.lti.oaqa.type.input.Question curQuestion = (edu.cmu.lti.oaqa.type.input.Question)ita.next();				
		
//		String qid = "";
//		while(ita.hasNext()){
//			edu.cmu.lti.oaqa.type.input.Question curQuestion = (edu.cmu.lti.oaqa.type.input.Question)ita.next();
//			qid = curQuestion.getId();					
//		}	
//		FSIterator<TOP> it = jcas.getJFSIndexRepository().getAllIndexedFS(
//				AtomicQueryConcept.type);
//		String questionText = null;
//		while (it.hasNext()){
//			AtomicQueryConcept con = (AtomicQueryConcept) it.next();
//			questionText = con.getText();
//	    }
//	   
//		 ArrayList<String> resultConcepts = new ArrayList<String>();   
//		 it = jcas.getJFSIndexRepository().getAllIndexedFS(
//		    		ConceptSearchResult.type);
//		 while(it.hasNext()){
//			ConceptSearchResult result = (ConceptSearchResult)it.next();
//			String gc = result.getUri();
//			String[] gcarray = gc.split("&");
//			resultConcepts.add(gcarray[gcarray.length - 1]);		
//		}	
		
		// Concept
	    FSIterator<TOP> conceptIter = jcas.getJFSIndexRepository().getAllIndexedFS(ConceptSearchResult.type);
	    Map<Integer,String> conceptMap = new TreeMap<Integer,String>();  
	    while(conceptIter.hasNext()){
	      ConceptSearchResult cpt = (ConceptSearchResult) conceptIter.next();
		  String uri = cpt.getUri();
		  String[] uriArray = uri.split("term=");
//		  System.out.println("The length of Array:" + uriArray.length);
//		  for (int i = 0; i < uriArray.length; i++) {
//			  System.out.println(uriArray[i]);
//		  }
//		  System.out.println("@@@@@@@Final :" + uriArray[uriArray.length - 1]);
	      conceptMap.put(cpt.getRank(),uriArray[uriArray.length - 1]);
	    }
	    //doc
	    FSIterator<TOP> docIter = jcas.getJFSIndexRepository().getAllIndexedFS(Document.type);
	    Map<Integer,String> docMap = new TreeMap<Integer,String>();  
	    while(docIter.hasNext()){
	      Document doc  = (Document) docIter.next();       
		  String uri = doc.getUri();
		  String[] uriArray = uri.split("&");
//		  System.out.println("@@@@@@@Final :" + uriArray[uriArray.length - 1]);
	      docMap.put(doc.getRank(),uriArray[uriArray.length - 1]);
	    }  
	    //triple
	    FSIterator<TOP> tripleIter = jcas.getJFSIndexRepository().getAllIndexedFS(TripleSearchResult.type);
	    Map<Integer,Triple> triMap = new TreeMap<Integer,Triple>();  
	    while(tripleIter.hasNext()){
	      TripleSearchResult trp = (TripleSearchResult) tripleIter.next();
	      // It conflict with gson.Triple
	      edu.cmu.lti.oaqa.type.kb.Triple temp = trp.getTriple();
	      triMap.put(trp.getRank(),new Triple(temp.getSubject(), temp.getPredicate(), temp.getObject())); 
	    }
	    
	    List<String> conceptList = new ArrayList<String>(conceptMap.values());
	    List<String> docList = new ArrayList<String>(docMap.values());
	    List<Triple> tripleList = new ArrayList<Triple>(triMap.values());
	    List<String> goldConceptList = new ArrayList<String>();
	    List<String> goldConceptList2 = new ArrayList<String>();
	    List<String> goldDocList = new ArrayList<String>();
	    List<Triple> goldTripleList = new ArrayList<Triple>();
	    
	    //gold standard
	    String queryId = curQuestion.getId();
	    //  System.out.println(goldSet.containsKey(queryId));
	      if (goldSet.containsKey(queryId)){
	    	goldConceptList2 =  goldSet.get(queryId).getConcepts();
	    	Iterator<String> gcItr = goldConceptList2.iterator(); 
	    	while (gcItr.hasNext()) {
	    		String goldConceptItem = gcItr.next();
	  		    String[] gcArray = goldConceptItem.split("term=");
	    		goldConceptList.add(gcArray[gcArray.length - 1]);	    		
	    	}
	    	goldDocList = goldSet.get(queryId).getDocuments();
	      	List<Triple> tempTriples = goldSet.get(queryId).getTriples();
	      	if (tempTriples != null){
	      		for (Triple tri : tempTriples){
	      			goldTripleList.add(new Triple(tri.getO(), tri.getP(), tri.getS()));
	      		}
	      	}
	      }
	    
	      //add precision, recall, fmeasure, and avrprec
	    Double[] precArray =new Double[3];
	    Double[] recArray =new Double[3];
	    Double[] fMeasureArray =new Double[3];
	    Double[] avrPrecArray = new Double[3];
	    precArray[0] = metr.precision(goldConceptList, conceptList);
	    precArray[1] = metr.precision(goldDocList, docList);
	    precArray[2] = metr.precision(goldTripleList, tripleList);
	    precList.add(precArray);
	    recArray[0] = metr.recall(goldConceptList, conceptList);
	    recArray[1] = metr.recall(goldDocList, docList);
	    recArray[2] = metr.recall(goldTripleList, tripleList);
	    recList.add(recArray);
	    fMeasureArray[0] = metr.fMeas(precArray[0], recArray[0]);
	    fMeasureArray[1] = metr.fMeas(precArray[1], recArray[1]);
	    fMeasureArray[2] = metr.fMeas(precArray[2], recArray[2]);
	    fMeasureList.add(fMeasureArray);
	    avrPrecArray[0] = metr.avrPrec(goldConceptList, conceptList);
	    avrPrecArray[1] = metr.avrPrec(goldDocList, docList);
	    avrPrecArray[2] = metr.avrPrec(goldTripleList, tripleList);
	    avrPrecList.add(avrPrecArray);
	   
//		for(int i = 0; i < gold.size(); i++){
//			Question q = gold.get(i);
//			if(qid.equals(q.getId())){
//				System.out.println("Metrics on question:" + questionText + "?");
//			    ArrayList<String> goldenConcepts = new ArrayList<String>();
//				goldenConcepts = (ArrayList<String>) q.getConcepts();
////				totalRel = goldenConcepts.size();
//				ArrayList<String> filer = new ArrayList<String>();
//				for(int j = 0; j < goldenConcepts.size(); j++){
//					String gc = goldenConcepts.get(j);
//					String[] gcarray = gc.split("&");
////					ss.add(gcarray[gcarray.length - 1]);
//					//golden standard array
//					filer.add(gcarray[gcarray.length - 1]);
//				}
//				prec = metr.precision(filer,resultConcepts); 
//				rec = metr.recall(filer,resultConcepts);
//				fMeasure = metr.fMeas(prec, rec);
////				break;	
//			}
//		 } 	 		 
//		 //double pre = hit / (hit + miss);
//		 //double rec = hit / totalRel;
//		 System.out.println("Precision = " +  prec); 
//		 // System.out.println("Replaced Precision = " + precision(goldenConcepts, resultConcepts));
//		 System.out.println("Recall = " + rec);
//		 System.out.println("F-measure = " +  fMeasure);	
	    
	
	}
    @Override
	public void destroy() {
		 
    }
    
    @Override
    public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
            IOException {
      super.collectionProcessComplete(arg0);
      int length = avrPrecList.size();
      Double map[] = new Double[3];
      Double gmap[] = new Double[3];
      
      map = metr.meanAvrPrec(avrPrecList);
      gmap = metr.geomMAP( avrPrecList);
      
      System.out.println("Final:");
      System.out.println("1:concept;   2:doc;  3:triple");
      for (int i = 0; i < length; i++){
      	System.out.println("  Query" + i + ":");
      	 Double output[] = new Double[3];
      	System.out.print("  precision:");
      	output = precList.get(i);
      	for (int j = 0; j < output.length; j++)
      		System.out.print(j+1 + ":  " + output[j] +  "\t");
      	System.out.print("\n  recall:");
      	output =  recList.get(i);
      	for (int j = 0; j < output.length; j++)
      		System.out.print(j+1 + ":  " + output[j] +  "\t");
      	System.out.print("\n  fmeasure:" );
      	output = fMeasureList.get(i);
      	for (int j = 0; j < output.length; j++)
      		System.out.print(j+1 + ":  " + output[j] +  "\t");
      }
      
      System.out.print("\nMAP:");
      for (int j = 0; j < map.length; j++)
  		System.out.print(j+1 + ":  " + map[j] +  "\t");
      System.out.print("\nGMAP:");
      for (int j = 0; j < gmap.length; j++)
  		System.out.print(j+1 + ":  " + gmap[j] +  "\t");
      System.out.println();
      
    }
    
}
