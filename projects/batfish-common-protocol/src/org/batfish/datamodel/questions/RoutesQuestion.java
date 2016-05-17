package org.batfish.datamodel.questions;

public class RoutesQuestion extends Question {

   public RoutesQuestion() {
      super(QuestionType.ROUTES);
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
      return false;
   }

}
