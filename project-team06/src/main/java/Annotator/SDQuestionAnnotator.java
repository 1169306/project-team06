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
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ConfidenceChunker;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;

import util.StanfordLemmatizer;
import util.Utils;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;

/**
 * SDQuestionAnnotator pre-processes the questions and save the terms to AtomicQuery-typed
 * annotations.
 * 
 * @author Hua Tang
 * 
 */
public class SDQuestionAnnotator extends JCasAnnotator_ImplBase {

  private TokenizerFactory aTokenizerFactory;
  
  private static final int MAX_n_best = 2; // Suppose there are 10 annotations in every sentence
  // detected at most.
  private static final double Conf = 0.0;

  private ConfidenceChunker chunker = null;
  
  private HashSet<String> stopWords;
  
  private HashSet<String> termMap;

  /**
   * The method reads stopwords.txt file to build a set of stop words.
   */
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    System.out.println("SDQuestionAnnotator");
    try {
        chunker = (ConfidenceChunker) AbstractExternalizable.readResourceObject(
        		SDQuestionAnnotator.class, (String) aContext.getConfigParameterValue("model"));
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    stopWords = new HashSet<String>();

    BufferedReader br;
    try {
      br = new BufferedReader(new FileReader("src/main/resources/stopwords.txt"));
      String line = null;

      while ((line = br.readLine()) != null) {
        stopWords.add(line.trim());
      }
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
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
    // Retrieve Questions from CAS
    FSIterator<Annotation> it = aJCas.getAnnotationIndex(Question.type).iterator();
    // Question question = null;
    // if (it.hasNext()) {
    // question = (Question) it.next();
    // }
    while (it.hasNext()) {
      Question question = (Question) it.next();
      // System.out.println("My question: " + question);
      // System.out.println("hi" + question.getQuestionType());
      // String type = question.getQuestionType();
      // System.out.println("###########QuestionAnnotator: " + question.getQuestionType());
      // if(question.getQuestionType() == "LIST"){
      // System.out.println("@@@@@@@@@@@@@@@@@@@QuestionAnnotator: " + question.getQuestionType());
      
      // Remove question marks
      String text = question.getText().replace("?", "");   
      System.out.println("original text: " + text);
      //int begin;
      //int end;
      //List<String> term = tokenize0(text);
      //for(String aterm: term){
    	  //char[] termChar = aterm.toCharArray();
    	  char[] termChar = text.toCharArray();
    	  Iterator<Chunk> term_iter = chunker.nBestChunks(termChar, 0, termChar.length, MAX_n_best);
    	  while (term_iter.hasNext()) {
    		  Chunk chunk = term_iter.next();
        double conf = Math.pow(2.0, chunk.score()); // Probability of the detected gene.
        System.out.println("###################");
        if (conf < Conf) { // Adjust confidence
          break;
        }
        System.out.println("@@@@@@@@@@@@@@");
        int begin = chunk.start();
        int end = chunk.end();
        String gene = text.substring(begin, end);
        gene = StanfordLemmatizer.stemText(gene.trim());
        //begin = begin - countWhiteSpaces(text.substring(0, begin));
        //end = begin + gene.length() - countWhiteSpaces(gene) - 1;
        //text = text.replace(gene.trim(), "");
        if(!stopWords.contains(gene.trim())){
	        AtomicQueryConcept c = new AtomicQueryConcept(aJCas);
	        c.setText(gene.trim());
	        System.out.println("agene: " + gene.trim());
	        c.setQuestion(question);
	        c.addToIndexes();
        }
      }
    //  }
    }
  }
  /**
   * This method tokenize the query string by white-spaces and return terms as a List
   * 
   * @param query
   *        The string to be tokenized.
   * 
   */
  private int countWhiteSpaces(String phrase) {
	    int countBlank = 0;
	    for (int i = 0; i < phrase.length(); i++) {
	      if (Character.isWhitespace(phrase.charAt(i))) {
	        countBlank++;
	      }
	    }
	    return countBlank;
	  }
  List<String> tokenize0(String query) {
	    List<String> queryList = new ArrayList<String>();

	    for (String s : query.split("\\s+"))
	      queryList.add(s);
	    return queryList;
	  }
}
