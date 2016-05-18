package org.batfish.datamodel.questions;

public class OspfLoopbacksQuestion extends Question {

   public OspfLoopbacksQuestion() {
      super(QuestionType.OSPF_LOOPBACKS);
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
