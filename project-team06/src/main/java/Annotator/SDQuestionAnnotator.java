package Annotator;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import util.Utils;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ComplexQueryConcept;

public class SDQuestionAnnotator extends JCasAnnotator_ImplBase {

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
		// System.out.println("God Damn Pipeline");
		FSIterator<Annotation> it = aJCas.getAnnotationIndex(Question.type)
				.iterator();
		if (it.hasNext()) {
			Question question = (Question) it.next();
			AtomicQueryConcept c = new AtomicQueryConcept(aJCas);
			String text = question.getText().replace("?", "");
			c.setText(text);
			c.addToIndexes();
			List<AtomicQueryConcept> a= new ArrayList<AtomicQueryConcept>();
			a.add(c);

			ComplexQueryConcept complexQuery = new ComplexQueryConcept(aJCas);
			complexQuery.setOperatorArgs(Utils.fromCollectionToFSList(aJCas, a));
			complexQuery.addToIndexes();	
		}
	}

}
