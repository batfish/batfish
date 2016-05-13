package org.batfish.datamodel.questions;

import java.io.IOException;
import java.util.Iterator;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.NeighborType;
import org.batfish.datamodel.collections.NeighborTypeSet;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

   public NeighborsQuestion(QuestionParameters parameters) {
      this();
      setParameters(parameters);
   }

   @Override
   @JsonIgnore
   public boolean getDataPlane() {
      return false;
   }

   @Override
   @JsonIgnore
   public boolean getDifferential() {
      return false;
   }

   @JsonProperty(DST_NODE_REGEX_VAR)
   public String getDstNodeRegex() {
      return _dstNodeRegex;
   }

   @JsonProperty(NEIGHBOR_TYPE_VAR)
   public NeighborTypeSet getNeighborTypeSet() {
      return _neighborType;
   }

   @JsonProperty(SRC_NODE_REGEX_VAR)
   public String getSrcNodeRegex() {
      return _srcNodeRegex;
   }

   public void setDstNodeRegex(String regex) {
      _dstNodeRegex = regex;
   }

   public void setNeighborTypeSet(NeighborTypeSet neighborType) {
      _neighborType = neighborType;
   }

   public void setSrcNodeRegex(String regex) {
      _srcNodeRegex = regex;
   }

   @Override
   public void setJsonParameters(JSONObject parameters) {
      super.setJsonParameters(parameters);

      Iterator<?> paramKeys = parameters.keys();
      
      while ( paramKeys.hasNext()) {
         String paramKey = (String) paramKeys.next();

         try {
            switch (paramKey) {
            case DST_NODE_REGEX_VAR:
               setDstNodeRegex(parameters.getString(paramKey));
               break;
            case NEIGHBOR_TYPE_VAR:
               ObjectMapper mapper = new ObjectMapper();
               NeighborTypeSet nset = mapper.readValue(parameters.getString(paramKey), NeighborTypeSet.class);               
               setNeighborTypeSet(nset);               
               break;
            case SRC_NODE_REGEX_VAR:
               setSrcNodeRegex(parameters.getString(paramKey));
               break;
            default:
               throw new BatfishException("Unknown key in NodesQuestion: " + paramKey);
            }
         } catch (JSONException | IOException e) {
            throw new BatfishException("JSONException in parameters", e);
         }
      }
   }

   @Override
   public void setParameters(QuestionParameters parameters) {
      super.setParameters(parameters);
      if (parameters.getTypeBindings().get(NEIGHBOR_TYPE_VAR) == VariableType.NODE_TYPE) {
         setNeighborTypeSet(parameters.getNeighborTypeSet(NEIGHBOR_TYPE_VAR));
      }
      if (parameters.getTypeBindings().get(DST_NODE_REGEX_VAR) == VariableType.STRING) {
         setSrcNodeRegex(parameters.getString(DST_NODE_REGEX_VAR));
      }
      if (parameters.getTypeBindings().get(SRC_NODE_REGEX_VAR) == VariableType.STRING) {
         setDstNodeRegex(parameters.getString(SRC_NODE_REGEX_VAR));
      }
   }
}
