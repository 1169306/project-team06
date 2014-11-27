package Annotator;

import java.util.Iterator;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.NBestChunker;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.ScoredObject;

import edu.cmu.lti.oaqa.type.input.Question;

public class SDNERAnnotator extends JCasAnnotator_ImplBase {

	private NBestChunker chunker;
	private static final String chunkerModel = "ModelFile";

	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
		super.initialize(aContext);
		System.out.println("This is NER");
		// Load the LingPipe pre-trained model
		try {
			chunker = (NBestChunker) AbstractExternalizable.readResourceObject(
					SDNERAnnotator.class,
					(String) aContext.getConfigParameterValue(chunkerModel));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void process(JCas aJCas) throws AnalysisEngineProcessException {

	}

}
