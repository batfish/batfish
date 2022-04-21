package org.batfish.common.bdd;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import net.sf.javabdd.BDD;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.UniverseIpSpace;
import org.junit.Before;
import org.junit.Test;

public class MemoizedIpSpaceToBDDTest {
  private static final IpSpace IP1_IP_SPACE = Ip.parse("1.1.1.1").toIpSpace();
  private static final IpSpace IP2_IP_SPACE = Ip.parse("2.2.2.2").toIpSpace();
  private static final IpSpace ACL_IP_SPACE =
      AclIpSpace.builder().thenPermitting(IP1_IP_SPACE).thenPermitting(IP2_IP_SPACE).build();

  private MemoizedIpSpaceToBDD _toBdd;

  @Before
  public void setup() {
    BDDPacket pkt = new BDDPacket();
    _toBdd = new MemoizedIpSpaceToBDD(pkt.getDstIp());
  }

  @Test
  public void testVisit() {
    assertThat(
        "no entry", _toBdd.getMemoizedBddForTesting(IP1_IP_SPACE), equalTo(Optional.empty()));
    BDD bdd = _toBdd.visit(IP1_IP_SPACE);
    Optional<BDD> memoized = _toBdd.getMemoizedBddForTesting(IP1_IP_SPACE);
    assertThat("entry is present", memoized, equalTo(Optional.of(bdd)));
    assertThat("entry is different than result", memoized.get(), not(sameInstance(bdd)));

    BDD bdd2 = _toBdd.visit(IP1_IP_SPACE);
    assertThat("visit twice", bdd2, equalTo(bdd));
    assertThat("visit different instance", bdd2, not(sameInstance(bdd)));
    Optional<BDD> memoized2 = _toBdd.getMemoizedBddForTesting(IP1_IP_SPACE);
    assertThat("visit twice same memoized instance", memoized2.get(), sameInstance(memoized.get()));
  }

  @Test
  public void testRecursion() {
    assertThat(_toBdd.getMemoizedBddForTesting(IP1_IP_SPACE), equalTo(Optional.empty()));
    assertThat(_toBdd.getMemoizedBddForTesting(IP2_IP_SPACE), equalTo(Optional.empty()));
    assertThat(_toBdd.getMemoizedBddForTesting(ACL_IP_SPACE), equalTo(Optional.empty()));
    BDD bdd = _toBdd.visit(ACL_IP_SPACE);
    assertTrue(
        "IP1_IP_SPACE should be memoized",
        _toBdd.getMemoizedBddForTesting(IP1_IP_SPACE).isPresent());
    assertTrue(
        "IP2_IP_SPACE should be memoized",
        _toBdd.getMemoizedBddForTesting(IP2_IP_SPACE).isPresent());
    assertThat(_toBdd.getMemoizedBddForTesting(ACL_IP_SPACE), equalTo(Optional.of(bdd)));
  }

  /**
   * A test that {@link AclIpSpace} memoization is feasible. This test finishes in well under a
   * second on a laptop (2017 Macbook Pro 13"), but times out in several prior implementations of
   * {@link MemoizedIpSpaceToBDD}.
   */
  @Test(timeout = 60_000)
  public void testDeepAclIpSpace() {
    int depth = 15;
    long ip = Ip.parse("1.2.3.3").asLong();
    IpSpace current =
        AclIpSpace.builder()
            .thenRejecting(Ip.parse("1.2.3.4").toIpSpace())
            .thenPermitting(Prefix.parse("1.0.0.0/8").toIpSpace())
            .thenRejecting(Ip.parse("2.2.3.4").toIpSpace())
            .thenPermitting(Prefix.parse("2.0.0.0/8").toIpSpace())
            .build();
    assertThat(current, instanceOf(AclIpSpace.class));
    for (int i = 0; i < depth; ++i) {
      current =
          AclIpSpace.permitting(Ip.create(ip++).toIpSpace())
              .thenRejecting(current)
              .thenPermitting(current.complement())
              .thenRejecting(current.complement())
              .thenPermitting(current.complement())
              .thenRejecting(current.complement())
              .thenPermitting(current.complement())
              .build();
    }
    _toBdd.visit(current);
    IpSpace cloned = SerializationUtils.clone(current);
    _toBdd.visit(cloned);
  }

  @Test
  public void testChaining_IpSpaceReference() {
    MemoizedIpSpaceToBDD toBdd2 =
        new MemoizedIpSpaceToBDD(_toBdd, ImmutableMap.of("IP1", IP1_IP_SPACE));

    IpSpaceReference ref = new IpSpaceReference("IP1");
    BDD bdd = toBdd2.visit(ref);
    // outer cache has an entry for the reference, but not the IP
    assertThat(toBdd2.getMemoizedBddForTesting(ref), equalTo(Optional.of(bdd)));
    assertThat(toBdd2.getMemoizedBddForTesting(IP1_IP_SPACE), equalTo(Optional.empty()));

    // inner cache has an entry for the IP, but not the reference
    assertThat(_toBdd.getMemoizedBddForTesting(ref), equalTo(Optional.empty()));
    assertThat(_toBdd.getMemoizedBddForTesting(IP1_IP_SPACE), equalTo(Optional.of(bdd)));
  }

  @Test
  public void testChaining_AclIpSpace() {
    MemoizedIpSpaceToBDD toBdd2 = new MemoizedIpSpaceToBDD(_toBdd, ImmutableMap.of());
    toBdd2.visit(ACL_IP_SPACE);

    // outer cache has an entry for the acl, but not the IPs
    assertTrue(toBdd2.getMemoizedBddForTesting(ACL_IP_SPACE).isPresent());
    assertFalse(toBdd2.getMemoizedBddForTesting(IP1_IP_SPACE).isPresent());
    assertFalse(toBdd2.getMemoizedBddForTesting(IP2_IP_SPACE).isPresent());

    // inner cache has an entry for the IPs, but not the acl
    assertFalse(_toBdd.getMemoizedBddForTesting(ACL_IP_SPACE).isPresent());
    assertTrue(_toBdd.getMemoizedBddForTesting(IP1_IP_SPACE).isPresent());
    assertTrue(_toBdd.getMemoizedBddForTesting(IP2_IP_SPACE).isPresent());
  }

  /**
   * Test that IpSpaces that cannot contain references are converted/cached by the inner
   * IpSpaceToBDD instance.
   */
  @Test
  public void testChaining_nonReference() {
    MemoizedIpSpaceToBDD toBdd2 = new MemoizedIpSpaceToBDD(_toBdd, ImmutableMap.of());

    IpWildcard wc = IpWildcard.parse("0.2.0.0:255.0.255.255");
    List<IpSpace> nonRefIpSpaces =
        ImmutableList.of(
            EmptyIpSpace.INSTANCE,
            UniverseIpSpace.INSTANCE,
            IP1_IP_SPACE,
            Prefix.parse("10.0.0.0/8").toIpSpace(),
            wc.toIpSpace(),
            IpWildcardSetIpSpace.create(ImmutableSet.of(IpWildcard.ANY), ImmutableSet.of(wc)));

    for (IpSpace ipSpace : nonRefIpSpaces) {
      toBdd2.visit(ipSpace);
      // outer cache does not have an entry
      assertFalse(toBdd2.getMemoizedBddForTesting(ipSpace).isPresent());
      // inner cache does have an entry
      assertTrue(_toBdd.getMemoizedBddForTesting(ipSpace).isPresent());
    }
  }
}
