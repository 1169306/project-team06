package util;
/*
 *  Copyright 2014 Carnegie Mellon University
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import edu.stanford.nlp.process.Morphology;

/**
  * 
  * A thin wrapper around Stanford stemmer.
  * 
  * <p>It is called a stemmer, but, in fact,
  * the Stanford stemmer seems to be a lemmatizer.
  *  
  */

public class StanfordLemmatizer {
  private static Morphology morph = new Morphology();
  
  public static int MAX_WORD_LEN = 128;
  
  public static String stemWord(String w) {
    String t = null;
    try {
      if (w.length() <= MAX_WORD_LEN)
        t = morph.stem(w);
    } catch( java.lang.StackOverflowError e) {
      /*
       *  TODO should we ignore stack overflow here?
       *       so far it happens only for very long
       *       tokens, but how knows, there might
       *       be some other reasons as well. In that,
       *       if stemming failed, we can simply
       *       return the origina, unmodified, string. 
       */
      e.printStackTrace();
      System.err.println("Stack overflow for string: '" + w + "'");
      System.exit(1);
    }
    return t != null ? t:"";
  }
  
  public static String lemma(String w, String tag) {
    return morph.lemma(w, tag);
  }
  
  /**
    * Split the text into token (assuming tokens are separated by whitespaces), 
    * then stem each token separately.
    *  
    */
  public static String stemText(String text) {
    
    StringBuilder sb = new StringBuilder();
    for (String s: text.split("\\s+")) {
      sb.append(stemWord(s));
      sb.append(' ');
    }
    
    return sb.toString();
  }
  
  /*
   * Some stupid test.
   */
  public static void main(String args[]) {
    String origText = "tested tester tested gone indices indices super_testers"
                    + "had having been was were would could might";
    
    System.out.println(stemText(origText));
    System.out.println("==================");
    System.out.println(origText);
  }
}
