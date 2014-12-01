package Annotator;

import java.util.ArrayList;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;

import util.Utils;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ComplexQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.QueryOperator;

/**
 * ComplexQueryAnnotator combines several AtomicQueryConcepts into a bag of words and save the
 * string into a ComplexQueryConcept.
 * 
 * @author Carol Cheng, Rui Wang
 * 
 */
public class ComplexQueryAnnotator extends JCasAnnotator_ImplBase {
  /**
   * This method extracts atomic concepts from CAS and combines them into a white-space separated
   * string.
   * 
   * @param aJCas
   * 	UIMA index, provides access to data
   * @throws AnalysisEngineProcessException
   */
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
	  // Retrieve AtomicQueryConcepts from CAS
		ArrayList<AtomicQueryConcept> atomicList = new ArrayList<AtomicQueryConcept>();
		FSIterator<TOP> it = aJCas.getJFSIndexRepository().getAllIndexedFS(
				AtomicQueryConcept.type);

		String queryString = "";
		String orignalString = "";
		// Make "queryString" from AtomicQueryConcepts
		while (it.hasNext()) {
			AtomicQueryConcept atomic = (AtomicQueryConcept) it.next();
			String concept = atomic.getText();
			queryString += concept;
			queryString += " ";
		}
		AtomicQueryConcept aConcept = new AtomicQueryConcept(aJCas);
		aConcept.setText(queryString);
		atomicList.add(aConcept);
		// Save "queryString" to a ComplexQueryConcept
		ComplexQueryConcept complex = new ComplexQueryConcept(aJCas);
		QueryOperator operator = new QueryOperator(aJCas);
		operator.setName("REQUIRED");
		complex.setOperator(operator);
		complex.setOperatorArgs(Utils.fromCollectionToFSList(aJCas, atomicList));
		complex.addToIndexes();
	}

}
