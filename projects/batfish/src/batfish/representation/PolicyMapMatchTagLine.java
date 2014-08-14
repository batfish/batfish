package batfish.representation;

import java.util.Set;

public class PolicyMapMatchTagLine extends PolicyMapMatchLine {

   private Set<Integer> _tags;

   public PolicyMapMatchTagLine(Set<Integer> tags) {
      _tags = tags;
   }

   @Override
   public String getIFString(int indentLevel) {
      // TODO implement properly
      return "PolicyMapMatchTagLine ???\n";
   }

   public Set<Integer> getTags() {
      return _tags;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.TAG;
   }

   @Override
   public boolean sameParseTree(PolicyMapMatchLine line, String prefix) {
      // TODO: implement properly
      return true;
   }

}
