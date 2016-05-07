package org.batfish.representation.cisco;

import java.io.Serializable;

public class RoutePolicyCommunitySetName extends RoutePolicyCommunitySet {

	private String _name;

	public RoutePolicyCommunitySetName(String name) { this._name = name; }

   private static final long serialVersionUID = 1L;

   public RoutePolicyCommunityType getCommunityType() {
   	return RoutePolicyCommunityType.NAME;
   }

	public String getName() { return this._name; }


}
