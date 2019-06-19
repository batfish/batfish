package org.batfish.common.bdd;

import static org.batfish.datamodel.IpAccessListLine.rejecting;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link IpAccessListToBdd}. */
public class IpAccessListToBddTest {
  @Rule public ExpectedException exception = ExpectedException.none();

  private NetworkFactory _nf;

  private BDDPacket _pkt;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _pkt = new BDDPacket();
  }

  private IpAccessList aclWithLines(IpAccessListLine... lines) {
    return _nf.aclBuilder().setLines(Arrays.asList(lines)).build();
  }

  private static IpAccessListLine accepting(AclLineMatchExpr matchExpr) {
    return IpAccessListLine.accepting().setMatchCondition(matchExpr).build();
  }

  private static IpAccessListLine accepting(HeaderSpace headerSpace) {
    return accepting(new MatchHeaderSpace(headerSpace));
  }

  private static IpAccessListLine acceptingDst(Prefix prefix) {
    return accepting(
        new MatchHeaderSpace(HeaderSpace.builder().setDstIps(prefix.toIpSpace()).build()));
  }

  private static IpAccessListLine rejectingDst(Prefix prefix) {
    return rejecting(
        new MatchHeaderSpace(HeaderSpace.builder().setDstIps(prefix.toIpSpace()).build()));
  }

  @Test
  public void testPermittedByAcl2() {
    Ip fooIp = Ip.parse("1.1.1.1");
    BDD fooIpBDD = _pkt.getDstIp().value(fooIp.asLong());
    Map<String, IpAccessList> namedAcls =
        ImmutableMap.of(
            "foo",
            aclWithLines(accepting(HeaderSpace.builder().setDstIps(fooIp.toIpSpace()).build())),
            "acl",
            aclWithLines(accepting(new PermittedByAcl("foo"))));

    BDD bdd =
        IpAccessListToBdd.toBDD(
            _pkt,
            aclWithLines(accepting(new PermittedByAcl("acl"))),
            namedAcls,
            ImmutableMap.of(),
            BDDSourceManager.empty(_pkt));
    assertThat(bdd, equalTo(fooIpBDD));
  }

  @Test
  public void testPermittedByAcl() {
    Ip fooIp = Ip.parse("1.1.1.1");
    BDD fooIpBDD = _pkt.getDstIp().value(fooIp.asLong());
    IpAccessList fooAcl =
        aclWithLines(accepting(HeaderSpace.builder().setDstIps(fooIp.toIpSpace()).build()));
    Map<String, IpAccessList> namedAcls = ImmutableMap.of("foo", fooAcl);
    IpAccessList acl = aclWithLines(accepting(new PermittedByAcl("foo")));
    BDD bdd =
        new IpAccessListToBddImpl(_pkt, BDDSourceManager.empty(_pkt), namedAcls, ImmutableMap.of())
            .toBdd(acl);
    assertThat(bdd, equalTo(fooIpBDD));
  }

  @Test
  public void testPermittedByAcl_undefined() {
    IpAccessList acl = aclWithLines(accepting(new PermittedByAcl("foo")));
    IpAccessListToBdd ipAccessListToBdd =
        new IpAccessListToBddImpl(
            _pkt, BDDSourceManager.empty(_pkt), ImmutableMap.of(), ImmutableMap.of());
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Undefined PermittedByAcl reference: foo");
    ipAccessListToBdd.toBdd(acl);
  }

  @Test
  public void testPermittedByAcl_circular() {
    PermittedByAcl permittedByAcl = new PermittedByAcl("foo");
    IpAccessList fooAcl = aclWithLines(accepting(permittedByAcl));
    Map<String, IpAccessList> namedAcls = ImmutableMap.of("foo", fooAcl);
    IpAccessListToBdd ipAccessListToBdd =
        new IpAccessListToBddImpl(_pkt, BDDSourceManager.empty(_pkt), namedAcls, ImmutableMap.of());
    exception.expect(BatfishException.class);
    exception.expectMessage("Circular PermittedByAcl reference: foo");
    ipAccessListToBdd.toBdd(fooAcl);
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

    List<BDD> matchLines1 =
        aclToBdd.reachAndMatchLines(
            aclWithLines(
                acceptingDst(p32), acceptingDst(p24), acceptingDst(p16), acceptingDst(p8)));

    assertThat(
        matchLines1,
        contains(
            bdd32,
            bdd32.not().and(bdd24),
            bdd24.not().and(bdd16),
            bdd16.not().and(bdd8),
            bdd8.not()));

    List<BDD> matchLines2 =
        aclToBdd.reachAndMatchLines(
            aclWithLines(
                rejectingDst(p32), acceptingDst(p24), rejectingDst(p16), acceptingDst(p8)));

    // LineAction doesn't matter.
    assertThat(matchLines1, equalTo(matchLines2));
  }
}
