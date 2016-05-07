package org.batfish.representation.cisco;

import java.io.Serializable;

public class RoutePolicyDeleteCommunityStatement extends RoutePolicyDeleteStatement {

	private boolean negated;
	private RoutePolicyCommunitySet commset;

	public RoutePolicyDeleteCommunityStatement(boolean negated, RoutePolicyCommunitySet commset) {
		this.negated = negated;
		this.commset = commset;
	}

   private static final long serialVersionUID = 1L;

   public RoutePolicyDeleteType getDeleteType() { return RoutePolicyDeleteType.COMMUNITY; }

   public boolean getNegated() { return negated; }

   public RoutePolicyCommunitySet getCommSet() { return commset; }

}
