package org.batfish.representation;

import java.io.Serializable;

public abstract class PolicyMapMatchLine implements Serializable {

   private static final long serialVersionUID = 1L;

   public abstract PolicyMapMatchType getType();

}
