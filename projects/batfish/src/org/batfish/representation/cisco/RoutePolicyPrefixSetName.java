package org.batfish.representation.cisco;

import java.io.Serializable;

public class RoutePolicyPrefixSetName extends RoutePolicyPrefixSet {

	private String _name;

	public RoutePolicyPrefixSetName(String name) { _name = name; }

   private static final long serialVersionUID = 1L;

   public RoutePolicyPrefixType getPrefixType() {
   	return RoutePolicyPrefixType.NAME;
   }

	public String getName() { return _name; }


}
