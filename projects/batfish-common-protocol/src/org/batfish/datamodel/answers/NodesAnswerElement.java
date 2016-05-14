package org.batfish.datamodel.answers;

import java.util.Map;

import org.batfish.datamodel.Configuration;

public class NodesAnswerElement implements AnswerElement {

   private Map<String, Configuration> _answer;

   public NodesAnswerElement(Map<String, Configuration> nodes) {
      _answer = nodes;
   }

   public Map<String, Configuration> getAnswer() {
      return _answer;
   }

}
