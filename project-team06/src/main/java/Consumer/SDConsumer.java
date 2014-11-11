package Consumer;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;
import java.io.File;

import json.gson.TrainingSet;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;

import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;

public class SDConsumer extends CasConsumer_ImplBase {

	public static final String PATH = "Output";
	public String filePath;
	private Writer fileWriter = null;

	public void initialize() throws ResourceInitializationException {
		filePath = (String) getConfigParameterValue(PATH);
		try {
			fileWriter = new FileWriter(new File(filePath));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void processCas(CAS aCAS) throws ResourceProcessException {

		JCas jcas = null;
		try {
			jcas = aCAS.getJCas();
		} catch (Exception e) {
			e.printStackTrace();
		}

		FSIterator<TOP> it = jcas.getJFSIndexRepository().getAllIndexedFS(
				AtomicQueryConcept.type);
		while (it.hasNext()) {
			AtomicQueryConcept con = (AtomicQueryConcept) it.next();
			String text = con.getText();
			System.out.println(text);
			try {
				fileWriter.append(text + "\n");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		it = jcas.getJFSIndexRepository().getAllIndexedFS(
				ConceptSearchResult.type);
		while (it.hasNext()) {
			ConceptSearchResult re = (ConceptSearchResult) it.next();
		}

	}

	public void destroy() {
		try {
			fileWriter.close();
			// this.statictics();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
