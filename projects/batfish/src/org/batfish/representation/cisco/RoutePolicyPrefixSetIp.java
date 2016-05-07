package org.batfish.representation.cisco;

import java.io.Serializable;
import org.batfish.representation.Ip;
import org.batfish.util.SubRange;

public class RoutePolicyPrefixSetIp extends RoutePolicyPrefixSetInline {

	private Ip _addr;
	private SubRange _range;

	public RoutePolicyPrefixSetIp(Ip addr, Integer lower, Integer upper) { 
		super(lower, upper);
		_addr = addr;
	}

   private static final long serialVersionUID = 1L;

   public RoutePolicyPrefixType getPrefixType() {
   	return RoutePolicyPrefixType.IP;
   }

	public Ip getAddress() { return _addr; }
	public SubRange getRange() { return _range; }


}
