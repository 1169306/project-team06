package Consumer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class metrics {
	/**
	 * Return precision value.
	 * 
	 * @param trueValue
	 * 				True dataset
	 * @param retrValue
	 * 				Retrieval dataset 
	 * @return
	 */
	public <T> double precision(List<T> trueValue, List<T>retrValue) {
		// If retrValue is empty, precision is zero
		if (retrValue.size() == 0) {
			return 0;
		}
		Set<T> trueSet = new HashSet<T>(trueValue);
		Set<T> retrSet  = new HashSet<T>(retrValue);
		// Retain only the elements in goldSet, which excludes all of the false value
		retrSet.retainAll(trueSet);
		int truePositive  = retrSet.size();
		//return ((double)truePositive)/((double)trueValue.size());
		return ((double)truePositive) / ((double)retrValue.size());
	}
	
	/**
	 * Return recall value.
	 * @param trueValue
	 * 				True dataset
	 * @param retrValue
	 * 				Retrieval dataset
	 * @return
	 */
	public <T> double recall(List<T> trueValue, List<T> retrValue) {
		// If trueValue is empty, recall is zero
		if (trueValue.size() == 0){
		      return 0;
		}
	    Set<T> trueSet = new HashSet<T>(trueValue);
	    Set<T> retrSet  = new HashSet<T>(retrValue);
	    // Retain only the elements in goldSet, which excludes all of the false value
	    retrSet.retainAll(trueSet);
	    int truePositive  = retrSet.size();
	    return ((double)truePositive) / ((double)trueValue.size());    
	}
	
	/**
	 * Return f-measure value.(F1-Score)
	 * @param precision
	 * 				Precision value
	 * @param recall
	 * 				Recall value
	 * @return
	 */
	public double fMeas(double precision, double recall) {
		// If either precision or recall equals to zero, return zero
		if ((precision == 0) || (recall == 0)) {
		     return 0;
		}
		return (2 * precision * recall) / (precision + recall);       
	}
	
	/**
	 * Return average precision(AP)
	 * @param trueValue
	 * 				True dataset
	 * @param retrValue
	 * 				Retrieval dataset
	 * @return
	 */
	public <T> double avrPrec(List<T> trueValue, List<T> retrValue){
		// number of positive item so far
		int posiCount = 0;
		double ap = 0.0;
		// the size of the list containing the first r items.
		int numItem = 0;
		for (T item : retrValue) {   
			if (trueValue.contains(item)) {
		       posiCount += 1;
		       ap += (posiCount / ((double)(numItem + 1)));
		    }
		    numItem = numItem + 1;
		}
		// posiCount doesn't increase or ap doesn't increase
		if ((ap == 0) || (posiCount == 0)) {
			return 0;
		}
		return ap / posiCount;       
	}
	
	/**
	 * Return the average value of precision(MAP)
	 * @param apList
	 * 			Input average precision list
	 * @return
	 */
	public <T> Double[] meanAvrPrec(List<Double[]> apList) {
		  Double map[] = {0.0,0.0,0.0};
		  Double length = new Double(apList.size());
		  for (Double[] ap : apList){
		    	for (int i = 0; i < ap.length; i++){
		    		map[i] += ap[i] / length;
		    	}
		    }
	    return map;
	}
	
	/**
	 * Return Geometric Mean Average Precision of the given average precision list(GMAP)
	 * @param apList
	 * 			Input average precision list
	 * @return
	 */
	public <T> Double[] geomMAP(List<Double[]> apList) {
		  Double gmap[] = {1.0,1.0,1.0};
		  Double epslon = 1e-3;
		  Double length = new Double(apList.size());
		  for (Double[] ap : apList){
		    	for (int i = 0; i < ap.length; i++)
		    		gmap[i] *= (ap[i] + epslon);
		  }
		  for (int i = 0; i < gmap.length; i++){
		    	gmap[i] = Math.pow(gmap[i], 1./length);
		    }
		  return gmap;
	}
}
