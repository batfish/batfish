package org.batfish.datamodel.questions;

import org.batfish.datamodel.NamedStructType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class CompareSameNameQuestion extends Question {

   private static final String NODE_REGEX_VAR = "nodeRegex";
   private static final String NAMED_STRUCT_TYPE_VAR = "namedStructType";

   private NamedStructType _namedStructType = NamedStructType.ANY;
   private String _nodeRegex = ".*";

   public CompareSameNameQuestion() {
      super(QuestionType.COMPARE_SAME_NAME);
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

   @JsonProperty(NAMED_STRUCT_TYPE_VAR)
   public NamedStructType getNodeType() {
      return _namedStructType;
   }

   @Override
   public boolean getTraffic() {
      return false;
   }

   public void setNamedStructType(NamedStructType nType) {
      _namedStructType = nType;
   }

   public void setNodeRegex(String regex) {
      _nodeRegex = regex;
   }

}
