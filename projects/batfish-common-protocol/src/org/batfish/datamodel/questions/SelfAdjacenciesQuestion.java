package org.batfish.datamodel.questions;

public class SelfAdjacenciesQuestion extends Question {

   public SelfAdjacenciesQuestion() {
      super(QuestionType.SELF_ADJACENCIES);
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
