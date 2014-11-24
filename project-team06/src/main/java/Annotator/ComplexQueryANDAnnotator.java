package Annotator;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;

import util.Utils;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ComplexQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.QueryOperator;

public class ComplexQueryANDAnnotator extends JCasAnnotator_ImplBase {

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    // TODO Auto-generated method stub
    ArrayList<AtomicQueryConcept> atomicList = new ArrayList<AtomicQueryConcept>();
    FSIterator<TOP> atomicIter = aJCas.getJFSIndexRepository().getAllIndexedFS(AtomicQueryConcept.type);

    //Iterator atomicIter = atomicIndex.iterator();

     while (atomicIter.hasNext()) {
        AtomicQueryConcept atomic = (AtomicQueryConcept)atomicIter.next();
    atomicList.add(atomic);
   }

    ComplexQueryConcept complex = new ComplexQueryConcept(aJCas);
    QueryOperator operator = new QueryOperator(aJCas);
    operator.setName("AND");
    complex.setOperator(operator);
    complex.setOperatorArgs(Utils.fromCollectionToFSList(aJCas, atomicList));
    complex.addToIndexes();
  }

}

