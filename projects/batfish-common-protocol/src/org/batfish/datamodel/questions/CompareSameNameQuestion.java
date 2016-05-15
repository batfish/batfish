package org.batfish.datamodel.questions;

import org.batfish.datamodel.NamedStructType;

public final class CompareSameNameQuestion extends Question {

   private NamedStructType _namedStructType;
   private String _nodeRegex;

   public CompareSameNameQuestion() {
      super(QuestionType.COMPARE_SAME_NAME);
      _namedStructType = NamedStructType.ANY;
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

   public String getNodeRegex() {
      return _nodeRegex;
   }

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
