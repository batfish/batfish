package org.batfish.datamodel.questions;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.NeighborType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NeighborsQuestion extends Question {

   private static final String DST_NODE_REGEX_VAR = "dstNodeRegex";
   private static final String NEIGHBOR_TYPE_VAR = "neighborType";
   private static final String SRC_NODE_REGEX_VAR = "srcNodeRegex";

   private String _dstNodeRegex = ".*";
   private NeighborType _neighborType = NeighborType.PHYSICAL;   
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
   public NeighborType getNeighborType() {
      return _neighborType;
   }

   @JsonProperty(SRC_NODE_REGEX_VAR)
   public String getSrcNodeRegex() {
      return _srcNodeRegex;
   }

   public void setDstNodeRegex(String regex) {
      _dstNodeRegex = regex;
   }

   public void setNeighborType(NeighborType neighborType) {
      _neighborType = neighborType;
   }

   public void setSrcNodeRegex(String regex) {
      _srcNodeRegex = regex;
   }

   @Override
   public void setParameters(QuestionParameters parameters) {
      super.setParameters(parameters);
      if (parameters.getTypeBindings().get(NEIGHBOR_TYPE_VAR) == VariableType.NODE_TYPE) {
         setNeighborType(parameters.getNeighborType(NEIGHBOR_TYPE_VAR));
      }
      if (parameters.getTypeBindings().get(DST_NODE_REGEX_VAR) == VariableType.STRING) {
         setSrcNodeRegex(parameters.getString(DST_NODE_REGEX_VAR));
      }
      if (parameters.getTypeBindings().get(SRC_NODE_REGEX_VAR) == VariableType.STRING) {
         setDstNodeRegex(parameters.getString(SRC_NODE_REGEX_VAR));
      }
   }
}
