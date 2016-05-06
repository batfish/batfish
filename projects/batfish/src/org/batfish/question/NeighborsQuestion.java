package org.batfish.question;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;

public class NeighborsQuestion extends Question {

   private Set<NeighborType> _neighborTypes;
   
   private Pattern _dstNodeRegex;

   private Pattern _srcNodeRegex;

   public NeighborsQuestion(QuestionParameters parameters) {
      super(QuestionType.NEIGHBORS, parameters);
      _neighborTypes = EnumSet.noneOf(NeighborType.class);
   }

   public Set<NeighborType> getNeighborTypes() {
      return _neighborTypes;
   }

   @Override
   public boolean getDataPlane() {
      return false;
   }

   @Override
   public boolean getDifferential() {
      return false;
   }

   public Pattern getDstNodeRegex() {
      return _dstNodeRegex;
   }

   public Pattern getSrcNodeRegex() {
	      return _srcNodeRegex;
   }

   public void setDstNodeRegex(String regex) {
      try {
         _dstNodeRegex = Pattern.compile(regex);
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               "Supplied regex for dst node is not a valid java regex: \""
                     + regex + "\"", e);
      }
   }

   public void setSrcNodeRegex(String regex) {
      try {
         _srcNodeRegex = Pattern.compile(regex);
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               "Supplied regex for src node is not a valid java regex: \""
                     + regex + "\"", e);
      }
   }
}
