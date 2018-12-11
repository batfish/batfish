package org.batfish.datamodel;

import static org.batfish.datamodel.Prefix.parse;
import static org.batfish.datamodel.Prefix.strict;
import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDInteger;
import org.batfish.common.bdd.BDDUtils;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PrefixTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testCanonicalization() {
    Prefix p = Prefix.parse("255.255.255.255/15");
    assertThat(p.getStartIp(), equalTo(new Ip("255.254.0.0")));
    assertThat(p.getPrefixLength(), equalTo(15));
  }

  @Test
  public void testContains() {
    PrefixIpSpace p = Prefix.parse("1.2.3.4/31").toIpSpace();
    assertThat(p, containsIp(new Ip("1.2.3.4")));
    assertThat(p, containsIp(new Ip("1.2.3.5")));
    assertThat(p, not(containsIp(new Ip("1.2.3.6"))));
    assertThat(p, not(containsIp(new Ip("1.2.3.3"))));

    // Edge cases - 32 bit prefix
    p = Prefix.parse("1.2.3.4/32").toIpSpace();
    assertThat(p, containsIp(new Ip("1.2.3.4")));
    assertThat(p, not(containsIp(new Ip("1.2.3.5"))));
    assertThat(p, not(containsIp(new Ip("1.2.3.3"))));

    // Edge cases - 0 bit prefix
    p = Prefix.parse("0.0.0.0/0").toIpSpace();
    assertThat(p, containsIp(new Ip("0.0.0.0")));
    assertThat(p, containsIp(new Ip("128.128.128.128")));
    assertThat(p, containsIp(new Ip("255.255.255.255")));
  }

  @Test
  public void testComplement() {
    IpSpace ipSpace = Prefix.parse("1.2.3.4/31").toIpSpace().complement();
    assertThat(ipSpace, not(containsIp(new Ip("1.2.3.4"))));
    assertThat(ipSpace, not(containsIp(new Ip("1.2.3.5"))));
    assertThat(ipSpace, containsIp(new Ip("1.2.3.6")));
    assertThat(ipSpace, containsIp(new Ip("1.2.3.3")));

    // Edge cases - 32 bit prefix
    ipSpace = Prefix.parse("1.2.3.4/32").toIpSpace().complement();
    assertThat(ipSpace, not(containsIp(new Ip("1.2.3.4"))));
    assertThat(ipSpace, containsIp(new Ip("1.2.3.5")));
    assertThat(ipSpace, containsIp(new Ip("1.2.3.3")));

    // Edge cases - 0 bit prefix
    ipSpace = Prefix.parse("0.0.0.0/0").toIpSpace().complement();
    assertThat(ipSpace, not(containsIp(new Ip("0.0.0.0"))));
    assertThat(ipSpace, not(containsIp(new Ip("128.128.128.128"))));
    assertThat(ipSpace, not(containsIp(new Ip("255.255.255.255"))));
  }

  @Test
  public void testStrictCanonical() {
    String canonicalStr = "1.0.0.0/8";
    Prefix canonical = strict(canonicalStr);

    assertThat(strict(canonicalStr).toString(), equalTo(canonicalStr));
    assertThat(parse(canonicalStr), equalTo(canonical));
  }

  @Test
  public void testStrictNonCanonical() {
    String nonCanonical = "1.2.0.0/8";

    _thrown.expect(IllegalArgumentException.class);
    strict(nonCanonical);
  }

  @Test
  public void testToHostIpSpace() {
    BDDInteger ipAddrBdd = BDDInteger.makeFromIndex(BDDUtils.bddFactory(32), 32, 0, true);
    IpSpaceToBDD toBDD = new IpSpaceToBDD(ipAddrBdd);
    assertThat(
        "/32 host space is preserved",
        toBDD.visit(Prefix.parse("1.2.3.4/32").toHostIpSpace()),
        equalTo(toBDD.visit(Prefix.parse("1.2.3.4/32").toIpSpace())));
    assertThat(
        "/31 host space is preserved",
        toBDD.visit(Prefix.parse("1.2.3.4/31").toHostIpSpace()),
        equalTo(toBDD.visit(Prefix.parse("1.2.3.4/31").toIpSpace())));
    BDD slash30Filtered =
        toBDD.visit(new Ip("1.2.3.5").toIpSpace()).or(toBDD.visit(new Ip("1.2.3.6").toIpSpace()));
    assertThat(
        "/30 host space is two IPs",
        toBDD.visit(Prefix.parse("1.2.3.4/30").toHostIpSpace()),
        equalTo(slash30Filtered));
  }
}
