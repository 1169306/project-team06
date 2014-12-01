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
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.tokenizer.TokenizerFactory;

import util.StanfordLemmatizer;
import util.Utils;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ComplexQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.QueryOperator;
import edu.stanford.nlp.io.EncodingPrintWriter.out;

/**
 * This annotator produces query extracted from original
 * question without any processing.
 * @author Victor Zhao <xinyunzh@andrew.cmu.edu>
 *
 */
public class OriginalQuery extends JCasAnnotator_ImplBase {

	private TokenizerFactory aTokenizerFactory;

	/**
	 * initialization function
	 * @param aContext
	 * 	
	 */
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
		super.initialize(aContext);
		System.out.println("ComplexQueryOriginalAnnotator");
	}

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
		ArrayList<AtomicQueryConcept> atomicList = new ArrayList<AtomicQueryConcept>();
		while (it.hasNext()) {
			Question question = (Question) it.next();
			// System.out.println("My question: " + question);
			String text = question.getText().replace("?", "");
			AtomicQueryConcept aConcept = new AtomicQueryConcept(aJCas);
			aConcept.setText(text);
			aConcept.setQuestion(question);
			aConcept.addToIndexes();
			
			atomicList.add(aConcept);
			
			ComplexQueryConcept c = new ComplexQueryConcept(aJCas);
			QueryOperator operator = new QueryOperator(aJCas);
			operator.setName("REQUIRED");
			c.setOperator(operator);
			//System.out.println(c);
			c.setOperatorArgs(Utils.fromCollectionToFSList(aJCas, atomicList));
			c.addToIndexes();
		}
		System.out.println("-------------------------------------------------!!!!");

	}
}
