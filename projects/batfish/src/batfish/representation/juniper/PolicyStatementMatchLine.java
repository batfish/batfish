package batfish.representation.juniper;

import java.io.Serializable;

public abstract class PolicyStatementMatchLine implements Serializable {

   private static final long serialVersionUID = 1L;

   
   public abstract MatchType getType();

}
