package org.batfish.question;

import java.util.HashSet;
import java.util.Set;

import org.batfish.representation.Flow;

public class TracerouteQuestion extends Question {

   private Set<Flow> _flows;

   public TracerouteQuestion(QuestionParameters parameters) {
      super(QuestionType.TRACEROUTE, parameters);
      _flows = new HashSet<Flow>();
   }

   @Override
   public boolean getDataPlane() {
      return true;
   }

   @Override
   public boolean getDifferential() {
      return false;
   }

   public Set<Flow> getFlows() {
      return _flows;
   }

}
