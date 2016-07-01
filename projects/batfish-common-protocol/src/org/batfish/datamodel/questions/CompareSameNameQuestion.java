package org.batfish.datamodel.questions;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.NamedStructType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class CompareSameNameQuestion extends Question {

   private static final String NAMED_STRUCT_TYPE_VAR = "namedStructType";

   private static final String NODE_REGEX_VAR = "nodeRegex";

   private Set<NamedStructType> _namedStructTypes;

   private String _nodeRegex;

   public CompareSameNameQuestion() {
      super(QuestionType.COMPARE_SAME_NAME);
      _namedStructTypes = EnumSet.noneOf(NamedStructType.class);
      _nodeRegex = ".*";
   }

   @Override
   public boolean getDataPlane() {
      return false;
   }

   @JsonProperty(NAMED_STRUCT_TYPE_VAR)
   public Set<NamedStructType> getNamedStructTypes() {
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
         if (isBaseParamKey(paramKey))
            continue;         

         try {
            switch (paramKey) {
            case NAMED_STRUCT_TYPE_VAR:
               setNamedStructTypes(new ObjectMapper()
                     .<Set<NamedStructType>> readValue(
                           parameters.getString(paramKey),
                           new TypeReference<Set<NamedStructType>>() {
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

   public void setNamedStructTypes(Set<NamedStructType> nType) {
      _namedStructTypes = nType;
   }

   public void setNodeRegex(String regex) {
      _nodeRegex = regex;
   }

}
