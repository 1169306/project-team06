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

import com.aliasi.tokenizer.TokenizerFactory;

import util.StanfordLemmatizer;
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

  private HashSet<String> stopWords;

  /**
   * The method reads stopwords.txt file to build a set of stop words.
   */
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    System.out.println("SDQuestionAnnotator");

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
      // Tokenize a question to several terms by white-spaces
      List<String> term = tokenize0(text);
      // Iterate each term to do stemming
      Iterator<String> iter_term = term.iterator();
      while (iter_term.hasNext()) {
        String aterm = iter_term.next();
        aterm = StanfordLemmatizer.stemText(aterm.trim());
        // If the term is not a stop word, add it as a AtomicQuery-typed annotation to CAS.
        if (!stopWords.contains(aterm.trim())) {
          AtomicQueryConcept c = new AtomicQueryConcept(aJCas);
          //System.out.println(aterm);
          c.setText(aterm.trim());
          c.setQuestion(question);
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
