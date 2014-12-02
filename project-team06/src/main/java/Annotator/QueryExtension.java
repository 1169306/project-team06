package Annotator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.tokenizer.TokenizerFactory;

import util.StanfordLemmatizer;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ComplexQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;

/**
 * SDQuestionAnnotator pre-processes the questions and save the terms to AtomicQuery-typed
 * annotations.
 * 
 * @author Hua Tang
 * 
 */
public class QueryExtension extends JCasAnnotator_ImplBase {

  private TokenizerFactory aTokenizerFactory;

  private HashSet<String> original;

  /**
   * The method reads stopwords.txt file to build a set of stop words.
   */
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    System.out.println("QueryExtension");
    original = new HashSet<String>();
  }

  @Override
  /**
   * The process method reads the question-typed query from CAS. Then it removes question mark,
   *  do stemming on each term and store the pre-processed terms into AtomicQuery-typed annotations.
   * 
   * @param aJCas
   * 				The reference to the JCas instance of the pipeline.
   * 
   */
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
	FSIterator<TOP> it_atomic = aJCas.getJFSIndexRepository().getAllIndexedFS(
			AtomicQueryConcept.type);
	while(it_atomic.hasNext()){
		AtomicQueryConcept atomic = (AtomicQueryConcept) it_atomic.next();
		if(!original.contains(atomic.getText().trim())){
			original.add(atomic.getText().trim());
		}
	}
	FSIterator<TOP> it_concept = aJCas.getJFSIndexRepository().getAllIndexedFS(
			ConceptSearchResult.type);
    while (it_concept.hasNext()) {
    	ConceptSearchResult conceptResult = (ConceptSearchResult) it_concept.next();
    	String text = conceptResult.getText().replace(",", "");
    	List<String> term = tokenize0(text);   	
    	Iterator<String> iter_term = term.iterator();
        while (iter_term.hasNext()) {
          String aterm = iter_term.next();
          aterm = StanfordLemmatizer.stemText(aterm.trim());
          if (!original.contains(aterm.trim())) {
        	  original.add(aterm.trim());
        	  AtomicQueryConcept c = new AtomicQueryConcept(aJCas);
        	  c.setText(aterm.trim());
              c.addToIndexes();
          }
        }
    }
  }
  /**
   * This method tokenize the query string by white-spaces and return terms as a List
   * 
   * @param query
   *        The string to be tokenized.
   * 
   */
  List<String> tokenize0(String query) {
    List<String> queryList = new ArrayList<String>();

    for (String s : query.split("\\s+"))
      queryList.add(s);
    return queryList;
  }
}
