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

   private Set<String> _foreignBgpGroups;

   public BgpSessionCheckQuestion() {
      super(QuestionType.BGP_SESSION_CHECK);
      _foreignBgpGroups = new TreeSet<String>();
   }

   @Override
   public boolean getDataPlane() {
      return false;
   }

   @Override
   public boolean getDifferential() {
      return false;
   }

   @JsonProperty(FOREIGN_BGP_GROUPS_VAR)
   public Set<String> getForeignBgpGroups() {
      return _foreignBgpGroups;
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

         try {
            switch (paramKey) {
            case FOREIGN_BGP_GROUPS_VAR:
               setForeignBgpGroups(new ObjectMapper().<Set<String>> readValue(
                     parameters.getString(paramKey),
                     new TypeReference<Set<String>>() {
                     }));
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
}
