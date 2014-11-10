package Annotator;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import json.JsonCollectionReaderHelper;
import json.gson.Question;
import json.gson.TrainingSet;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

//import util.FileOp;

public class FileCollectionReader extends CollectionReader_ImplBase {
	/**
	 * Name of configuration parameter that must be set to the path of a Input
	 * file.
	 */
	public static final String PATH = "InputFile";

	/**
	 * The name of the file you want to process
	 */
	private String filePath;

	/**
	 * The lines in the input file
	 */
	List<Question> questions;

	/**
	 * the current index of line
	 */
	private int mCurrentIndex;

	/**
	 * @see org.apache.uima.collection.CollectionReader_ImplBase#initialize()
	 */
	public void initialize() throws ResourceInitializationException {
		filePath = (String) getConfigParameterValue(PATH);
		mCurrentIndex = 0;

		questions = TrainingSet.load(getClass().getResourceAsStream(filePath))
				.stream().collect(toList());

		questions
				.stream()
				.filter(input -> input.getBody() != null)
				.forEach(
						input -> input.setBody(input.getBody().trim()
								.replaceAll("\\s+", " ")));
	}

	/**
	 * @see org.apache.uima.collection.CollectionReader#hasNext()
	 */
	public boolean hasNext() {
		return mCurrentIndex < questions.size();
	}

	/**
	 * @see org.apache.uima.collection.CollectionReader#getNext(org.apache.uima.cas.CAS)
	 */
	public void getNext(CAS aCAS) throws IOException, CollectionException {
		JCas jcas;
		try {
			jcas = aCAS.getJCas();
		} catch (CASException e) {
			throw new CollectionException(e);
		}

		// open input stream to file
		Question curQuestion = questions.get(mCurrentIndex++);

		JsonCollectionReaderHelper.addQuestionToIndex(curQuestion, "", jcas);
	}

	/**
	 * @see org.apache.uima.collection.base_cpm.BaseCollectionReader#close()
	 */
	public void close() throws IOException {
	}

	@Override
	public Progress[] getProgress() {
		// TODO Auto-generated method stub
		return null;
	}

}
