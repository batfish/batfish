package org.batfish.representation.frr;

/** A visitor of {@link BgpCommunityList}. */
public interface BgpCommunityListVisitor<T> {

  T visitBgpCommunityListExpanded(BgpCommunityListExpanded bgpCommunityListExpanded);

  T visitBgpCommunityListStandard(BgpCommunityListStandard bgpCommunityListStandard);
}
