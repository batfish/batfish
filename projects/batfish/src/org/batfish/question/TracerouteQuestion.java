package org.batfish.question;

import java.util.HashSet;
import java.util.Set;

import org.batfish.representation.FlowBuilder;

public class TracerouteQuestion extends Question {

   private Set<FlowBuilder> _flowBuilders;

   public TracerouteQuestion(QuestionParameters parameters) {
      super(QuestionType.TRACEROUTE, parameters);
      _flowBuilders = new HashSet<FlowBuilder>();
   }

   @Override
   public boolean getDataPlane() {
      return true;
   }

   @Override
   public boolean getDifferential() {
      return false;
   }

   public Set<FlowBuilder> getFlowBuilders() {
      return _flowBuilders;
   }

}
