package Annotator;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.NBestChunker;
import com.aliasi.util.AbstractExternalizable;

public class SDNERAnnotator extends JCasAnnotator_ImplBase {

	private NBestChunker chunker;
	private static final String chunkerModel = "ModelFile";

  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    // Load the LingPipe pre-trained model
    try {
      chunker = (NBestChunker) AbstractExternalizable.readResourceObject(
              SDNERAnnotator.class,
              (String) aContext.getConfigParameterValue(chunkerModel));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void process(JCas arg0) throws AnalysisEngineProcessException {
    // TODO Auto-generated method stub
    
  }

}
