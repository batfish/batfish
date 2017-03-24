package org.batfish.datamodel.answers;

import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ProblemsAnswerElement implements AnswerElement {

   private static final String PROBLEMS_VAR = "problems";

   private SortedMap<String, Problem> _problems;

   public ProblemsAnswerElement() {
      _problems = new TreeMap<>();
   }

   @JsonProperty(PROBLEMS_VAR)
   public SortedMap<String, Problem> getProblems() {
      return _problems;
   }

   @JsonProperty(PROBLEMS_VAR)
   public void setProblems(SortedMap<String, Problem> problems) {
      _problems = problems;
   }

}
