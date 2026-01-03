package org.batfish.common.bdd;

import static org.batfish.common.bdd.BDDMatchers.isOne;
import static org.batfish.common.bdd.BDDMatchers.isZero;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class IpSpaceToBDDTest {
  @Rule public ExpectedException exception = ExpectedException.none();

  private BDDFactory _factory;
  private BDDOps _bddOps;
  private BDD[] _ipAddrBitvec;
  private ImmutableBDDInteger _ipAddrBdd;
  private IpSpaceToBDD _ipSpaceToBdd;

  private static final IpSpace IP1_IP_SPACE = Ip.parse("1.1.1.1").toIpSpace();
  private static final IpSpace IP2_IP_SPACE = Ip.parse("2.2.2.2").toIpSpace();
  private static final IpSpace ACL_IP_SPACE =
      AclIpSpace.builder().thenPermitting(IP1_IP_SPACE).thenPermitting(IP2_IP_SPACE).build();

  @Before
  public void init() {
    _factory = BDDUtils.bddFactory(32);
    _bddOps = new BDDOps(_factory);
    _ipAddrBitvec = IntStream.range(0, 32).mapToObj(_factory::ithVar).toArray(BDD[]::new);
    _ipAddrBdd = new ImmutableBDDInteger(_factory, _ipAddrBitvec);
    _ipSpaceToBdd = new IpSpaceToBDD(_ipAddrBdd);
  }

  @Test
  public void testEmptyIpSpace() {
    BDD bdd = _ipSpaceToBdd.visit(EmptyIpSpace.INSTANCE);
    assertThat(bdd, isZero());
    BDD bdd2 = _ipSpaceToBdd.visit(EmptyIpSpace.INSTANCE);
    assertThat(bdd2, allOf(equalTo(bdd), not(sameInstance(bdd))));
  }

  @Test
  public void testUniverseIpSpace() {
    BDD bdd = _ipSpaceToBdd.visit(UniverseIpSpace.INSTANCE);
    assertThat(bdd, isOne());
    BDD bdd2 = _ipSpaceToBdd.visit(UniverseIpSpace.INSTANCE);
    assertThat(bdd2, allOf(equalTo(bdd), not(sameInstance(bdd))));
  }

  @Test
  public void testIpIpSpace_0() {
    IpSpace ipSpace = Ip.parse("0.0.0.0").toIpSpace();
    BDD bdd = _ipSpaceToBdd.visit(ipSpace);
    assertThat(
        bdd,
        equalTo(
            _bddOps.and(
                Arrays.stream(_ipAddrBitvec)
                    .map(BDD::not)
                    .collect(ImmutableList.toImmutableList()))));
  }

  @Test
  public void testIpIpSpace_255() {
    IpSpace ipSpace = Ip.parse("255.255.255.255").toIpSpace();
    BDD bdd = _ipSpaceToBdd.visit(ipSpace);
    assertThat(bdd, equalTo(_bddOps.and(_ipAddrBitvec)));
    BDD bdd2 = _ipSpaceToBdd.visit(ipSpace);
    assertThat(bdd2, allOf(equalTo(bdd), not(sameInstance(bdd))));
  }

  @Test
  public void testPrefixIpSpace() {
    IpSpace ipSpace = Prefix.parse("255.0.0.0/8").toIpSpace();
    BDD bdd = _ipSpaceToBdd.visit(ipSpace);
    assertThat(bdd, equalTo(_bddOps.and(Arrays.asList(_ipAddrBitvec).subList(0, 8))));
    BDD bdd2 = _ipSpaceToBdd.visit(ipSpace);
    assertThat(bdd2, allOf(equalTo(bdd), not(sameInstance(bdd))));
  }

  @Test
  public void testPrefixIpSpace_andMoreSpecific() {
    IpSpace ipSpace1 = Prefix.parse("255.0.0.0/8").toIpSpace();
    IpSpace ipSpace2 = Prefix.parse("255.255.0.0/16").toIpSpace();
    BDD bdd1 = _ipSpaceToBdd.visit(ipSpace1);
    BDD bdd2 = _ipSpaceToBdd.visit(ipSpace2);
    assertThat(_bddOps.and(bdd1, bdd2), equalTo(bdd2));
  }

  @Test
  public void testPrefixIpSpace_andNonOverlapping() {
    IpSpace ipSpace1 = Prefix.parse("0.0.0.0/8").toIpSpace();
    IpSpace ipSpace2 = Prefix.parse("1.0.0.0/8").toIpSpace();
    BDD bdd1 = _ipSpaceToBdd.visit(ipSpace1);
    BDD bdd2 = _ipSpaceToBdd.visit(ipSpace2);
    assertThat(_bddOps.and(bdd1, bdd2), equalTo(_factory.zero()));
  }

  @Test
  public void testPrefixIpSpace_orMoreSpecific() {
    IpSpace ipSpace1 = Prefix.parse("255.0.0.0/8").toIpSpace();
    IpSpace ipSpace2 = Prefix.parse("255.255.0.0/16").toIpSpace();
    BDD bdd1 = _ipSpaceToBdd.visit(ipSpace1);
    BDD bdd2 = _ipSpaceToBdd.visit(ipSpace2);
    assertThat(_bddOps.or(bdd1, bdd2), equalTo(bdd1));
  }

  @Test
  public void testPrefixIpSpace_orNonOverlapping() {
    IpSpace ipSpace1 = Prefix.parse("0.0.0.0/8").toIpSpace();
    IpSpace ipSpace2 = Prefix.parse("1.0.0.0/8").toIpSpace();
    BDD bdd1 = _ipSpaceToBdd.visit(ipSpace1);
    BDD bdd2 = _ipSpaceToBdd.visit(ipSpace2);
    assertThat(
        _bddOps.or(bdd1, bdd2),
        equalTo(
            _bddOps.and(
                Arrays.asList(_ipAddrBitvec).subList(0, 7).stream()
                    .map(BDD::not)
                    .collect(ImmutableList.toImmutableList()))));
  }

  @Test
  public void testIpWildcard() {
    IpSpace ipSpace =
        IpWildcard.ipWithWildcardMask(Ip.parse("255.0.255.0"), Ip.parse("0.255.0.255")).toIpSpace();
    BDD bdd = _ipSpaceToBdd.visit(ipSpace);
    assertThat(
        bdd,
        equalTo(
            _bddOps.and(
                Streams.concat(
                        Arrays.asList(_ipAddrBitvec).subList(0, 8).stream(),
                        Arrays.asList(_ipAddrBitvec).subList(16, 24).stream())
                    .collect(ImmutableList.toImmutableList()))));
    BDD bdd2 = _ipSpaceToBdd.visit(ipSpace);
    assertThat(bdd2, allOf(equalTo(bdd), not(sameInstance(bdd))));
  }

  @Test
  public void testIpWildcard_prefix() {
    IpSpace ipWildcardIpSpace =
        IpWildcard.ipWithWildcardMask(Ip.parse("123.0.0.0"), Ip.parse("0.255.255.255")).toIpSpace();
    IpSpace prefixIpSpace = Prefix.parse("123.0.0.0/8").toIpSpace();
    BDD bdd1 = _ipSpaceToBdd.visit(ipWildcardIpSpace);
    BDD bdd2 = _ipSpaceToBdd.visit(prefixIpSpace);
    assertThat(bdd1, equalTo(bdd2));
  }

  @Test
  public void testIpSpaceReference() {
    Ip ip = Ip.parse("1.1.1.1");
    Map<String, IpSpace> namedIpSpaces = ImmutableMap.of("foo", ip.toIpSpace());
    IpSpace reference = new IpSpaceReference("foo");
    IpSpaceToBDD ipSpaceToBDD = new IpSpaceToBDD(_ipSpaceToBdd, namedIpSpaces);
    BDD ipBDD = ipSpaceToBDD.visit(ip.toIpSpace());
    BDD referenceBDD = ipSpaceToBDD.visit(reference);
    assertThat(referenceBDD, equalTo(ipBDD));
    BDD bdd2 = ipSpaceToBDD.visit(reference);
    assertThat(bdd2, allOf(equalTo(referenceBDD), not(sameInstance(referenceBDD))));
  }

  @Test
  public void testUndefinedIpSpaceReference() {
    IpSpace reference = new IpSpaceReference("foo");
    exception.expect(UncheckedExecutionException.class);
    exception.expectMessage("Undefined IpSpace reference: foo");
    _ipSpaceToBdd.visit(reference);
  }

  @Test
  public void testCircularIpSpaceReference() {
    IpSpace foo = new IpSpaceReference("foo");
    IpSpace bar = new IpSpaceReference("bar");
    Map<String, IpSpace> namedIpSpaces = ImmutableMap.of("foo", bar, "bar", foo);
    IpSpaceToBDD ipSpaceToBDD = new IpSpaceToBDD(_ipSpaceToBdd, namedIpSpaces);
    exception.expect(UncheckedExecutionException.class);
    exception.expectMessage("Recursive load of: IpSpaceReference{name=foo}");
    ipSpaceToBDD.visit(foo);
  }

  @Test
  public void testAclIpSpace() {
    IpSpace ipSpace1 = Ip.parse("1.1.1.1").toIpSpace();
    IpSpace ipSpace2 = Ip.parse("2.2.2.2").toIpSpace();
    IpSpace ipSpace3 = Ip.parse("3.3.3.3").toIpSpace();
    IpSpace ipSpace4 = Ip.parse("4.4.4.4").toIpSpace();
    IpSpace ipSpace5 = Prefix.ZERO.toIpSpace();
    IpSpace aclIpSpace =
        AclIpSpace.builder()
            .thenPermitting(ipSpace1, ipSpace2)
            .thenRejecting(ipSpace3, ipSpace4)
            .thenPermitting(ipSpace5)
            .build();

    BDD one = _factory.one();
    BDD zero = _factory.zero();
    BDD bdd1 = _ipSpaceToBdd.visit(ipSpace1);
    BDD bdd2 = _ipSpaceToBdd.visit(ipSpace2);
    BDD bdd3 = _ipSpaceToBdd.visit(ipSpace3);
    BDD bdd4 = _ipSpaceToBdd.visit(ipSpace4);
    BDD bdd5 = _ipSpaceToBdd.visit(ipSpace5);
    BDD expected = bdd1.ite(one, bdd2.ite(one, bdd3.ite(zero, bdd4.ite(zero, bdd5))));

    BDD aclBdd = _ipSpaceToBdd.visit(aclIpSpace);
    assertEquals(expected, aclBdd);

    BDD aclBdd2 = _ipSpaceToBdd.visit(aclIpSpace);
    assertThat(aclBdd2, allOf(equalTo(aclBdd), not(sameInstance(aclBdd))));
  }

  @Test
  public void testIpWildcardSetIpSpace() {
    Prefix include1 = Prefix.parse("1.1.1.0/24");
    Prefix include2 = Prefix.parse("1.1.2.0/24");
    Prefix include3 = Prefix.parse("1.1.3.0/24");
    Prefix exclude1 = Prefix.parse("1.1.0.0/16");
    Prefix exclude2 = Prefix.parse("1.1.1.1/32");
    Prefix exclude3 = Prefix.parse("1.1.2.2/32");
    Prefix exclude4 = Prefix.parse("1.1.3.3/32");
    IpWildcardSetIpSpace ipSpace =
        IpWildcardSetIpSpace.builder()
            .including(IpWildcard.create(include1))
            .including(IpWildcard.create(include2))
            .including(IpWildcard.create(include3))
            .excluding(IpWildcard.create(exclude1))
            .excluding(IpWildcard.create(exclude2))
            .excluding(IpWildcard.create(exclude3))
            .excluding(IpWildcard.create(exclude4))
            .build();

    BDD include1Bdd = _ipSpaceToBdd.toBDD(include1);
    BDD include2Bdd = _ipSpaceToBdd.toBDD(include2);
    BDD include3Bdd = _ipSpaceToBdd.toBDD(include3);
    BDD exclude1Bdd = _ipSpaceToBdd.toBDD(exclude1);
    BDD exclude2Bdd = _ipSpaceToBdd.toBDD(exclude2);
    BDD exclude3Bdd = _ipSpaceToBdd.toBDD(exclude3);
    BDD exclude4Bdd = _ipSpaceToBdd.toBDD(exclude4);

    BDD expected =
        include1Bdd
            .or(include2Bdd)
            .or(include3Bdd)
            .diff(exclude1Bdd)
            .diff(exclude2Bdd)
            .diff(exclude3Bdd)
            .diff(exclude4Bdd);
    BDD actual = _ipSpaceToBdd.visit(ipSpace);
    assertEquals(expected, actual);
    BDD actual2 = _ipSpaceToBdd.visit(ipSpace);
    assertThat(actual2, allOf(equalTo(actual), not(sameInstance(actual))));
  }

  @Test
  public void testMemoization() {
    assertThat(
        "no entry",
        _ipSpaceToBdd.getMemoizedBddForTesting(IP1_IP_SPACE),
        equalTo(Optional.empty()));
    BDD bdd = _ipSpaceToBdd.visit(IP1_IP_SPACE);
    Optional<BDD> memoized = _ipSpaceToBdd.getMemoizedBddForTesting(IP1_IP_SPACE);
    assertThat("entry is present", memoized, equalTo(Optional.of(bdd)));
    assertThat("entry is different than result", memoized.get(), not(sameInstance(bdd)));

    BDD bdd2 = _ipSpaceToBdd.visit(IP1_IP_SPACE);
    assertThat("visit twice", bdd2, equalTo(bdd));
    assertThat("visit different instance", bdd2, not(sameInstance(bdd)));
    Optional<BDD> memoized2 = _ipSpaceToBdd.getMemoizedBddForTesting(IP1_IP_SPACE);
    assertThat("visit twice same memoized instance", memoized2.get(), sameInstance(memoized.get()));
  }

  @Test
  public void testRecursion() {
    assertThat(_ipSpaceToBdd.getMemoizedBddForTesting(IP1_IP_SPACE), equalTo(Optional.empty()));
    assertThat(_ipSpaceToBdd.getMemoizedBddForTesting(IP2_IP_SPACE), equalTo(Optional.empty()));
    assertThat(_ipSpaceToBdd.getMemoizedBddForTesting(ACL_IP_SPACE), equalTo(Optional.empty()));
    BDD bdd = _ipSpaceToBdd.visit(ACL_IP_SPACE);
    assertTrue(
        "IP1_IP_SPACE should be memoized",
        _ipSpaceToBdd.getMemoizedBddForTesting(IP1_IP_SPACE).isPresent());
    assertTrue(
        "IP2_IP_SPACE should be memoized",
        _ipSpaceToBdd.getMemoizedBddForTesting(IP2_IP_SPACE).isPresent());
    assertThat(_ipSpaceToBdd.getMemoizedBddForTesting(ACL_IP_SPACE), equalTo(Optional.of(bdd)));
  }

  /**
   * A test that {@link AclIpSpace} memoization is feasible. This test finishes in well under a
   * second on a laptop (2017 Macbook Pro 13"), but times out in several prior implementations of
   * {@link IpSpaceToBDD}.
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
    _ipSpaceToBdd.visit(current);
    IpSpace cloned = SerializationUtils.clone(current);
    _ipSpaceToBdd.visit(cloned);
  }

  @Test
  public void testChaining_IpSpaceReference() {
    IpSpaceToBDD toBdd2 = new IpSpaceToBDD(_ipSpaceToBdd, ImmutableMap.of("IP1", IP1_IP_SPACE));

    IpSpaceReference ref = new IpSpaceReference("IP1");
    BDD bdd = toBdd2.visit(ref);
    // outer cache has an entry for the reference, but not the IP
    assertThat(toBdd2.getMemoizedBddForTesting(ref), equalTo(Optional.of(bdd)));
    assertThat(toBdd2.getMemoizedBddForTesting(IP1_IP_SPACE), equalTo(Optional.empty()));

    // inner cache has an entry for the IP, but not the reference
    assertThat(_ipSpaceToBdd.getMemoizedBddForTesting(ref), equalTo(Optional.empty()));
    assertThat(_ipSpaceToBdd.getMemoizedBddForTesting(IP1_IP_SPACE), equalTo(Optional.of(bdd)));
  }

  @Test
  public void testChaining_AclIpSpace() {
    IpSpaceToBDD toBdd2 = new IpSpaceToBDD(_ipSpaceToBdd, ImmutableMap.of());
    toBdd2.visit(ACL_IP_SPACE);

    // outer cache has an entry for the acl, but not the IPs
    assertTrue(toBdd2.getMemoizedBddForTesting(ACL_IP_SPACE).isPresent());
    assertFalse(toBdd2.getMemoizedBddForTesting(IP1_IP_SPACE).isPresent());
    assertFalse(toBdd2.getMemoizedBddForTesting(IP2_IP_SPACE).isPresent());

    // inner cache has an entry for the IPs, but not the acl
    assertFalse(_ipSpaceToBdd.getMemoizedBddForTesting(ACL_IP_SPACE).isPresent());
    assertTrue(_ipSpaceToBdd.getMemoizedBddForTesting(IP1_IP_SPACE).isPresent());
    assertTrue(_ipSpaceToBdd.getMemoizedBddForTesting(IP2_IP_SPACE).isPresent());
  }

  /**
   * Test that IpSpaces that cannot contain references are converted/cached by the inner
   * IpSpaceToBDD instance.
   */
  @Test
  public void testChaining_nonReference() {
    IpSpaceToBDD toBdd2 = new IpSpaceToBDD(_ipSpaceToBdd, ImmutableMap.of());

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
      assertTrue(_ipSpaceToBdd.getMemoizedBddForTesting(ipSpace).isPresent());
    }
  }
}
