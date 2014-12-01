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
import java.io.BufferedWriter;
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

import util.metrics;
import edu.cmu.lti.oaqa.type.answer.Answer;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.retrieval.TripleSearchResult;

public class SDConsumer extends CasConsumer_ImplBase {

	public static final String PATH = "Output";
	public static final String Standard = "goldenstandard";
	public String filePath;
	public String standardPath;
	private metrics metr = new metrics();
	private static BufferedWriter writer = null;
	
	private List<TestQuestion> gold;
	HashMap<String, TestQuestion> goldSet;
	private List<Double[]> avrPrecList;
	private List<Double[]> precList;
	private List<Double[]> recList;
	private List<Double[]> fMeasureList;
    private List<Answer> ansList;  

	public void initialize() throws ResourceInitializationException {
		filePath = (String) getConfigParameterValue(PATH);
		if (filePath == null) {
			throw new ResourceInitializationException(
					ResourceInitializationException.CONFIG_SETTING_ABSENT,
					new Object[] { "output file initialization fail" });
		}
		try {
			writer = new BufferedWriter(new FileWriter(new File(filePath)));;

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/******* Get Gold Standard file *******/
		standardPath = (String) getConfigParameterValue(Standard);
		gold = TestSet.load(getClass().getResourceAsStream(standardPath))
				.stream().collect(toList());
	
		gold.stream()
				.filter(input -> input.getBody() != null)
				.forEach(
						input -> input.setBody(input.getBody().trim()
								.replaceAll("\\s+", " ")));
		
		 goldSet = new HashMap<String, TestQuestion>();
		 for(TestQuestion que : gold){
			 goldSet.put(que.getId(), que);
		 }
		avrPrecList = new ArrayList<Double[]>();
		precList = new ArrayList<Double[]>();
		recList = new ArrayList<Double[]>();
		fMeasureList = new ArrayList<Double[]>();
	    ansList = new ArrayList<Answer>(); 
	    
	    //print start of json file
	    try {
			writer.write("\"questions\": [\n");
		} catch (IOException e) {
			System.out.println("writer header wrong!");
			e.printStackTrace();
		}
	}
	
	/**
	 * This method is used to calculate the precision, recall, f-measure, and average precision of
	 * Concept, doc and triple, and store the value in a list for each query
	 * 
	 * @param CAS
	 * 
	 * @return void
	 */
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
		
		/******* Get Question *******/
		String body = jcas.getDocumentText();
		
		/******* Get Concept *******/
		//save concepts
		ArrayList<String> concepts = new ArrayList<String>();
		
	    FSIterator<TOP> conceptIter = jcas.getJFSIndexRepository().getAllIndexedFS(ConceptSearchResult.type);
	    Map<Integer,String> conceptMap = new TreeMap<Integer,String>();  
	    while(conceptIter.hasNext()){
	      ConceptSearchResult cpt = (ConceptSearchResult) conceptIter.next();
		  String uri = cpt.getUri();
		  String[] uriArray = uri.split("term=");
	      conceptMap.put(cpt.getRank(),uriArray[uriArray.length - 1]);
	      concepts.add(uri);
	    }
		/******* Get Doc *******/
	    //save documents
	    ArrayList<String> docs = new ArrayList<String>();
	    
	    FSIterator<TOP> docIter = jcas.getJFSIndexRepository().getAllIndexedFS(Document.type);
	    Map<Integer,String> docMap = new TreeMap<Integer,String>();  
	    while(docIter.hasNext()){
	      Document doc  = (Document) docIter.next();       
		  String uri = doc.getUri();
		  String[] uriArray = uri.split("&");
	      docMap.put(doc.getRank(),uriArray[uriArray.length - 1]);
	      docs.add(uri);
	    }  
		/******* Get Triple *******/
	    FSIterator<TOP> tripleIter = jcas.getJFSIndexRepository().getAllIndexedFS(TripleSearchResult.type);
	    Map<Integer,Triple> triMap = new TreeMap<Integer,Triple>();  
	    while(tripleIter.hasNext()){
	      TripleSearchResult trp = (TripleSearchResult) tripleIter.next();
	      edu.cmu.lti.oaqa.type.kb.Triple temp = trp.getTriple();
	      triMap.put(trp.getRank(),new Triple(temp.getSubject(), temp.getPredicate(), temp.getObject())); 
	    }
		/******* Get Answer *******/
	    //save answer
	    String answer = "";
	    
	    FSIterator<TOP> ansIter = jcas.getJFSIndexRepository().getAllIndexedFS(Answer.type);

	    while(ansIter.hasNext()){
	    	Answer ans = (Answer) ansIter.next();
	    	answer = ans.getText();
	    }
	    
//	    while(ansIter.hasNext()){
//	      Answer ans = (Answer) ansIter.next();
//	      ansList.add(ans);
//	    }
	    
	    printResult(body, answer, concepts, docs);
	    
	    List<String> conceptList = new ArrayList<String>(conceptMap.values());
	    List<String> docList = new ArrayList<String>(docMap.values());
	    List<Triple> tripleList = new ArrayList<Triple>(triMap.values());
	    List<String> goldConceptList = new ArrayList<String>();
	    List<String> goldConceptList2 = new ArrayList<String>();
	    List<String> goldDocList = new ArrayList<String>();
	    List<Triple> goldTripleList = new ArrayList<Triple>();
	    
		/******* Get Gold Standard *******/
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
	    
		/******* Add precision, recall, fmeasure, and avrprec *******/
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
	}
	
    @Override
	public void destroy() {
    	
    	
		try{
			writer.write("]}\n");
			writer.close();
		}catch(Exception e){
			e.printStackTrace();
		}
    }
    
	/**
	 * This method is used to calculate the total MAP and GMAP, using the average precision
	 * in the linked list, and print the result
	 * 
	 * @return void
	 */
    @Override
    public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
            IOException {
      super.collectionProcessComplete(arg0);
      int length = avrPrecList.size();
	  /******* Initialize and calculate the MAP/GMAP for concept, doc and triple *******/
      Double map[] = new Double[3];
      Double gmap[] = new Double[3]; 
      map = metr.meanAvrPrec(avrPrecList);
      gmap = metr.geomMAP( avrPrecList);
      
	  /******* print the precision, recall and f-measure *******/
      System.out.println("Final:");
      System.out.println("1:concept;   2:doc;  3:triple");
      for (int i = 0; i < length; i++){
      	System.out.println("  Query" + i + ":");
      	 Double output[] = new Double[3];
      	System.out.print("  precision:    ");
      	output = precList.get(i);
      	for (int j = 0; j < output.length; j++)
      		System.out.print(j+1 + ":  " + output[j] +  "    ");
      	System.out.print("\n  recall:    ");
      	output =  recList.get(i);
      	for (int j = 0; j < output.length; j++)
      		System.out.print(j+1 + ":  " + output[j] +  "    ");
      	System.out.print("\n  fmeasure:    " );
      	output = fMeasureList.get(i);
      	for (int j = 0; j < output.length; j++)
      		System.out.print(j+1 + ":  " + output[j] +  "    ");
      	System.out.println();
      }
	  /******* print MAP and GMAP *******/
      System.out.print("\nMAP:");
      for (int j = 0; j < map.length; j++)
  		System.out.print(j+1 + ":  " + map[j] +  "\t");
      System.out.print("\nGMAP:");
      for (int j = 0; j < gmap.length; j++)
  		System.out.print(j+1 + ":  " + gmap[j] +  "\t");
      System.out.println();
      
      Iterator<Answer> ansIter = ansList.iterator();
      while (ansIter.hasNext()) {
    	  System.out.println("Answer" + ansIter.next().getRank() + "is : " + ansIter.next().getText());
      }
    }
    
    public void printResult(String question, String answer, List<String> concepts, List<String> docs){
    	  	writer.write("\t{\n");  
    	    
    	  	writer.write("\t\t\"body\": \"" + question + "\",\n");
    	    
    	    // print concepts
    	    writer.write("\t\t\"concepts\": [");
    	    for(int i = 0; i < concepts.size(); i++){
    	    	writer.write("\n\t\t\t\"" + concepts.get(i) + "\"");
    	    	if(i < concepts.size() - 1){
    	    		writer.write(",\n");
    	    	}
    	    }
    	    
//    	    while (conceptIt.hasNext()) {
//    	      ConceptSearchResult concept = (ConceptSearchResult) conceptIt.next();
//    	      writer.printf("\n\t\t\t\"%s\"", concept.getUri());
//    	      if (conceptIt.hasNext())
//    	        writer.printf(",");
//    	    }
//    	    writer.printf("\n\t\t],\n");
//    	    // documents
//    	    writer.printf("\t\t\"documents\": [");
//    	    FSIterator docIt = jcas.getJFSIndexRepository().getAllIndexedFS(Document.type);
//    	    while (docIt.hasNext()) {
//    	      Document doc = (Document) docIt.next();
//    	      writer.printf("\n\t\t\t\"%s\"", doc.getTitle());
//    	      if (conceptIt.hasNext())
//    	        writer.printf(",");
//    	    }
//    	    writer.printf("\n\t\t],\n");
//    	    // triples
//    	    writer.printf("\t\t\"triples\": [");
//    	    FSIterator tripleIt = jcas.getJFSIndexRepository().getAllIndexedFS(TripleSearchResult.type);
//    	    while (tripleIt.hasNext()) {
//    	      TripleSearchResult triple = (TripleSearchResult) tripleIt.next();
//    	      writer.printf(
//    	              "\n\t\t\t{\n\t\t\t\t\"o\": \"%s\"\n\t\t\t\t\"p\": \"%s\"\n\t\t\t\t\"s\": \"%s\"\n\t\t\t}",
//    	              triple.getTriple().getObject(), triple.getTriple().getPredicate(), triple.getTriple()
//    	                      .getSubject());
//    	      if (conceptIt.hasNext())
//    	        writer.printf(",");
//    	    }
//    	    writer.printf("\n\t\t],\n");
//    	    // question id
//    	    writer.printf("\t\t\"id\": \"" + question.getId() + "\",\n");
//    	    // the end of the question
//    	    writer.printf("\t},\n");
//    	    // snippets
//    	    writer.printf("\t\t\"triples\": [");
//    	    FSIterator snippetIt = jcas.getJFSIndexRepository().getAllIndexedFS(Passage.type);
//    	    while (snippetIt.hasNext()) {
//    	      Passage snippet = (Passage) snippetIt.next();
//    	      writer.printf(
//    	              "\n\t\t\t{\n\t\t\t\t\"beginSection\": \"%s\"\n\t\t\t\t\"document\": \"%s\"\n\t\t\t\t\"endSection\": \"%s\"\n\t\t\t\t\"offsetInBeginSection\": \"%s\"\n\t\t\t\t\"offsetInEndSection\": \"%s\"\n\t\t\t\t\"text\": \"%s\"\n\t\t\t}",
//    	              snippet.getBeginSection(),snippet.getTitle(),snippet.getEndSection(),snippet.getOffsetInBeginSection(),snippet.getOffsetInEndSection(),snippet.getText());
//    	      if (conceptIt.hasNext())
//    	        writer.printf(",");
//    	    }
//    	    writer.printf("\n\t\t],\n");
//    	    // question id
//    	    writer.printf("\t\t\"id\": \"" + question.getId() + "\",\n");
//    	    // the end of the question
//    	    writer.printf("\t},\n");
    }
}
