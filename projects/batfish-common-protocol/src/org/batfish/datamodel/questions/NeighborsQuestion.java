package org.batfish.datamodel.questions;

import java.io.IOException;
import java.util.Iterator;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.NeighborType;
import org.batfish.datamodel.collections.NeighborTypeSet;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NeighborsQuestion extends Question {

   private static final String DST_NODE_REGEX_VAR = "dstNodeRegex";
   private static final String NEIGHBOR_TYPE_VAR = "neighborType";
   private static final String SRC_NODE_REGEX_VAR = "srcNodeRegex";

   private String _dstNodeRegex = ".*";
   private NeighborTypeSet _neighborType = new NeighborTypeSet(NeighborType.ANY);
   private String _srcNodeRegex = ".*";

   public NeighborsQuestion() {
      super(QuestionType.NEIGHBORS);
   }

   @Override
   public boolean getDataPlane() {
      return false;
   }

   @Override
   public boolean getDifferential() {
      return false;
   }

   @JsonProperty(DST_NODE_REGEX_VAR)
   public String getDstNodeRegex() {
      return _dstNodeRegex;
   }

   @JsonProperty(NEIGHBOR_TYPE_VAR)
   public NeighborTypeSet getNeighborTypes() {
      return _neighborType;
   }

   @JsonProperty(SRC_NODE_REGEX_VAR)
   public String getSrcNodeRegex() {
      return _srcNodeRegex;
   }

   @Override
   public boolean getTraffic() {
      return false;
   }

   public void setDstNodeRegex(String regex) {
      _dstNodeRegex = regex;
   }

   @Override
   public void setJsonParameters(JSONObject parameters) {
      super.setJsonParameters(parameters);

      Iterator<?> paramKeys = parameters.keys();

      while (paramKeys.hasNext()) {
         String paramKey = (String) paramKeys.next();

         try {
            switch (paramKey) {
            case DST_NODE_REGEX_VAR:
               setDstNodeRegex(parameters.getString(paramKey));
               break;
            case NEIGHBOR_TYPE_VAR:
               ObjectMapper mapper = new ObjectMapper();
               NeighborTypeSet nset = mapper.readValue(
                     parameters.getString(paramKey), NeighborTypeSet.class);
               setNeighborTypeSet(nset);
               break;
            case SRC_NODE_REGEX_VAR:
               setSrcNodeRegex(parameters.getString(paramKey));
               break;
            default:
               throw new BatfishException("Unknown key in NodesQuestion: "
                     + paramKey);
            }
         }
         catch (JSONException | IOException e) {
            throw new BatfishException("JSONException in parameters", e);
         }
      }
   }

   public void setNeighborTypeSet(NeighborTypeSet neighborType) {
      _neighborType = neighborType;
   }

   public void setSrcNodeRegex(String regex) {
      _srcNodeRegex = regex;
   }

}
