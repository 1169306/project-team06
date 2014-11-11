package Annotator;

import java.io.IOException;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.LinkedLifeDataServiceResponse;
import edu.cmu.lti.oaqa.type.kb.Triple;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;

/**
 * The SDQuestionTripleAnnotator uses LinkedLifeDataServiceResponse service to
 * process the query content and return several features which corresponds to
 * the data type provided by project archetype. Finally, store these information
 * into Triple-typed annotation.
 *
 */
public class SDQuestionTripleAnnotator extends JCasAnnotator_ImplBase {
	public GoPubMedService service;

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
				AtomicQueryConcept.type);
		// System.out.println("-_____________-");
		while (it.hasNext()) {
			AtomicQueryConcept con = (AtomicQueryConcept) it.next();
			String text = con.getText();

			try {
				LinkedLifeDataServiceResponse.Result result = service
						.findLinkedLifeDataEntitiesPaged(text, 0, 1);

				List<LinkedLifeDataServiceResponse.Entity> entities = result
						.getEntities();
				// System.out.println(entities.size());
				for (int i = 0; i < entities.size(); i++) {
					LinkedLifeDataServiceResponse.Entity en = entities.get(i);
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

			} catch (IOException e) {

				e.printStackTrace();
			}
		}

	}

}
