package org.batfish.client.answer;

import java.util.SortedSet;
import java.util.TreeSet;

import org.batfish.datamodel.answers.AnswerElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LoadQuestionAnswerElement implements AnswerElement {

   private static final String ADDED_VAR = "added";

   private static final String NUM_LOADED_VAR = "numLoaded";

   private static final String REPLACED_VAR = "replaced";

   private SortedSet<String> _added;

   private int _numLoaded;

   private SortedSet<String> _replaced;

   @JsonCreator
   public LoadQuestionAnswerElement() {
      _added = new TreeSet<>();
      _replaced = new TreeSet<>();
   }

   @JsonProperty(ADDED_VAR)
   public SortedSet<String> getAdded() {
      return _added;
   }

   @JsonProperty(NUM_LOADED_VAR)
   public int getNumLoaded() {
      return _numLoaded;
   }

   @JsonProperty(REPLACED_VAR)
   public SortedSet<String> getReplaced() {
      return _replaced;
   }

   @Override
   public String prettyPrint() {
      if (_numLoaded == 0) {
         return "WARNING: no question .json files found in provided path\n";
      }
      else {
         StringBuilder sb = new StringBuilder();
         sb.append("Loaded " + _numLoaded + " questions");
         if (!_replaced.isEmpty()) {
            sb.append(" (Added:" + _added.size() + " Replaced:"
                  + _replaced.size() + ")\n");
         }
         return sb.toString();
      }
   }

   @JsonProperty(ADDED_VAR)
   public void setAdded(SortedSet<String> added) {
      _added = added;
   }

   @JsonProperty(NUM_LOADED_VAR)
   public void setNumLoaded(int numLoaded) {
      _numLoaded = numLoaded;
   }

   @JsonProperty(REPLACED_VAR)
   public void setReplaced(SortedSet<String> replaced) {
      _replaced = replaced;
   }

}
