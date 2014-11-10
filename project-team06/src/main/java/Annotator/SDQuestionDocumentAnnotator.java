package Annotator;

import java.util.List;
import java.io.IOException;

import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse.Document;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
//import edu.cmu.lti.oaqa.type.retrieval.Document;

public class SDQuestionDocumentAnnotator extends JCasAnnotator_ImplBase {

	  public GoPubMedService service;
		
	  public void initialize(UimaContext aContext) throws ResourceInitializationException {
		    super.initialize(aContext);		   
		    try {
		      service = new GoPubMedService("project.properties");
		    } catch (Exception ex) {
		      throw new ResourceInitializationException();
		    }
		  }
	
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
	    FSIterator<TOP> it = aJCas.getJFSIndexRepository().getAllIndexedFS(
	            AtomicQueryConcept.type);
	    String urlPrefix = "http://www.ncbi.nlm.nih.gov/pubmed/";
	     while(it.hasNext()){
	    	 AtomicQueryConcept con = (AtomicQueryConcept) it.next();
	    	 String text = con.getText();
	    	 
	    	 try {
				PubMedSearchServiceResponse.Result result = service.findPubMedCitations(text, 0);
				List<PubMedSearchServiceResponse.Document> resultList = result.getDocuments();
				for(int i = 0; i < resultList.size(); i++){
					PubMedSearchServiceResponse.Document doc = resultList.get(i);
					//System.out.println(doc.getPmid() + " " + doc.getTitle() + " " + "http://www.ncbi.nlm.nih.gov/pubmed/" + doc.getPmid());
					
					edu.cmu.lti.oaqa.type.retrieval.Document retrievdedDoc = new edu.cmu.lti.oaqa.type.retrieval.Document(aJCas);
					retrievdedDoc.setUri(urlPrefix + doc.getPmid());
					retrievdedDoc.setRank(i);
					retrievdedDoc.setDocId(doc.getPmid());
					retrievdedDoc.setTitle(doc.getTitle());
					retrievdedDoc.addToIndexes();	
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	     }
	    	
	}

}
