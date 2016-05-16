package org.batfish.datamodel.questions;

public class AclReachabilityQuestion extends Question {

   private String _nodeRegex;
   private String _aclNameRegex;

   public AclReachabilityQuestion() {
      super(QuestionType.ACL_REACHABILITY);
      _nodeRegex = ".*";
      _aclNameRegex = ".*";
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

   public String getNodeType() {
      return _aclNameRegex;
   }

   @Override
   public boolean getTraffic() {
      return false;
   }

   public void setAclNameRegex(String regex) {
      _aclNameRegex = regex;
   }

   public void setNodeRegex(String regex) {
      _nodeRegex = regex;
   }
}
