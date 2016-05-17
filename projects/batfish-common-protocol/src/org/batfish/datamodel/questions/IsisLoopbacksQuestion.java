package org.batfish.datamodel.questions;

public class IsisLoopbacksQuestion extends Question {

   public IsisLoopbacksQuestion() {
      super(QuestionType.ISIS_LOOPBACKS);
   }

   @Override
   public boolean getDataPlane() {
      return false;
   }

   @Override
   public boolean getDifferential() {
      return false;
   }

   @Override
   public boolean getTraffic() {
      return false;
   }

}
