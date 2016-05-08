package org.batfish.common.datamodel.questions;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
import org.batfish.common.datamodel.NodeType;

public class NodesQuestion extends Question {

   private Set<NodeType> _nodeTypes;
   
   private Pattern _nodeRegex;

   public NodesQuestion(QuestionParameters parameters) {
      super(QuestionType.NODES, parameters);
      _nodeTypes = EnumSet.noneOf(NodeType.class);
   }

   public Set<NodeType> getNodeTypes() {
      return _nodeTypes;
   }

   @Override
   public boolean getDataPlane() {
      return false;
   }

   @Override
   public boolean getDifferential() {
      return false;
   }

   public Pattern getNodeRegex() {
      return _nodeRegex;
   }

   public void setNodeRegex(String regex) {
      try {
         _nodeRegex = Pattern.compile(regex);
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               "Supplied regex for nodes is not a valid java regex: \""
                     + regex + "\"", e);
      }
   }
}
