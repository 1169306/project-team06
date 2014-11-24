package Annotator;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;

import util.Utils;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ComplexQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.QueryOperator;

public class ComplexQueryAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		ArrayList<AtomicQueryConcept> atomicList = new ArrayList<AtomicQueryConcept>();
		FSIterator<TOP> it = aJCas.getJFSIndexRepository().getAllIndexedFS(
				AtomicQueryConcept.type);

		String queryString = "";
		String orignalString = "";
		// Iterator atomicIter = atomicIndex.iterator();

		while (it.hasNext()) {
			AtomicQueryConcept atomic = (AtomicQueryConcept) it.next();
			String concept = atomic.getText();
			queryString += concept;
			queryString += " ";
			// atomicList.add(atomic);
		}

		AtomicQueryConcept aConcept = new AtomicQueryConcept(aJCas);
		aConcept.setText(queryString);
		atomicList.add(aConcept);

		ComplexQueryConcept complex = new ComplexQueryConcept(aJCas);
		QueryOperator operator = new QueryOperator(aJCas);
		operator.setName("REQUIRED");
		complex.setOperator(operator);
		complex.setOperatorArgs(Utils.fromCollectionToFSList(aJCas, atomicList));
		complex.addToIndexes();
	}

}
