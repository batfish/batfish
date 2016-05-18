package org.batfish.datamodel.questions;

public class ReducedReachabilityQuestion extends Question {

   public ReducedReachabilityQuestion() {
      super(QuestionType.REDUCED_REACHABILITY);
   }

   @Override
   public boolean getDataPlane() {
      return true;
   }

   @Override
   public boolean getDifferential() {
      return true;
   }

   @Override
   public boolean getTraffic() {
      return true;
   }

}
