package Annotator;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;

import util.Utils;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse.Document;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ComplexQueryConcept;
//import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.retrieval.QueryOperator;

/**
 * The SDQuestionConceptAnnotator uses PubMedSearchServiceResponse service to
 * process the query content and return several features which corresponds to
 * the data type provided by project archetype. Finally, store these information
 * into Document-typed annotation.
 *
 */
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
	            ComplexQueryConcept.type);
	    String urlPrefix = "http://www.ncbi.nlm.nih.gov/pubmed/";
	     while(it.hasNext()){
	    	 ComplexQueryConcept con = (ComplexQueryConcept) it.next();
	    	 FSList conceptFslist = con.getOperatorArgs();
				ArrayList<AtomicQueryConcept> conceptArray = Utils.fromFSListToCollection(conceptFslist, AtomicQueryConcept.class);
				QueryOperator operator = con.getOperator();
				String queryText = "";
				queryText = conceptArray.get(0).getText();
				if(conceptArray.size() != 1){
					int index = 1;
					while(index < conceptArray.size()){
						queryText += operator;
						queryText += conceptArray.get(index).getText();
						index++;
					}
				}
	    	 
	    	 try {
				PubMedSearchServiceResponse.Result result = service.findPubMedCitations(queryText, 0);
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
