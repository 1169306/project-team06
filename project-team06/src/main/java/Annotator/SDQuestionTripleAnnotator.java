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
import org.apache.uima.resource.ResourceInitializationException;

import util.Utils;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.LinkedLifeDataServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.LinkedLifeDataServiceResponse.Entity;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse.Finding;
import edu.cmu.lti.oaqa.type.kb.Triple;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ComplexQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.QueryOperator;

/**
 * The SDQuestionTripleAnnotator uses LinkedLifeDataServiceResponse service to
 * process the query content and return several features which corresponds to
 * the data type provided by project archetype. Finally, store these information
 * into Triple-typed annotation.
 *
 */
public class SDQuestionTripleAnnotator extends JCasAnnotator_ImplBase {
	private GoPubMedService service;
	private int mResultsPerPage = 100;

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
			queryText = conceptArray.get(0).getText();
			if (conceptArray.size() != 1) {
				int index = 1;
				while (index < conceptArray.size()) {
					queryText += " " + operator.getName() + " "
							+ conceptArray.get(index).getText();
					index++;
				}
			}
			List<LinkedLifeDataServiceResponse.Entity> combinedEntities = new ArrayList<LinkedLifeDataServiceResponse.Entity>();
			try {
				LinkedLifeDataServiceResponse.Result result = service
						.findLinkedLifeDataEntitiesPaged(queryText, 0,
								mResultsPerPage);
				//combinedEntities = Intersect(combinedEntities, result.getEntities());
				combinedEntities = Union(combinedEntities, result.getEntities());
				/*
				 * List<LinkedLifeDataServiceResponse.Entity> entities = result
				 * .getEntities(); // System.out.println(entities.size()); for
				 * (int i = 0; i < entities.size(); i++) {
				 * LinkedLifeDataServiceResponse.Entity en = entities.get(i);
				 * LinkedLifeDataServiceResponse.Relation relation = en
				 * .getRelations().get(0); //
				 * System.out.println(relation.getSubj() + "  " + //
				 * relation.getPred() + " " + relation.getObj());
				 * 
				 * Triple t = new Triple(aJCas);
				 * t.setSubject(relation.getSubj());
				 * t.setPredicate(relation.getPred());
				 * t.setObject(relation.getObj()); t.addToIndexes(); }
				 */

			} catch (IOException e) {

				e.printStackTrace();
			}
			
			/*for(Entity aentity : combinedEntities){
		         System.out.println(" > " + aentity.getRelations().get(0).getSubj() + " "
		         + aentity.getRelations().get(0).getPred()+" "+aentity.getRelations().get(0).getObj());			
			}*/
			
			System.out.println("Triple Size: " + combinedEntities.size());
			for (int i = 0; i < combinedEntities.size(); i++) {
				LinkedLifeDataServiceResponse.Entity en = combinedEntities
						.get(i);
				LinkedLifeDataServiceResponse.Relation relation = en
						.getRelations().get(0);
				// System.out.println(relation.getSubj() + "  " +
				// relation.getPred() + " " + relation.getObj());

				Triple t = new Triple(aJCas);
				t.setSubject(relation.getSubj());
				t.setPredicate(relation.getPred());
				t.setObject(relation.getObj());
				t.addToIndexes();
			}
		}
		System.out.println("Triple finished");
	}

	List<LinkedLifeDataServiceResponse.Entity> Intersect(
			List<LinkedLifeDataServiceResponse.Entity> E1,
			List<LinkedLifeDataServiceResponse.Entity> E2) {
		HashMap<LinkedLifeDataServiceResponse.Entity, Integer> E1Map = new HashMap<LinkedLifeDataServiceResponse.Entity, Integer>();
		Iterator<LinkedLifeDataServiceResponse.Entity> iter_E1 = E1.iterator();
		while (iter_E1.hasNext()) {
			LinkedLifeDataServiceResponse.Entity aEntity = iter_E1.next();
			E1Map.put(aEntity, 1);
		}

		Iterator<LinkedLifeDataServiceResponse.Entity> iter_E2 = E2.iterator();
		List<LinkedLifeDataServiceResponse.Entity> E3 = new ArrayList<LinkedLifeDataServiceResponse.Entity>();
		while (iter_E2.hasNext()) {
			LinkedLifeDataServiceResponse.Entity aEntity = iter_E2.next();
			if (E1Map.containsKey(aEntity)) {
				E3.add(aEntity);
			}
		}
		return E3;
	}

	List<LinkedLifeDataServiceResponse.Entity> Union(
			List<LinkedLifeDataServiceResponse.Entity> E1,
			List<LinkedLifeDataServiceResponse.Entity> E2) {
		HashMap<LinkedLifeDataServiceResponse.Entity, Integer> E1Map = new HashMap<LinkedLifeDataServiceResponse.Entity, Integer>();
		Iterator<LinkedLifeDataServiceResponse.Entity> iter_E1 = E1.iterator();
		while (iter_E1.hasNext()) {
			LinkedLifeDataServiceResponse.Entity aEntity = iter_E1.next();
			E1Map.put(aEntity, 1);
		}

		Iterator<LinkedLifeDataServiceResponse.Entity> iter_E2 = E2.iterator();
		while (iter_E2.hasNext()) {
			LinkedLifeDataServiceResponse.Entity aEntity = iter_E2.next();
			if (!E1Map.containsKey(aEntity)) {
				E1.add(aEntity);
			}
		}
		return E1;
	}

}
