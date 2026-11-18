package org.batfish.datamodel;

import static org.batfish.datamodel.AclIpSpace.rejecting;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

/** Test for {@link AbstractIpSpaceContainsIp}. */
class AbstractIpSpaceContainsIpTest {
  private static final Ip IP1 = Ip.parse("1.1.1.1");
  private static final Ip IP2 = Ip.parse("2.2.2.2");

  private static AbstractIpSpaceContainsIp containsIp(Ip ip) {
    return new AbstractIpSpaceContainsIp(ip) {
      @Override
      public Boolean visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Test
  void testVisitAclIpSpace() {
    AclIpSpace ipSpace =
        (AclIpSpace) rejecting(IP2.toIpSpace()).thenPermitting(UniverseIpSpace.INSTANCE).build();
    assertTrue(containsIp(IP1).visitAclIpSpace(ipSpace));
    assertFalse(containsIp(IP2).visitAclIpSpace(ipSpace));
  }

  @Test
  void testVisitEmptyIpSpace() {
    assertFalse(containsIp(IP1).visitEmptyIpSpace(EmptyIpSpace.INSTANCE));
  }

  @Test
  void testVisitIpIpSpace() {
    assertTrue(containsIp(IP1).visitIpIpSpace(IP1.toIpSpace()));
    assertFalse(containsIp(IP2).visitIpIpSpace(IP1.toIpSpace()));
  }

  @Test
  void testVisitIpWildcardIpSpace() {
    IpWildcardIpSpace ipSpace = IpWildcard.create(IP1).toIpSpace();
    assertTrue(containsIp(IP1).visitIpWildcardIpSpace(ipSpace));
    assertFalse(containsIp(IP2).visitIpWildcardIpSpace(ipSpace));
  }

  @Test
  void testVisitIpWildcardSetIpSpace() {
    IpWildcardSetIpSpace ipSpace =
        IpWildcardSetIpSpace.create(
            ImmutableSet.of(IpWildcard.create(IP2)), ImmutableSet.of(IpWildcard.create(IP1)));
    assertTrue(containsIp(IP1).visitIpWildcardSetIpSpace(ipSpace));
    assertFalse(containsIp(IP2).visitIpWildcardSetIpSpace(ipSpace));
  }

  @Test
  void testVisitPrefixIpSpace() {
    PrefixIpSpace ipSpace = (PrefixIpSpace) Prefix.create(IP1, 31).toIpSpace();
    assertTrue(containsIp(IP1).visitPrefixIpSpace(ipSpace));
    assertFalse(containsIp(IP2).visitPrefixIpSpace(ipSpace));
  }

  @Test
  void visitUniverseIpSpace() {
    assertTrue(containsIp(IP1).visitUniverseIpSpace(UniverseIpSpace.INSTANCE));
  }
}
