package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UniqueBgpPrefixOriginationQuestion extends Question {

   private static final String NODE_REGEX_VAR = "nodeRegex";

   private String _nodeRegex;

   public UniqueBgpPrefixOriginationQuestion() {
      super(QuestionType.UNIQUE_BGP_PREFIX_ORIGINATION);
      _nodeRegex = ".*";
   }

   @Override
   public boolean getDataPlane() {
      return false;
   }

   @Override
   public boolean getDifferential() {
      return false;
   }

   @JsonProperty(NODE_REGEX_VAR)
   public String getNodeRegex() {
      return _nodeRegex;
   }

   @Override
   public boolean getTraffic() {
      return false;
   }

   public void setNodeRegex(String nodeRegex) {
      _nodeRegex = nodeRegex;
   }

}
