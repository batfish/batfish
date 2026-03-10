package org.batfish.datamodel.bgp.community;

/** A visitor of {@link Community}. */
public interface CommunityVisitor<T> {

  T visitExtendedCommunity(ExtendedCommunity extendedCommunity);

  T visitLargeCommunity(LargeCommunity largeCommunity);

  T visitStandardCommunity(StandardCommunity standardCommunity);
}
