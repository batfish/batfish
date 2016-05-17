package org.batfish.datamodel.questions;

public class PairwiseVpnConnectivityQuestion extends Question {

   public PairwiseVpnConnectivityQuestion() {
      super(QuestionType.PAIRWISE_VPN_CONNECTIVITY);
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
