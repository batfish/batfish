package org.batfish.datamodel.questions;

import java.util.EnumSet;
import java.util.Set;

import org.batfish.datamodel.NeighborType;

public class NeighborsQuestion extends Question {

   private String _dstNodeRegex;

   private Set<NeighborType> _neighborType;

   private String _srcNodeRegex = ".*";

   public NeighborsQuestion() {
      super(QuestionType.NEIGHBORS);
      _dstNodeRegex = ".*";
      _neighborType = EnumSet.noneOf(NeighborType.class);
      _srcNodeRegex = ".*";
   }

   @Override
   public boolean getDataPlane() {
      return false;
   }

   @Override
   public boolean getDifferential() {
      return false;
   }

   public String getDstNodeRegex() {
      return _dstNodeRegex;
   }

   public Set<NeighborType> getNeighborType() {
      return _neighborType;
   }

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

   public void setSrcNodeRegex(String regex) {
      _srcNodeRegex = regex;
   }

}
