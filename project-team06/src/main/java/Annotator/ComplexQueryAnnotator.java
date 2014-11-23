package Annotator;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;

import util.Utils;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ComplexQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.QueryOperator;

public class ComplexQueryAnnotator extends JCasAnnotator_ImplBase {

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    ArrayList<AtomicQueryConcept> atomicList = new ArrayList<AtomicQueryConcept>();
    FSIndex atomicIndex = aJCas.getAnnotationIndex(AtomicQueryConcept.type);
    Iterator atomicIter = atomicIndex.iterator();   
    while (atomicIter.hasNext()) {
      AtomicQueryConcept atomic = (AtomicQueryConcept)atomicIter.next();
      atomicList.add(atomic);
    }
    ComplexQueryConcept complex = new ComplexQueryConcept(aJCas);
    QueryOperator operator = new QueryOperator(aJCas);
    operator.setName("REQUIRED");
    complex.setOperator(operator);
    complex.setOperatorArgs(Utils.fromCollectionToFSList(aJCas, atomicList));
    complex.addToIndexes();
  }

}
