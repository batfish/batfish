package org.batfish.representation.cumulus;

import java.util.Map;

/** A shared interfaces for the two Cumulus configuration types -- concatenated, frr */
public interface CumulusNodeConfiguration {

  Map<String, IpCommunityList> getIpCommunityLists();

  Map<String, IpPrefixList> getIpPrefixLists();
}
