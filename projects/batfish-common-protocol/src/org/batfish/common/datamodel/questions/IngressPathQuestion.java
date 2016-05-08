package org.batfish.common.datamodel.questions;


public class IngressPathQuestion extends Question {

   public IngressPathQuestion(QuestionParameters parameters) {
      super(QuestionType.INGRESS_PATH, parameters);
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
