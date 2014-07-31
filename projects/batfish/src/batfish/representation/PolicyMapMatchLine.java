package batfish.representation;

import java.io.Serializable;

public abstract class PolicyMapMatchLine implements Serializable {

   private static final long serialVersionUID = 1L;

   public abstract String getIFString(int indentLevel);

   public abstract PolicyMapMatchType getType();

   public abstract boolean sameParseTree(PolicyMapMatchLine line, String prefix);

}
