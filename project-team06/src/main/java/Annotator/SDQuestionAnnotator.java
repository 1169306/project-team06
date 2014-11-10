package Annotator;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;

public class SDQuestionAnnotator extends JCasAnnotator_ImplBase{

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		//System.out.println("God Damn Pipeline");
		FSIterator<Annotation> it= aJCas.getAnnotationIndex(Question.type).iterator();
		
		if(it.hasNext()){
			Question question = (Question) it.next();
			AtomicQueryConcept c = new AtomicQueryConcept(aJCas);
			String text = question.getText().replace("?", "");
			c.setText(text);
			c.addToIndexes();
		}
	}

}
