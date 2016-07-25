package org.batfish.datamodel.questions;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.NeighborType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NeighborsQuestion extends Question {

   private static final String NEIGHBOR_TYPE_VAR = "neighborType";

   private static final String NODE1_REGEX_VAR = "node1Regex";

   private static final String NODE2_REGEX_VAR = "node2Regex";

   private Set<NeighborType> _neighborTypes;

   private String _node1Regex;

   private String _node2Regex;

   public NeighborsQuestion() {
      super(QuestionType.NEIGHBORS);
      _node1Regex = ".*";
      _node2Regex = ".*";
      _neighborTypes = EnumSet.noneOf(NeighborType.class);
   }

   @Override
   public boolean getDataPlane() {
      return false;
   }

   @JsonProperty(NEIGHBOR_TYPE_VAR)
   public Set<NeighborType> getNeighborTypes() {
      return _neighborTypes;
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
   public String prettyPrint() {
      try {
         String retString = String.format("neighbors %s%s=%s | %s=%s | %s=%s",
               prettyPrintBase(), NODE1_REGEX_VAR, _node1Regex,
               NODE2_REGEX_VAR, _node2Regex, NEIGHBOR_TYPE_VAR,
               _neighborTypes.toString());
         return retString;
      }
      catch (Exception e) {
         try {
            return "Pretty printing failed. Printing Json\n" + toJsonString();
         }
         catch (JsonProcessingException e1) {
            throw new BatfishException("Both pretty and json printing failed\n");
         }
      }

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
            case NEIGHBOR_TYPE_VAR:
               setNeighborTypes(new ObjectMapper()
                     .<Set<NeighborType>> readValue(
                           parameters.getString(paramKey),
                           new TypeReference<Set<NeighborType>>() {
                           }));
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

   public void setNeighborTypes(Set<NeighborType> neighborType) {
      _neighborTypes = neighborType;
   }

   public void setNode1Regex(String regex) {
      _node1Regex = regex;
   }

   public void setNode2Regex(String regex) {
      _node2Regex = regex;
   }

}
