package Annotator;

import java.util.Map;
import java.util.Set;

import util.PosTagNamedEntityRecognizer;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;

public class StanfordNERAnnotator extends JCasAnnotator_ImplBase {

	private PosTagNamedEntityRecognizer stanfordNER;

	/**
	 * The method initializes PosTagNamedEntityRecognizer to perform name entity
	 * recognition.
	 * 
	 * @throws ResourceInitializationException
	 */
	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
		try {
			stanfordNER = new PosTagNamedEntityRecognizer();
		} catch (ResourceInitializationException e) {
			e.printStackTrace();
		}
		System.out.println("StanfordNER annotator is ready...:D");
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		FSIterator<Annotation> questionIter = aJCas.getAnnotationIndex(
				Question.type).iterator();
		while (questionIter.hasNext()) {
			Question q = (Question) questionIter.next();
			String text = q.getText();
			Map<Integer, Integer> resultMap = stanfordNER.getNameSpans(text);
			Set<Integer> keySet = resultMap.keySet();
			for (Integer key : keySet) {
				AtomicQueryConcept atomic = new AtomicQueryConcept(aJCas);
				atomic.setText(text.substring(key, resultMap.get(key)));
				atomic.addToIndexes();
			}
		}
	}

}
