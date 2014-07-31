package batfish.representation;

import java.io.Serializable;

public abstract class PolicyMapSetLine implements Serializable {

   private static final long serialVersionUID = 1L;

   public abstract String getIFString(int indentLevel);

   public abstract PolicyMapSetType getType();

   public abstract boolean sameParseTree(PolicyMapSetLine line, String prefix);

}
