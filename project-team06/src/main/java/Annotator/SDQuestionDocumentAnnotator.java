package Annotator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse.Finding;
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
	private static final String urlPrefix = "http://www.ncbi.nlm.nih.gov/pubmed/";
	private GoPubMedService service;
	private int mResultsPerPage = 200;

	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
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
		while (it.hasNext()) {
			ComplexQueryConcept con = (ComplexQueryConcept) it.next();
			FSList conceptFslist = con.getOperatorArgs();
			ArrayList<AtomicQueryConcept> conceptArray = Utils
					.fromFSListToCollection(conceptFslist,
							AtomicQueryConcept.class);
			QueryOperator operator = con.getOperator();
			String queryText = "";
			String queryString = "";
			queryText = conceptArray.get(0).getText();
			queryString = conceptArray.get(0).getText();
			if (conceptArray.size() != 1) {
				int index = 1;
				while (index < conceptArray.size()) {
					queryText += " " + operator.getName() + " "
							+ conceptArray.get(index).getText();
					queryString +=" " + conceptArray.get(index).getText();
					index++;
				}
			}
			List<PubMedSearchServiceResponse.Document> combinedDocs = new ArrayList<PubMedSearchServiceResponse.Document>();
			try {
				PubMedSearchServiceResponse.Result result = service
						.findPubMedCitations(queryText, 0, mResultsPerPage);
				//combinedDocs = Intersect(combinedDocs, result.getDocuments());
				
					combinedDocs = Union(combinedDocs, result.getDocuments());
				
				/*
				 * List<PubMedSearchServiceResponse.Document> resultList =
				 * result.getDocuments(); 
				 * for(int i = 0; i < resultList.size();
				 * i++){ PubMedSearchServiceResponse.Document doc =
				 * resultList.get(i); //System.out.println(doc.getPmid() + " " +
				 * doc.getTitle() + " " + "http://www.ncbi.nlm.nih.gov/pubmed/"
				 * + doc.getPmid());
				 * 
				 * edu.cmu.lti.oaqa.type.retrieval.Document retrievdedDoc = new
				 * edu.cmu.lti.oaqa.type.retrieval.Document(aJCas);
				 * retrievdedDoc.setUri(urlPrefix + doc.getPmid());
				 * retrievdedDoc.setRank(i);
				 * retrievdedDoc.setDocId(doc.getPmid());
				 * retrievdedDoc.setTitle(doc.getTitle());
				 * retrievdedDoc.addToIndexes(); }
				 */
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/*for(Document adoc : combinedDocs){
		         System.out.println(" > " + adoc.getPmid() + " "
		         + adoc.getTitle());			
			}*/
			
			System.out.println("Docs Size: " + combinedDocs.size());
			for (int i = 0; i < combinedDocs.size(); i++) {
				PubMedSearchServiceResponse.Document doc = combinedDocs.get(i);
				// System.out.println(doc.getPmid() + " " + doc.getTitle() + " "
				// + "http://www.ncbi.nlm.nih.gov/pubmed/" + doc.getPmid());

				edu.cmu.lti.oaqa.type.retrieval.Document retrievdedDoc = new edu.cmu.lti.oaqa.type.retrieval.Document(
						aJCas);
				retrievdedDoc.setUri(urlPrefix + doc.getPmid());
				retrievdedDoc.setRank(i);
				retrievdedDoc.setDocId(doc.getPmid());
				retrievdedDoc.setTitle(doc.getTitle());
				retrievdedDoc.setQueryString(queryString);
				retrievdedDoc.addToIndexes();
			}
		}
		System.out.println("Document finished");
	}

	List<PubMedSearchServiceResponse.Document> Intersect(
			List<PubMedSearchServiceResponse.Document> D1,
			List<PubMedSearchServiceResponse.Document> D2) {
		HashMap<PubMedSearchServiceResponse.Document, Integer> D1Map = new HashMap<PubMedSearchServiceResponse.Document, Integer>();
		Iterator<PubMedSearchServiceResponse.Document> iter_D1 = D1.iterator();
		while (iter_D1.hasNext()) {
			PubMedSearchServiceResponse.Document aDoc = iter_D1.next();
			D1Map.put(aDoc, 1);
		}

		Iterator<PubMedSearchServiceResponse.Document> iter_D2 = D2.iterator();
		List<PubMedSearchServiceResponse.Document> D3 = new ArrayList<PubMedSearchServiceResponse.Document>();
		while (iter_D2.hasNext()) {
			PubMedSearchServiceResponse.Document aDoc = iter_D2.next();
			if (D1Map.containsKey(aDoc)) {
				D3.add(aDoc);
			}
		}
		return D3;
	}

	List<PubMedSearchServiceResponse.Document> Union(
			List<PubMedSearchServiceResponse.Document> D1,
			List<PubMedSearchServiceResponse.Document> D2) {
		HashMap<PubMedSearchServiceResponse.Document, Integer> D1Map = new HashMap<PubMedSearchServiceResponse.Document, Integer>();
		Iterator<PubMedSearchServiceResponse.Document> iter_D1 = D1.iterator();
		while (iter_D1.hasNext()) {
			PubMedSearchServiceResponse.Document aDoc = iter_D1.next();
			D1Map.put(aDoc, 1);
		}

		Iterator<PubMedSearchServiceResponse.Document> iter_D2 = D2.iterator();
		while (iter_D2.hasNext()) {
			PubMedSearchServiceResponse.Document aDoc = iter_D2.next();
			if (!D1Map.containsKey(aDoc)) {
				D1.add(aDoc);
			}
		}
		return D1;
	}
}
