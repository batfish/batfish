package org.batfish.representation.cisco;

import java.io.Serializable;
import org.batfish.representation.Prefix6;
import org.batfish.util.SubRange;

public class RoutePolicyPrefixSetNumberV6 extends RoutePolicyPrefixSetInline {

	private Prefix6 _prefix;

	public RoutePolicyPrefixSetNumberV6(Prefix6 prefix, Integer lower, Integer upper) {
		super(lower, upper);
		_prefix = prefix;
	}

   private static final long serialVersionUID = 1L;

   public RoutePolicyPrefixType getPrefixType() {
   	return RoutePolicyPrefixType.NUMBER_V6;
   }

	public Prefix6 getPrefix() { return _prefix; }


}
