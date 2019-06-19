package org.batfish.common.bdd;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Map;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class IpSpaceToBDDTest {
  @Rule public ExpectedException exception = ExpectedException.none();

  private BDDFactory _factory;
  private BDDOps _bddOps;
  private BDDInteger _ipAddrBdd;
  private IpSpaceToBDD _ipSpaceToBdd;

  @Before
  public void init() {
    _factory = BDDUtils.bddFactory(32);
    _bddOps = new BDDOps(_factory);
    _ipAddrBdd = BDDInteger.makeFromIndex(_factory, 32, 0, true);
    _ipSpaceToBdd = new IpSpaceToBDD(_ipAddrBdd);
  }

  @Test
  public void testIpIpSpace_0() {
    IpSpace ipSpace = Ip.parse("0.0.0.0").toIpSpace();
    BDD bdd = ipSpace.accept(_ipSpaceToBdd);
    assertThat(
        bdd,
        equalTo(
            _bddOps.and(
                Arrays.stream(_ipAddrBdd.getBitvec())
                    .map(BDD::not)
                    .collect(ImmutableList.toImmutableList()))));
  }

  @Test
  public void testIpIpSpace_255() {
    IpSpace ipSpace = Ip.parse("255.255.255.255").toIpSpace();
    BDD bdd = ipSpace.accept(_ipSpaceToBdd);
    assertThat(bdd, equalTo(_bddOps.and(_ipAddrBdd.getBitvec())));
  }

  @Test
  public void testPrefixIpSpace() {
    IpSpace ipSpace = Prefix.parse("255.0.0.0/8").toIpSpace();
    BDD bdd = ipSpace.accept(_ipSpaceToBdd);
    assertThat(bdd, equalTo(_bddOps.and(Arrays.asList(_ipAddrBdd.getBitvec()).subList(0, 8))));
  }

  @Test
  public void testPrefixIpSpace_andMoreSpecific() {
    IpSpace ipSpace1 = Prefix.parse("255.0.0.0/8").toIpSpace();
    IpSpace ipSpace2 = Prefix.parse("255.255.0.0/16").toIpSpace();
    BDD bdd1 = ipSpace1.accept(_ipSpaceToBdd);
    BDD bdd2 = ipSpace2.accept(_ipSpaceToBdd);
    assertThat(_bddOps.and(bdd1, bdd2), equalTo(bdd2));
  }

  @Test
  public void testPrefixIpSpace_andNonOverlapping() {
    IpSpace ipSpace1 = Prefix.parse("0.0.0.0/8").toIpSpace();
    IpSpace ipSpace2 = Prefix.parse("1.0.0.0/8").toIpSpace();
    BDD bdd1 = ipSpace1.accept(_ipSpaceToBdd);
    BDD bdd2 = ipSpace2.accept(_ipSpaceToBdd);
    assertThat(_bddOps.and(bdd1, bdd2), equalTo(_factory.zero()));
  }

  @Test
  public void testPrefixIpSpace_orMoreSpecific() {
    IpSpace ipSpace1 = Prefix.parse("255.0.0.0/8").toIpSpace();
    IpSpace ipSpace2 = Prefix.parse("255.255.0.0/16").toIpSpace();
    BDD bdd1 = ipSpace1.accept(_ipSpaceToBdd);
    BDD bdd2 = ipSpace2.accept(_ipSpaceToBdd);
    assertThat(_bddOps.or(bdd1, bdd2), equalTo(bdd1));
  }

  @Test
  public void testPrefixIpSpace_orNonOverlapping() {
    IpSpace ipSpace1 = Prefix.parse("0.0.0.0/8").toIpSpace();
    IpSpace ipSpace2 = Prefix.parse("1.0.0.0/8").toIpSpace();
    BDD bdd1 = ipSpace1.accept(_ipSpaceToBdd);
    BDD bdd2 = ipSpace2.accept(_ipSpaceToBdd);
    assertThat(
        _bddOps.or(bdd1, bdd2),
        equalTo(
            _bddOps.and(
                Arrays.asList(_ipAddrBdd.getBitvec()).subList(0, 7).stream()
                    .map(BDD::not)
                    .collect(ImmutableList.toImmutableList()))));
  }

  @Test
  public void testIpWildcard() {
    IpSpace ipSpace =
        IpWildcard.ipWithWildcardMask(Ip.parse("255.0.255.0"), Ip.parse("0.255.0.255")).toIpSpace();
    BDD bdd = ipSpace.accept(_ipSpaceToBdd);
    assertThat(
        bdd,
        equalTo(
            _bddOps.and(
                Streams.concat(
                        Arrays.asList(_ipAddrBdd.getBitvec()).subList(0, 8).stream(),
                        Arrays.asList(_ipAddrBdd.getBitvec()).subList(16, 24).stream())
                    .collect(ImmutableList.toImmutableList()))));
  }

  @Test
  public void testIpWildcard_prefix() {
    IpSpace ipWildcardIpSpace =
        IpWildcard.ipWithWildcardMask(Ip.parse("123.0.0.0"), Ip.parse("0.255.255.255")).toIpSpace();
    IpSpace prefixIpSpace = Prefix.parse("123.0.0.0/8").toIpSpace();
    BDD bdd1 = ipWildcardIpSpace.accept(_ipSpaceToBdd);
    BDD bdd2 = prefixIpSpace.accept(_ipSpaceToBdd);
    assertThat(bdd1, equalTo(bdd2));
  }

  @Test
  public void testIpSpaceReference() {
    Ip ip = Ip.parse("1.1.1.1");
    Map<String, IpSpace> namedIpSpaces = ImmutableMap.of("foo", ip.toIpSpace());
    IpSpace reference = new IpSpaceReference("foo");
    IpSpaceToBDD ipSpaceToBDD = new IpSpaceToBDD(_ipAddrBdd, namedIpSpaces);
    BDD ipBDD = ip.toIpSpace().accept(ipSpaceToBDD);
    BDD referenceBDD = reference.accept(ipSpaceToBDD);
    assertThat(referenceBDD, equalTo(ipBDD));
  }

  @Test
  public void testUndefinedIpSpaceReference() {
    IpSpace reference = new IpSpaceReference("foo");
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Undefined IpSpace reference: foo");
    reference.accept(_ipSpaceToBdd);
  }

  @Test
  public void testCircularIpSpaceReference() {
    IpSpace foo = new IpSpaceReference("foo");
    IpSpace bar = new IpSpaceReference("bar");
    Map<String, IpSpace> namedIpSpaces = ImmutableMap.of("foo", bar, "bar", foo);
    IpSpaceToBDD ipSpaceToBDD = new IpSpaceToBDD(_ipAddrBdd, namedIpSpaces);
    exception.expect(BatfishException.class);
    exception.expectMessage("Circular IpSpaceReference: foo");
    foo.accept(ipSpaceToBDD);
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
  }
}
