package org.batfish.representation.cisco;

import java.io.Serializable;

public class RoutePolicyCommunitySetNumber extends RoutePolicyCommunitySet {

	private String number;

	public RoutePolicyCommunitySetNumber(String number) { this.number = number; }

   private static final long serialVersionUID = 1L;

   public RoutePolicyCommunityType getCommunityType() {
   	return RoutePolicyCommunityType.NUMBER;
   }

   public String getNumber() { return this.number; }

}
