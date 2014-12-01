package Annotator;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;

import util.Utils;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ComplexQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.QueryOperator;

/**
 * ComplexQueryANDAnnotator combines AtomicQueryConcept into ComplexQueryConcept.
 * 
 * @author Hua Tang
 * 
 */
public class ComplexQueryANDAnnotator extends JCasAnnotator_ImplBase {

  /**
   * This method extracts atomic concepts from CAS and combines them into complex concepts.
   * 
   * @throws AnalysisEngineProcessException
   */
  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    // Retrieve AtomicQueryConcept from CAS
    ArrayList<AtomicQueryConcept> atomicList = new ArrayList<AtomicQueryConcept>();
    FSIterator<TOP> atomicIter = aJCas.getJFSIndexRepository().getAllIndexedFS(
            AtomicQueryConcept.type);
    // Add all of AtomicQueryConcepts to an ArrayList
    while (atomicIter.hasNext()) {
      AtomicQueryConcept atomic = (AtomicQueryConcept) atomicIter.next();
      atomicList.add(atomic);
    }
    // Make a ComplexQueryConcpep from these AtomicQueryConcepts
    ComplexQueryConcept complex = new ComplexQueryConcept(aJCas);
    QueryOperator operator = new QueryOperator(aJCas);
    operator.setName("AND");
    complex.setOperator(operator);
    complex.setOperatorArgs(Utils.fromCollectionToFSList(aJCas, atomicList));
    complex.addToIndexes();
  }

}
