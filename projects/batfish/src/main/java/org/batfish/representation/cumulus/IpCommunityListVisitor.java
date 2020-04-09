package org.batfish.representation.cumulus;

/** A visitor of {@link IpCommunityList}. */
public interface IpCommunityListVisitor<T> {

  T visitIpCommunityListExpanded(IpCommunityListExpanded ipCommunityListExpanded);

  T visitIpCommunityListStandard(IpCommunityListStandard ipCommunityListStandard);
}
