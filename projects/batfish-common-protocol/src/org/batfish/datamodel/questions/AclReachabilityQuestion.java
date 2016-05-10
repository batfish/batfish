package org.batfish.datamodel.questions;

public class AclReachabilityQuestion extends Question {

   public AclReachabilityQuestion(QuestionParameters parameters) {
      super(QuestionType.ACL_REACHABILITY, parameters);
   }

   @Override
   public boolean getDataPlane() {
      return false;
   }

   @Override
   public boolean getDifferential() {
      return false;
   }

}
