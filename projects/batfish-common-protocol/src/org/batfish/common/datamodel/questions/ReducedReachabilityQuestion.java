package org.batfish.common.datamodel.questions;

public class ReducedReachabilityQuestion extends Question {

   public ReducedReachabilityQuestion(QuestionParameters parameters) {
      super(QuestionType.REDUCED_REACHABILITY, parameters);
   }

   @Override
   public boolean getDataPlane() {
      return true;
   }

   @Override
   public boolean getDifferential() {
      return true;
   }

}
