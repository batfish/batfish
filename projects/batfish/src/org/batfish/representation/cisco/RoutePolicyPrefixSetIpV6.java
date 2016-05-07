package org.batfish.representation.cisco;

import java.io.Serializable;
import org.batfish.representation.Ip6;
import org.batfish.util.SubRange;

public class RoutePolicyPrefixSetIpV6 extends RoutePolicyPrefixSetInline {

	private Ip6 _addr;
	private SubRange _range;

	public RoutePolicyPrefixSetIpV6(Ip6 addr, Integer lower, Integer upper) { 
		super(lower, upper);
		_addr = addr;
	}

   private static final long serialVersionUID = 1L;

   public RoutePolicyPrefixType getPrefixType() {
   	return RoutePolicyPrefixType.IP_V6;
   }

	public Ip6 getAddress() { return _addr; }
	public SubRange getRange() { return _range; }


}
