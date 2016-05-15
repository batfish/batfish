package org.batfish.datamodel.questions;

import java.util.Set;
import java.util.TreeSet;

public class BgpSessionCheckQuestion extends Question {

   private final Set<String> _foreignBgpGroups;

   public BgpSessionCheckQuestion() {
      super(QuestionType.BGP_SESSION_CHECK);
      _foreignBgpGroups = new TreeSet<String>();
   }

   @Override
   public boolean getDataPlane() {
      return false;
   }

   @Override
   public boolean getDifferential() {
      return false;
   }

   public Set<String> getForeignBgpGroups() {
      return _foreignBgpGroups;
   }

   @Override
   public boolean getTraffic() {
      return false;
   }

}
