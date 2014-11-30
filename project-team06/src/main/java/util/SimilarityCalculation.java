package util;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class SimilarityCalculation {
	/**
	 * This method is used to calculate the cosine_similarity for two input
	 * vector, which are Query Vector and Doc Vector, respectively.
	 * 
	 * @param queryVector
	 *            Query Vector
	 * 
	 * @param docVector
	 *            Document Vector
	 * 
	 * @return cosine_similarity
	 */
	public static double computeCosineSimilarity(Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		double cosine_similarity = 0.0;

		// TODO :: compute cosine similarity between two sentences
		double qVLen, dVLen = 0;
		qVLen = computeVectorLength(queryVector);
		dVLen = computeVectorLength(docVector);
		Set<Entry<String, Integer>> entrySetQV = queryVector.entrySet();
		for (Entry<String, Integer> entryQV : entrySetQV) {
			String commonString = entryQV.getKey();
			if (docVector.containsKey(commonString)) {
				cosine_similarity += entryQV.getValue()
						* docVector.get(commonString);
			}
		}
		cosine_similarity = cosine_similarity / (qVLen * dVLen);

		return (double) Math.round(cosine_similarity * 10000) / 10000;
	}

	public static double computeVectorLength(Map<String, Integer> vector) {
		double vLen = 0;
		for (Integer freq : vector.values()) {
			vLen = freq * freq + vLen;
		}
		vLen = Math.sqrt(vLen);
		return vLen;
	}
}
