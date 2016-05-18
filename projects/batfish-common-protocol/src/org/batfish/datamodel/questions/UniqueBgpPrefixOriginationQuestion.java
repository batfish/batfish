package org.batfish.datamodel.questions;

public class UniqueBgpPrefixOriginationQuestion extends Question {

   public UniqueBgpPrefixOriginationQuestion() {
      super(QuestionType.UNIQUE_BGP_PREFIX_ORIGINATION);
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
