package org.batfish.datamodel.questions;

public class UniqueIpAssignmentsQuestion extends Question {

   public UniqueIpAssignmentsQuestion() {
      super(QuestionType.UNIQUE_IP_ASSIGNMENTS);
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
