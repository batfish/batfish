package org.batfish.common.bdd;

import static org.batfish.datamodel.ExprAclLine.ACCEPT_ALL;
import static org.batfish.datamodel.ExprAclLine.rejecting;
import static org.batfish.datamodel.ExprAclLine.rejectingHeaderSpace;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.DeniedByAcl;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link IpAccessListToBdd}. */
public class IpAccessListToBddTest {
  @Rule public ExpectedException exception = ExpectedException.none();

  private static final NetworkFactory NF = new NetworkFactory();
  private final BDDPacket _pkt = new BDDPacket();
  private final BDD _zero = _pkt.getFactory().zero();

  private IpAccessList aclWithLines(ExprAclLine... lines) {
    return NF.aclBuilder().setLines(Arrays.asList(lines)).build();
  }

  private static ExprAclLine accepting(AclLineMatchExpr matchExpr) {
    return ExprAclLine.accepting().setMatchCondition(matchExpr).build();
  }

  private static ExprAclLine accepting(HeaderSpace headerSpace) {
    return accepting(new MatchHeaderSpace(headerSpace));
  }

  private static ExprAclLine acceptingDst(Prefix prefix) {
    return accepting(
        new MatchHeaderSpace(HeaderSpace.builder().setDstIps(prefix.toIpSpace()).build()));
  }

  private static ExprAclLine rejectingDst(Prefix prefix) {
    return rejecting(
        new MatchHeaderSpace(HeaderSpace.builder().setDstIps(prefix.toIpSpace()).build()));
  }

  @Test
  public void testPermittedByAcl2() {
    Ip fooIp = Ip.parse("1.1.1.1");
    BDD fooIpBDD = _pkt.getDstIp().value(fooIp.asLong());
    IpAccessList acl1 =
        IpAccessList.builder()
            .setName("acl1")
            .setLines(accepting(HeaderSpace.builder().setDstIps(fooIp.toIpSpace()).build()))
            .build();
    IpAccessList acl2 =
        IpAccessList.builder()
            .setName("acl2")
            .setLines(accepting(new PermittedByAcl("acl1")))
            .build();
    IpAccessList acl3 =
        IpAccessList.builder()
            .setName("acl3")
            .setLines(accepting(new PermittedByAcl("acl2")))
            .build();
    Map<String, IpAccessList> namedAcls =
        ImmutableMap.of(
            acl1.getName(), acl1,
            acl2.getName(), acl2,
            acl3.getName(), acl3);

    BDD bdd =
        IpAccessListToBdd.toBDD(
            _pkt, acl3, namedAcls, ImmutableMap.of(), BDDSourceManager.empty(_pkt));
    assertThat(bdd, equalTo(fooIpBDD));
  }

  @Test
  public void testDeniedByAcl() {
    Ip fooIp = Ip.parse("1.1.1.1");
    BDD fooIpBDD = _pkt.getDstIp().value(fooIp.asLong());
    IpAccessList fooAcl =
        aclWithLines(accepting(HeaderSpace.builder().setDstIps(fooIp.toIpSpace()).build()));
    Map<String, IpAccessList> namedAcls = ImmutableMap.of("foo", fooAcl);
    BDD bdd =
        new IpAccessListToBddImpl(_pkt, BDDSourceManager.empty(_pkt), namedAcls, ImmutableMap.of())
            .toBdd(new DeniedByAcl("foo"));
    assertThat(bdd, equalTo(fooIpBDD.not()));
  }

  @Test
  public void testDeniedByAcl2() {
    Ip fooIp = Ip.parse("1.1.1.1");
    BDD fooIpBDD = _pkt.getDstIp().value(fooIp.asLong());
    IpAccessList fooAcl =
        IpAccessList.builder()
            .setName("foo")
            .setLines(
                rejectingHeaderSpace(HeaderSpace.builder().setDstIps(fooIp.toIpSpace()).build()),
                ACCEPT_ALL)
            .build();
    IpAccessList acl = aclWithLines(accepting(new DeniedByAcl("foo")));
    Map<String, IpAccessList> namedAcls = ImmutableMap.of("foo", fooAcl, acl.getName(), acl);
    BDD bdd =
        new IpAccessListToBddImpl(_pkt, BDDSourceManager.empty(_pkt), namedAcls, ImmutableMap.of())
            .toBdd(acl);
    assertThat(bdd, equalTo(fooIpBDD));
  }

  @Test
  public void testPermittedByAcl() {
    Ip fooIp = Ip.parse("1.1.1.1");
    BDD fooIpBDD = _pkt.getDstIp().value(fooIp.asLong());
    IpAccessList fooAcl =
        aclWithLines(accepting(HeaderSpace.builder().setDstIps(fooIp.toIpSpace()).build()));
    Map<String, IpAccessList> namedAcls = ImmutableMap.of("foo", fooAcl);
    BDD bdd =
        new IpAccessListToBddImpl(_pkt, BDDSourceManager.empty(_pkt), namedAcls, ImmutableMap.of())
            .toBdd(new PermittedByAcl("foo"));
    assertThat(bdd, equalTo(fooIpBDD));
  }

  @Test
  public void testDeniedByAcl_undefined() {
    IpAccessListToBdd ipAccessListToBdd =
        new IpAccessListToBddImpl(
            _pkt, BDDSourceManager.empty(_pkt), ImmutableMap.of(), ImmutableMap.of());
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Undefined filter reference: foo");
    ipAccessListToBdd.toBdd(new DeniedByAcl("foo"));
  }

  @Test
  public void testDeniedByAcl_circular() {
    IpAccessList acl =
        IpAccessList.builder().setName("foo").setLines(accepting(new DeniedByAcl("foo"))).build();
    Map<String, IpAccessList> namedAcls = ImmutableMap.of("foo", acl);
    IpAccessListToBdd ipAccessListToBdd =
        new IpAccessListToBddImpl(_pkt, BDDSourceManager.empty(_pkt), namedAcls, ImmutableMap.of());
    exception.expect(BatfishException.class);
    exception.expectMessage("Circular filter reference: foo");
    ipAccessListToBdd.toBdd(acl);
  }

  @Test
  public void testPermittedByAcl_undefined() {
    IpAccessListToBdd ipAccessListToBdd =
        new IpAccessListToBddImpl(
            _pkt, BDDSourceManager.empty(_pkt), ImmutableMap.of(), ImmutableMap.of());
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Undefined filter reference: foo");
    ipAccessListToBdd.toBdd(new PermittedByAcl("foo"));
  }

  @Test
  public void testPermittedByAcl_circular() {
    IpAccessList acl =
        IpAccessList.builder()
            .setName("foo")
            .setLines(accepting(new PermittedByAcl("foo")))
            .build();
    Map<String, IpAccessList> namedAcls = ImmutableMap.of("foo", acl);
    IpAccessListToBdd ipAccessListToBdd =
        new IpAccessListToBddImpl(_pkt, BDDSourceManager.empty(_pkt), namedAcls, ImmutableMap.of());
    exception.expect(BatfishException.class);
    exception.expectMessage("Circular filter reference: foo");
    ipAccessListToBdd.toBdd(acl);
  }

  @Test
  public void testReachAndMatchLines() {
    Prefix p32 = Prefix.parse("1.1.1.1/32");
    Prefix p24 = Prefix.parse("1.1.1.0/24");
    Prefix p16 = Prefix.parse("1.1.0.0/16");
    Prefix p8 = Prefix.parse("1.0.0.0/8");

    IpAccessListToBdd aclToBdd =
        new IpAccessListToBddImpl(
            _pkt, BDDSourceManager.empty(_pkt), ImmutableMap.of(), ImmutableMap.of());
    IpSpaceToBDD dstToBdd = aclToBdd.getHeaderSpaceToBDD().getDstIpSpaceToBdd();

    BDD bdd32 = dstToBdd.toBDD(p32);
    BDD bdd24 = dstToBdd.toBDD(p24);
    BDD bdd16 = dstToBdd.toBDD(p16);
    BDD bdd8 = dstToBdd.toBDD(p8);

    List<PermitAndDenyBdds> matchLines1 =
        aclToBdd.reachAndMatchLines(
            aclWithLines(
                rejectingDst(p32), acceptingDst(p24), rejectingDst(p16), acceptingDst(p8)));

    assertThat(
        matchLines1,
        contains(
            new PermitAndDenyBdds(_zero, bdd32),
            new PermitAndDenyBdds(bdd32.not().and(bdd24), _zero),
            new PermitAndDenyBdds(_zero, bdd24.not().and(bdd16)),
            new PermitAndDenyBdds(bdd16.not().and(bdd8), _zero),
            new PermitAndDenyBdds(_zero, bdd8.not())));
  }
}
