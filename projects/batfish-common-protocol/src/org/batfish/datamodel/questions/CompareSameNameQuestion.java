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

public final class CompareSameNameQuestion extends Question {

   private static final String NAMED_STRUCT_TYPE_VAR = "namedStructType";

   private static final String NODE_REGEX_VAR = "nodeRegex";

   private Set<String> _namedStructTypes;

   private String _nodeRegex;

   public CompareSameNameQuestion() {
      super(QuestionType.COMPARE_SAME_NAME);
      _namedStructTypes = new TreeSet<String>();
      _nodeRegex = ".*";
   }

   @Override
   public boolean getDataPlane() {
      return false;
   }

   @JsonProperty(NAMED_STRUCT_TYPE_VAR)
   public Set<String> getNamedStructTypes() {
      return _namedStructTypes;
   }

   @JsonProperty(NODE_REGEX_VAR)
   public String getNodeRegex() {
      return _nodeRegex;
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
            case NAMED_STRUCT_TYPE_VAR:
               setNamedStructTypes(new ObjectMapper().<Set<String>> readValue(
                     parameters.getString(paramKey),
                     new TypeReference<Set<String>>() {
                     }));
               break;
            case NODE_REGEX_VAR:
               setNodeRegex(parameters.getString(paramKey));
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

   public void setNamedStructTypes(Set<String> namedStructTypes) {
      _namedStructTypes = namedStructTypes;
   }

   public void setNodeRegex(String regex) {
      _nodeRegex = regex;
   }

}
