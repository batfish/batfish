package org.batfish.datamodel.questions;

import java.util.Iterator;

import org.batfish.common.BatfishException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AclReachabilityQuestion extends Question {

   private static final String NODE_REGEX_VAR = "nodeRegex";
   private static final String ACL_NAME_REGEX_VAR = "aclNameRegex";

   private String _nodeRegex = ".*";
   private String _aclNameRegex = ".*";

   public AclReachabilityQuestion() {
      super(QuestionType.ACL_REACHABILITY);
   }

   public AclReachabilityQuestion(QuestionParameters parameters) {
      this();
      setParameters(parameters);
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

   @JsonProperty(ACL_NAME_REGEX_VAR)
   public String getNodeType() {
      return _aclNameRegex;
   }

   public void setNodeRegex(String regex) {
      _nodeRegex = regex;
   }

   public void setAclNameRegex(String regex) {
      _aclNameRegex = regex;
   }
 
   public void setJsonParameters(JSONObject parameters) {
      super.setJsonParameters(parameters);

      Iterator<?> paramKeys = parameters.keys();

      while ( paramKeys.hasNext()) {
         String paramKey = (String) paramKeys.next();

         try {
            switch (paramKey) {
            case NODE_REGEX_VAR:
               setNodeRegex(parameters.getString(paramKey));
               break;
            case ACL_NAME_REGEX_VAR:
               setAclNameRegex(parameters.getString(paramKey));
               break;
            default:
               throw new BatfishException("Unknown key in NodesQuestion: " + paramKey);
            }
         } catch (JSONException e) {
            throw new BatfishException("JSONException in parameters", e);
         }
      }
   }
}
