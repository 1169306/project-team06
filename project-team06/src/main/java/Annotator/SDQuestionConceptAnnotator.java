package Annotator;

import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.util.concurrent.Service;

import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse.Concept;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse.Finding;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;




public class SDQuestionConceptAnnotator extends JCasAnnotator_ImplBase{
	
	  public GoPubMedService service;
	
	  public void initialize(UimaContext aContext) throws ResourceInitializationException {
		    super.initialize(aContext);		   
		    try {
		      service = new GoPubMedService("project.properties");
		    } catch (Exception ex) {
		      throw new ResourceInitializationException();
		    }
		  }
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
	    FSIterator<TOP> it = aJCas.getJFSIndexRepository().getAllIndexedFS(
	            AtomicQueryConcept.type);
		 //System.out.println("-_____________-");
	     while(it.hasNext()){
	    	 AtomicQueryConcept con = (AtomicQueryConcept) it.next();
	    	 String text = con.getText();
	    	 try {
				OntologyServiceResponse.Result result = service.findMeshEntitiesPaged(text, 0);
				int curRank = 0;
				for (Finding finding : result.getFindings()) {
					edu.cmu.lti.oaqa.type.kb.Concept concept = new edu.cmu.lti.oaqa.type.kb.Concept(aJCas);
			        concept.setName(finding.getConcept().getLabel());
			        concept.addToIndexes();	
			        
			        ConceptSearchResult result1 = new ConceptSearchResult(aJCas);
			        result1.setConcept(concept);
			        result1.setUri(finding.getConcept().getUri());
			        result1.setScore(finding.getScore());
			        result1.setText(finding.getConcept().getLabel());
			        result1.setRank(curRank++);
			        result1.setQueryString(text);
			        result1.addToIndexes();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	 			
	     }
	}

}
