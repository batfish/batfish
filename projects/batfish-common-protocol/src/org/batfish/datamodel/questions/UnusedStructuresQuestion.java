package org.batfish.datamodel.questions;

public class UnusedStructuresQuestion extends Question {

   public UnusedStructuresQuestion() {
      super(QuestionType.UNUSED_STRUCTURES);
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
