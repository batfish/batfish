package org.batfish.datamodel.questions;

import java.util.Iterator;

import org.batfish.common.BatfishException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UniqueIpAssignmentsQuestion extends Question {

   private static final String DIFF_VAR = "diff";

   private static final String NODE_REGEX_VAR = "nodeRegex";

   private static final String VERBOSE_VAR = "verbose";

   private boolean _differential;

   private String _nodeRegex;

   private boolean _verbose;

   public UniqueIpAssignmentsQuestion() {
      super(QuestionType.UNIQUE_IP_ASSIGNMENTS);
      _nodeRegex = ".*";
      _differential = false;
   }

   @Override
   public boolean getDataPlane() {
      return false;
   }

   @Override
   @JsonIgnore(false)
   @JsonProperty(DIFF_VAR)
   public boolean getDifferential() {
      return _differential;
   }

   @JsonProperty(NODE_REGEX_VAR)
   public String getNodeRegex() {
      return _nodeRegex;
   }

   @Override
   public boolean getTraffic() {
      return false;
   }

   @JsonProperty(VERBOSE_VAR)
   public boolean getVerbose() {
      return _verbose;
   }

   private void setDifferential(boolean differential) {
      _differential = differential;
   }

   @Override
   public void setJsonParameters(JSONObject parameters) {
      super.setJsonParameters(parameters);

      Iterator<?> paramKeys = parameters.keys();

      while (paramKeys.hasNext()) {
         String paramKey = (String) paramKeys.next();

         try {
            switch (paramKey) {
            case DIFF_VAR:
               setDifferential(parameters.getBoolean(paramKey));
               break;
            case NODE_REGEX_VAR:
               setNodeRegex(parameters.getString(paramKey));
               break;
            case VERBOSE_VAR:
               setVerbose(parameters.getBoolean(paramKey));
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

   public void setNodeRegex(String nodeRegex) {
      _nodeRegex = nodeRegex;
   }

   private void setVerbose(boolean verbose) {
      _verbose = verbose;
   }

}
