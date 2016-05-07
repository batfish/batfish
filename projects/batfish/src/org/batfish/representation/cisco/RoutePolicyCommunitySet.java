package org.batfish.representation.cisco;

import java.io.Serializable;

public abstract class RoutePolicyCommunitySet implements Serializable {

   private static final long serialVersionUID = 1L;

   public abstract RoutePolicyCommunityType getCommunityType();

}
