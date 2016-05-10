package org.batfish.common.datamodel.questions;

import java.util.Set;

import org.batfish.common.BatfishException;
import org.batfish.common.collections.NodeInterfacePair;
import org.batfish.common.datamodel.NodeType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NodesQuestion extends Question {

   private static final String NODE_REGEX_VAR = "nodeRegex";
   private static final String NODE_TYPE_VAR = "nodeType";
   
   private NodeType _nodeType = NodeType.ANY;   
   private String _nodeRegex = ".*";

   public NodesQuestion() {
      super(QuestionType.NODES);
   }
   
   public NodesQuestion(QuestionParameters parameters) {
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

   @JsonProperty(NODE_REGEX_VAR)
   public String getNodeRegex() {
      return _nodeRegex;
   }

   @JsonProperty(NODE_TYPE_VAR)
   public NodeType getNodeType() {
      return _nodeType;
   }

   public void setNodeRegex(String regex) {
         _nodeRegex = regex;
   }
   
   public void setNodeType(NodeType nType) {
      _nodeType = nType;
   }
   
   @Override
   public void setParameters(QuestionParameters parameters) {
      super.setParameters(parameters);
      if (parameters.getTypeBindings().get(NODE_REGEX_VAR) == VariableType.STRING) {
         setNodeRegex(parameters.getString(NODE_REGEX_VAR));
      }
      if (parameters.getTypeBindings().get(NODE_TYPE_VAR) == VariableType.NODE_TYPE) {
         setNodeType(parameters.getNodeType(NODE_TYPE_VAR));
      }
   }
}
