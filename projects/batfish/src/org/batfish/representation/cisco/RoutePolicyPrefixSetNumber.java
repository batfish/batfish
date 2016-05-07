package org.batfish.representation.cisco;

import java.io.Serializable;

import org.batfish.representation.Prefix;
import org.batfish.util.SubRange;

public class RoutePolicyPrefixSetNumber extends RoutePolicyPrefixSetInline {

	private Prefix _prefix;

	public RoutePolicyPrefixSetNumber(Prefix prefix, Integer lower, Integer upper) {
		super(lower, upper);
		_prefix = prefix;
	}
	
   private static final long serialVersionUID = 1L;

   public RoutePolicyPrefixType getPrefixType() {
   	return RoutePolicyPrefixType.NUMBER;
   }

	public Prefix getPrefix() { return _prefix; }


}
