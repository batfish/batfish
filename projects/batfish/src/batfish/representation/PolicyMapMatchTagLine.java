package batfish.representation;

import java.util.Set;

public class PolicyMapMatchTagLine extends PolicyMapMatchLine {

   private static final long serialVersionUID = 1L;

   private Set<Integer> _tags;

   public PolicyMapMatchTagLine(Set<Integer> tags) {
      _tags = tags;
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
      boolean res = (line.getType() == PolicyMapMatchType.TAG)
            && (_tags.equals(((PolicyMapMatchTagLine) line)._tags));
      if (res == false) {
         System.out.println("PolicyMapMatchTagLine " + prefix);
      }
      return res;
   }

}
