package batfish.representation;

public abstract class PolicyMapSetLine {

	public abstract PolicyMapSetType getType();

	public abstract String getIFString(int indentLevel); 

   public abstract boolean sameParseTree(PolicyMapSetLine line, String prefix);

}
