package batfish.representation.juniper;

import java.io.Serializable;

public abstract class PolicyStatement_MatchLine implements Serializable {

   private static final long serialVersionUID = 1L;

   public abstract PolicyStatement_MatchType getType();

}
