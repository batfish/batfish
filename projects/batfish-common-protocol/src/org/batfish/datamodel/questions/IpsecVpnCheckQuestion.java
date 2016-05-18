package org.batfish.datamodel.questions;

public class IpsecVpnCheckQuestion extends Question {

   public IpsecVpnCheckQuestion() {
      super(QuestionType.IPSEC_VPN_CHECK);
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
