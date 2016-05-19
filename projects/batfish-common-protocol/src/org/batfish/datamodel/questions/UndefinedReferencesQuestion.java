package org.batfish.datamodel.questions;

public class UndefinedReferencesQuestion extends Question {

   public UndefinedReferencesQuestion() {
      super(QuestionType.UNDEFINED_REFERENCES);
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
