package org.batfish.datamodel.answers;

import java.util.Map;

import org.batfish.datamodel.Configuration;

public class NodesAnswerElement implements AnswerElement {

   private Map<String, Configuration> _nodes;

   public NodesAnswerElement(Map<String, Configuration> nodes) {
      _nodes = nodes;
   }

   public Map<String, Configuration> getAnswer() {
      return _nodes;
   }

}
