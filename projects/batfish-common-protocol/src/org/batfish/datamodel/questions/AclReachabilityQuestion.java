package org.batfish.datamodel.questions;

import java.io.Console;
import java.util.Iterator;

import org.batfish.common.BatfishException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AclReachabilityQuestion extends Question {

   private static final String ACL_NAME_REGEX_VAR = "aclNameRegex";

   private static final String NODE_REGEX_VAR = "nodeRegex";

   private String _aclNameRegex;

   private String _nodeRegex;

   public AclReachabilityQuestion() {
      super(QuestionType.ACL_REACHABILITY);
      _nodeRegex = ".*";
      _aclNameRegex = ".*";
   }

   @JsonProperty(ACL_NAME_REGEX_VAR)
   public String getAclNameRegex() {
      return _aclNameRegex;
   }

   @Override
   public boolean getDataPlane() {
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

   public void setAclNameRegex(String regex) {
      _aclNameRegex = regex;
   }

   @Override
   public void setJsonParameters(JSONObject parameters) {
      super.setJsonParameters(parameters);
      Iterator<?> paramKeys = parameters.keys();
      while (paramKeys.hasNext()) {
         String paramKey = (String) paramKeys.next();
         if (isBaseParamKey(paramKey))
            continue;         
         try {
            switch (paramKey) {
            case ACL_NAME_REGEX_VAR:
               setAclNameRegex(parameters.getString(paramKey));
               break;
            case NODE_REGEX_VAR:
               setNodeRegex(parameters.getString(paramKey));
               break;
            default:
               throw new BatfishException("Unknown key in "
                     + getClass().getSimpleName() + ": " + paramKey);
            }
         }
         catch (JSONException e) {
            throw new BatfishException("JSONException in parameters", e);
         }
      }
   }

   public void setNodeRegex(String regex) {
      _nodeRegex = regex;
   }

}
