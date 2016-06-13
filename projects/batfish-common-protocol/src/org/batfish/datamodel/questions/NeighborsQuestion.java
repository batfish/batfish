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

   @Override
   public boolean getDifferential() {
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
   public void setJsonParameters(JSONObject parameters) {
      super.setJsonParameters(parameters);

      Iterator<?> paramKeys = parameters.keys();

      while (paramKeys.hasNext()) {
         String paramKey = (String) paramKeys.next();

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
