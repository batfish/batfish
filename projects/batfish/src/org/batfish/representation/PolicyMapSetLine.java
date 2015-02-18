package org.batfish.representation;

import java.io.Serializable;

public abstract class PolicyMapSetLine implements Serializable {

   private static final long serialVersionUID = 1L;

   public abstract PolicyMapSetType getType();

}
