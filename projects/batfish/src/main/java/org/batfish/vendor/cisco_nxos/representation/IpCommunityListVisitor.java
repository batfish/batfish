package org.batfish.vendor.cisco_nxos.representation;

/** A visitor of {@link IpCommunityList}. */
public interface IpCommunityListVisitor<T> {

  T visitIpCommunityListExpanded(IpCommunityListExpanded ipCommunityListExpanded);

  T visitIpCommunityListStandard(IpCommunityListStandard ipCommunityListStandard);
}
