package org.batfish.symbolic.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import java.util.Arrays;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.junit.Before;
import org.junit.Test;

public class IpSpaceToBDDTest {
  private BDDFactory _factory;
  private BDDOps _bddOps;
  private BDDInteger _ipAddrBdd;
  private IpSpaceToBDD _ipSpaceToBdd;

  @Before
  public void init() {
    _factory = BDDUtils.bddFactory(32);
    _bddOps = new BDDOps(_factory);
    _ipAddrBdd = BDDInteger.makeFromIndex(_factory, 32, 0, true);
    _ipSpaceToBdd = new IpSpaceToBDD(_factory, _ipAddrBdd);
  }

  @Test
  public void testIpIpSpace_0() {
    IpSpace ipSpace = new Ip("0.0.0.0").toIpSpace();
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
    IpSpace ipSpace = new Ip("255.255.255.255").toIpSpace();
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
                Arrays.asList(_ipAddrBdd.getBitvec())
                    .subList(0, 7)
                    .stream()
                    .map(BDD::not)
                    .collect(ImmutableList.toImmutableList()))));
  }

  @Test
  public void testIpWildcard() {
    IpSpace ipSpace = new IpWildcard(new Ip("255.0.255.0"), new Ip("0.255.0.255")).toIpSpace();
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
        new IpWildcard(new Ip("123.0.0.0"), new Ip("0.255.255.255")).toIpSpace();
    IpSpace prefixIpSpace = Prefix.parse("123.0.0.0/8").toIpSpace();
    BDD bdd1 = ipWildcardIpSpace.accept(_ipSpaceToBdd);
    BDD bdd2 = prefixIpSpace.accept(_ipSpaceToBdd);
    assertThat(bdd1, equalTo(bdd2));
  }
}
