package org.batfish.datamodel.answers;

import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RunAnalysisAnswerElement implements AnswerElement {

   private static final String ANSWERS_VAR = "answers";

   private SortedMap<String, Answer> _answers;

   @JsonCreator
   public RunAnalysisAnswerElement() {
      _answers = new TreeMap<>();
   }

   @JsonProperty(ANSWERS_VAR)
   public SortedMap<String, Answer> getAnswers() {
      return _answers;
   }

   @JsonProperty(ANSWERS_VAR)
   public void setAnswers(SortedMap<String, Answer> answers) {
      _answers = answers;
   }

}
