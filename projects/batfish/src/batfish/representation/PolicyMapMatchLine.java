package batfish.representation;

public abstract class PolicyMapMatchLine {
   
   public abstract PolicyMapMatchType getType();

   public abstract String getIFString(int indentLevel);

   public abstract boolean sameParseTree(PolicyMapMatchLine line, String prefix);

}
