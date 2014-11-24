package util;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ComplexQueryConcept;

/**
 * The helper method that use to convert ComplexQueryConcept into String-typed concept
 * @author Victor Zhao <xinyunzh@andrew.cmu.edu>
 *
 */
public class GenerateQueryString {
	
	public static String complexQueryToString(ComplexQueryConcept query) {
		String operator = query.getOperator().getName();
		ArrayList<AtomicQueryConcept> terms = Utils.fromFSListToCollection(query.getOperatorArgs(), AtomicQueryConcept.class);
		String str = new String();
		for (AtomicQueryConcept term : terms) {
			str += term.getText();
			str += " ";
			str += operator;
		}
		return str;
	}
}
