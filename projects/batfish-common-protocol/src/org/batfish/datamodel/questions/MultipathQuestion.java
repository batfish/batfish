package org.batfish.datamodel.questions;

public class MultipathQuestion extends Question {

   public MultipathQuestion() {
      super(QuestionType.MULTIPATH);
   }

   @Override
   public boolean getDataPlane() {
      return true;
   }

   @Override
   public boolean getDifferential() {
      return false;
   }

   @Override
   public boolean getTraffic() {
      return true;
   }

}
