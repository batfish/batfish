package org.batfish.datamodel.questions;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.common.BatfishException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BgpSessionCheckQuestion extends Question {

   private static final String FOREIGN_BGP_GROUPS_VAR = "foreignBgpGroups";

   private static final String NODE1_REGEX_VAR = "node1Regex";

   private static final String NODE2_REGEX_VAR = "node2Regex";

   private Set<String> _foreignBgpGroups;

   private String _node1Regex;

   private String _node2Regex;

   public BgpSessionCheckQuestion() {
      super(QuestionType.BGP_SESSION_CHECK);
      _foreignBgpGroups = new TreeSet<String>();
      _node1Regex = ".*";
      _node2Regex = ".*";
   }

   @Override
   public boolean getDataPlane() {
      return false;
   }

   @JsonProperty(FOREIGN_BGP_GROUPS_VAR)
   public Set<String> getForeignBgpGroups() {
      return _foreignBgpGroups;
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

   public void setForeignBgpGroups(Set<String> foreignBgpGroups) {
      _foreignBgpGroups = foreignBgpGroups;
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
            case FOREIGN_BGP_GROUPS_VAR:
               setForeignBgpGroups(new ObjectMapper().<Set<String>> readValue(
                     parameters.getString(paramKey),
                     new TypeReference<Set<String>>() {
                     }));
               break;
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
         catch (JSONException | IOException e) {
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
