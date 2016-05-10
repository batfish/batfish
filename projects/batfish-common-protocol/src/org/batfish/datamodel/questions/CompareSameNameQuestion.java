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
   
   public CompareSameNameQuestion(QuestionParameters parameters) {
      this();
      setParameters(parameters);
   }

   @Override
   @JsonIgnore
   public boolean getDataPlane() {
      return false;
   }

   @Override
   @JsonIgnore
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

   public void setNodeRegex(String regex) {
         _nodeRegex = regex;
   }
   
   public void setNamedStructType(NamedStructType nType) {
      _namedStructType = nType;
   }
   
   @Override
   public void setParameters(QuestionParameters parameters) {
      super.setParameters(parameters);
      if (parameters.getTypeBindings().get(NODE_REGEX_VAR) == VariableType.STRING) {
         setNodeRegex(parameters.getString(NODE_REGEX_VAR));
      }
      if (parameters.getTypeBindings().get(NAMED_STRUCT_TYPE_VAR) == VariableType.NODE_TYPE) {
         setNamedStructType(parameters.getNamedStructType(NAMED_STRUCT_TYPE_VAR));
      }
   }
}
