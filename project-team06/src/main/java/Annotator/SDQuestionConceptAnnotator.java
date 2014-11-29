package Annotator;

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
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import util.Utils;

import com.google.common.util.concurrent.Service;

import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse.Concept;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse.Finding;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ComplexQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.QueryOperator;

/**
 * The SDQuestionConceptAnnotator uses OntologyServiceResponse service to
 * process the query content and return several features which corresponds to
 * the data type provided by project archetype. Finally, store these information
 * into ConceptSearchResult-typed annotation.
 *
 */
public class SDQuestionConceptAnnotator extends JCasAnnotator_ImplBase {
	/** The GoPubMedService instance variable */
	public GoPubMedService service;
	private int mResultsPerPage = 30;
	/**
	 * The initialize method initialize the GoPubMedService instance using a
	 * preset profile.
	 * 
	 * @see org.apache.uima.analysis_component.JCasAnnotator_ImplBase#initialize(UimaContext)
	 */
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
		super.initialize(aContext);
		try {
			service = new GoPubMedService("project.properties");
		} catch (Exception ex) {
			throw new ResourceInitializationException();
		}
	}

	@Override
	/**
	 * The process performs the main task that stores the information provided by OntologyServiceResponse 
	 * into CAS.
	 * 
	 * @see org.apache.uima.analysis_component.JCasAnnotator_ImplBase#process(JCas)
	 */
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
			queryText = conceptArray.get(0).getText();
			if (conceptArray.size() != 1) {
				int index = 1;
				while (index < conceptArray.size()) {
					queryText += " " + operator.getName() + " "
							+ conceptArray.get(index).getText();
					index++;
				}
			}
			System.out.println(queryText);
			List<Finding> combinedFindings = new ArrayList<Finding>();

			try {
				OntologyServiceResponse.Result result = service
						.findMeshEntitiesPaged(queryText, 0, mResultsPerPage);
				/*for(Finding finding : result.getFindings()){
			         System.out.println(" > " + finding.getConcept().getLabel() + " "
			         + finding.getConcept().getUri()+"\t Score"+finding.getScore());
					
				}*/
				//combinedFindings = Intersect(combinedFindings, result.getFindings());
				for(Finding finding: result.getFindings()){
				//	if(finding.getScore() > 0.2){
						combinedFindings = Union1(combinedFindings, finding);
					//}
				}				
				/*for(Finding finding : combinedFindings){
			         System.out.println(" > " + finding.getConcept().getLabel() + " "
			         + finding.getConcept().getUri()+"\t Score"+finding.getScore());			
				}*/
				/*
				 * int curRank = 0; for (Finding finding : result.getFindings())
				 * { edu.cmu.lti.oaqa.type.kb.Concept concept = new
				 * edu.cmu.lti.oaqa.type.kb.Concept( aJCas);
				 * concept.setName(finding.getConcept().getLabel());
				 * //System.out.println(finding.getConcept().getLabel());
				 * concept.addToIndexes();
				 * 
				 * ConceptSearchResult result1 = new ConceptSearchResult(aJCas);
				 * result1.setConcept(concept);
				 * result1.setUri(finding.getConcept().getUri());
				 * result1.setScore(finding.getScore());
				 * result1.setText(finding.getConcept().getLabel());
				 * result1.setRank(curRank++);
				 * result1.setQueryString(queryText); result1.addToIndexes(); }
				 */
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			/*for(Finding finding : combinedFindings){
		         System.out.println(" > " + finding.getConcept().getLabel() + " "
		         + finding.getConcept().getUri()+"\t Score"+finding.getScore());			
			}*/
			//System.out.println("Concept Size: " + combinedFindings.size());
			int counter = 0;
			int curRank = 0;
			for (Finding finding : combinedFindings) {
				edu.cmu.lti.oaqa.type.kb.Concept concept = new edu.cmu.lti.oaqa.type.kb.Concept(
						aJCas);
				System.out.println(finding);
				concept.setName(finding.getConcept().getLabel());
				concept.addToIndexes();
//				System.out.println("!!!!!!!");
				ConceptSearchResult result1 = new ConceptSearchResult(aJCas);
				result1.setConcept(concept);
				result1.setUri(finding.getConcept().getUri());
				result1.setScore(finding.getScore());
				result1.setText(finding.getConcept().getLabel());
				result1.setRank(curRank++);
				result1.setQueryString(queryText);
				result1.addToIndexes();
				counter++;
			}
			System.out.println("counter is :" + counter);
		}
		System.out.println("Concept finished");
	}

	List<Finding> Intersect(List<Finding> f1, List<Finding> f2) {
		HashMap<Finding, Integer> f1Map = new HashMap<Finding, Integer>();
		Iterator<Finding> iter_f1 = f1.iterator();
		while (iter_f1.hasNext()) {
			Finding afinding = iter_f1.next();
			f1Map.put(afinding, 1);
		}

		Iterator<Finding> iter_f2 = f2.iterator();
		List<Finding> f3 = new ArrayList<Finding>();
		while (iter_f2.hasNext()) {
			Finding afinding = iter_f2.next();
			if (f1Map.containsKey(afinding)) {
				f3.add(afinding);
			}
		}
		return f3;
	}

	List<Finding> Union(List<Finding> f1, List<Finding> f2) {
		HashMap<Finding, Integer> f1Map = new HashMap<Finding, Integer>();
		Iterator<Finding> iter_f1 = f1.iterator();
		while (iter_f1.hasNext()) {
			Finding afinding = iter_f1.next();
			f1Map.put(afinding, 1);
		}

		Iterator<Finding> iter_f2 = f2.iterator();
		while (iter_f2.hasNext()) {
			Finding afinding = iter_f2.next();
			if (!f1Map.containsKey(afinding)) {
				f1.add(afinding);
			}
		}
		return f1;
	}
	
	List<Finding> Union1(List<Finding> f1, Finding f2) {
		HashMap<Finding, Integer> f1Map = new HashMap<Finding, Integer>();
		Iterator<Finding> iter_f1 = f1.iterator();
		while (iter_f1.hasNext()) {
			Finding afinding = iter_f1.next();
			f1Map.put(afinding, 1);
		}
		if (!f1Map.containsKey(f2)) {
			f1.add(f2);
		}
		return f1;
	}

}
