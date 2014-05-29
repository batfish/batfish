package batfish.representation.juniper;

public class ASPathAccessListLine {

   private String _regex;

   public ASPathAccessListLine(String regex) {
      _regex = regex;
   }

   public String getRegex() {
      return _regex;
   }

}
