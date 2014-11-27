package Annotator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.tokenizer.TokenizerFactory;

import util.StanfordLemmatizer;
import util.Utils;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ComplexQueryConcept;
import edu.stanford.nlp.io.EncodingPrintWriter.out;

public class SDQuestionAnnotator extends JCasAnnotator_ImplBase {

	private TokenizerFactory aTokenizerFactory;

	private HashMap<String, Integer> stopWords;

	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
		super.initialize(aContext);
		System.out.println("SDQuestionAnnotator");

		stopWords = new HashMap<String, Integer>();

		BufferedReader br;
		try {
              br = new BufferedReader(new FileReader("src/main/resources/stopwords.txt"));
              String line = null;
             
              while((line = br.readLine()) != null){
                if(!stopWords.containsKey(line)){
                  stopWords.put(line, 1);;
                }
              }
              br.close();
            }catch (IOException e){
              e.printStackTrace();
           	} 
  		}

	@Override
	/**
	 * The process method will read the question-typed query from annotation with question mark 
	 * eliminated and store the text content into AtomicQuery-typed annotation.
	 * 
	 * @param aJCas
	 * 				The reference to the JCas instance of the pipeline.
	 * 
	 */
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		FSIterator<Annotation> it = aJCas.getAnnotationIndex(Question.type)
				.iterator();
		while (it.hasNext()) {
			Question question = (Question) it.next();
			// System.out.println("My question: " + question);
			String text = question.getText().replace("?", "");
			List<String> term = tokenize0(text);
		    Iterator<String> iter_term = term.iterator();
		    while (iter_term.hasNext()) {
		    	String aterm = iter_term.next();
		        aterm = StanfordLemmatizer.stemText(aterm);
		        
		        if(!stopWords.containsKey(aterm)){
		            AtomicQueryConcept c = new AtomicQueryConcept(aJCas);
		           System.out.println(aterm);
		           c.setText(aterm);
		           c.setQuestion(question);
		           c.addToIndexes();
		        }   
		    }	
		}
	}

	List<String> tokenize0(String query) {
		List<String> queryList = new ArrayList<String>();

		for (String s : query.split("\\s+"))
			queryList.add(s);
		return queryList;
	}
}
