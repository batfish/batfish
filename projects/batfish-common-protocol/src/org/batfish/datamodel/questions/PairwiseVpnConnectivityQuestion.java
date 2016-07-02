package org.batfish.datamodel.questions;

import java.util.Iterator;

import org.batfish.common.BatfishException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PairwiseVpnConnectivityQuestion extends Question {

   private static final String NODE1_REGEX_VAR = "node1Regex";

   private static final String NODE2_REGEX_VAR = "node2Regex";

   private String _node1Regex;

   private String _node2Regex;

   public PairwiseVpnConnectivityQuestion() {
      super(QuestionType.PAIRWISE_VPN_CONNECTIVITY);
      _node1Regex = ".*";
      _node2Regex = ".*";
   }

   @Override
   public boolean getDataPlane() {
      return false;
   }

   @JsonProperty(NODE1_REGEX_VAR)
   public String getNode1Regex() {
      return _node1Regex;
   }

   @JsonProperty(NODE2_REGEX_VAR)
   public String getNode2Regex() {
      return _node2Regex;
   }

   @Override
   public boolean getTraffic() {
      return false;
   }

   @Override
   public void setJsonParameters(JSONObject parameters) {
      super.setJsonParameters(parameters);

      Iterator<?> paramKeys = parameters.keys();

      while (paramKeys.hasNext()) {
         String paramKey = (String) paramKeys.next();
         if (isBaseParamKey(paramKey)) {
            continue;
         }

         try {
            switch (paramKey) {
            case NODE1_REGEX_VAR:
               setNode1Regex(parameters.getString(paramKey));
               break;
            case NODE2_REGEX_VAR:
               setNode2Regex(parameters.getString(paramKey));
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

   public void setNode1Regex(String regex) {
      _node1Regex = regex;
   }

   public void setNode2Regex(String regex) {
      _node2Regex = regex;
   }

}
