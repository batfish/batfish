package org.batfish.representation.cumulus_nclu;

/** A visitor of {@link IpCommunityList}. */
public interface IpCommunityListVisitor<T> {

  T visitIpCommunityListExpanded(IpCommunityListExpanded ipCommunityListExpanded);

  T visitIpCommunityListStandard(IpCommunityListStandard ipCommunityListStandard);
}
