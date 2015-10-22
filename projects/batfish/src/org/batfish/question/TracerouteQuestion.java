package org.batfish.question;

import java.util.HashSet;
import java.util.Set;

import org.batfish.representation.Flow;

public class TracerouteQuestion extends Question {

   private Set<Flow> _flows;

   public TracerouteQuestion() {
      super(QuestionType.TRACEROUTE);
      _flows = new HashSet<Flow>();
   }

   public Set<Flow> getFlows() {
      return _flows;
   }

}
