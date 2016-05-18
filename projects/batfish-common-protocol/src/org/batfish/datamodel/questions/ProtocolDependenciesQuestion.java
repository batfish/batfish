package org.batfish.datamodel.questions;

public class ProtocolDependenciesQuestion extends Question {

   public ProtocolDependenciesQuestion() {
      super(QuestionType.PROTOCOL_DEPENDENCIES);
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
