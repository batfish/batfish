package org.batfish.datamodel.questions;

public class BgpAdvertisementsQuestion extends Question {

   private boolean _ebgp;

   private boolean _ibgp;

   private boolean _received;

   private boolean _sent;

   public BgpAdvertisementsQuestion() {
      super(QuestionType.BGP_ADVERTISEMENTS);
      _ebgp = true;
      _ibgp = true;
      _received = true;
      _sent = true;
   }

   @Override
   public boolean getDataPlane() {
      return true;
   }

   @Override
   public boolean getDifferential() {
      return false;
   }

   public boolean getEbgp() {
      return _ebgp;
   }

   public boolean getIbgp() {
      return _ibgp;
   }

   public boolean getReceived() {
      return _received;
   }

   public boolean getSent() {
      return _sent;
   }

   @Override
   public boolean getTraffic() {
      return false;
   }

   public void setEbgp(boolean ebgp) {
      _ebgp = ebgp;
   }

   public void setIbgp(boolean ibgp) {
      _ibgp = ibgp;
   }

   public void setReceived(boolean received) {
      _received = received;
   }

   public void setSent(boolean sent) {
      _sent = sent;
   }

}
