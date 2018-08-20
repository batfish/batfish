package org.batfish.z3;

import static org.batfish.datamodel.IpAccessListLine.acceptingHeaderSpace;
import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.match;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.symbolic.bdd.AclLineMatchExprToBDD;
import org.batfish.symbolic.bdd.BDDInteger;
import org.batfish.symbolic.bdd.BDDPacket;
import org.batfish.symbolic.bdd.BDDSourceManager;
import org.batfish.symbolic.bdd.IpSpaceToBDD;
import org.junit.Test;

public class BDDIpAccessListSpecializerTest {
  private static final BDDPacket PKT = new BDDPacket();

  private static final IpSpaceToBDD DST_IP_SPACE_TO_BDD =
      new IpSpaceToBDD(PKT.getFactory(), PKT.getDstIp());

  private static final IpAccessListSpecializer EMPTY_HEADERSPACE_SPECIALIZER =
      new BDDIpAccessListSpecializer(PKT, PKT.getFactory().zero(), ImmutableMap.of());

  private static final IpAccessListSpecializer FULL_HEADERSPACE_SPECIALIZER =
      new BDDIpAccessListSpecializer(PKT, PKT.getFactory().one(), ImmutableMap.of());

  private static final IpSpaceToBDD SRC_IP_SPACE_TO_BDD =
      new IpSpaceToBDD(PKT.getFactory(), PKT.getSrcIp());

  private static final HeaderSpace UNCONSTRAINED = HeaderSpace.builder().build();

  @Test
  public void testCanSpecialize() {
    assertThat(EMPTY_HEADERSPACE_SPECIALIZER.canSpecialize(), is(true));
    assertThat(FULL_HEADERSPACE_SPECIALIZER.canSpecialize(), is(false));
  }

  @Test
  public void testSpecializeIpAccessListLine_singleDst() {
    IpAccessListLine ipAccessListLine =
        IpAccessListLine.accepting()
            .setMatchCondition(
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setDstIps(new IpWildcard("1.2.3.0/24").toIpSpace())
                        .build()))
            .build();

    assertThat(
        FULL_HEADERSPACE_SPECIALIZER.specialize(ipAccessListLine),
        equalTo(Optional.of(ipAccessListLine)));
    assertThat(
        EMPTY_HEADERSPACE_SPECIALIZER.specialize(ipAccessListLine), equalTo(Optional.empty()));

    // specialize to a headerspace that whitelists part of the dstIp
    BDD headerSpaceBDD = DST_IP_SPACE_TO_BDD.toBDD(new Ip("1.2.3.4"));
    IpAccessListSpecializer specializer =
        new BDDIpAccessListSpecializer(PKT, headerSpaceBDD, ImmutableMap.of());
    assertThat(
        specializer.specialize(ipAccessListLine),
        equalTo(Optional.of(IpAccessListLine.ACCEPT_ALL)));

    // specialize to a headerspace that blacklists part of the dstIp
    specializer = new BDDIpAccessListSpecializer(PKT, headerSpaceBDD.not(), ImmutableMap.of());
    assertThat(specializer.specialize(ipAccessListLine), equalTo(Optional.of(ipAccessListLine)));
  }

  @Test
  public void notDstIpAlwaysTrue() {
    BDD headerSpaceBDD = DST_IP_SPACE_TO_BDD.toBDD(new Ip("1.2.3.4"));
    IpAccessListSpecializer specializer =
        new BDDIpAccessListSpecializer(PKT, headerSpaceBDD, ImmutableMap.of());

    // both dstIps and notDstIps cover 1.2.3.4
    HeaderSpace headerSpace =
        HeaderSpace.builder()
            .setDstIps(Prefix.parse("1.2.0.0/16").toIpSpace())
            .setNotDstIps(Prefix.parse("1.2.3.0/24").toIpSpace())
            .build();
    IpAccessListLine line =
        IpAccessListLine.accepting().setMatchCondition(match(headerSpace)).build();
    assertThat(specializer.specialize(line), equalTo(Optional.empty()));
  }

  @Test
  public void testSpecializeIpAccessListLine_singleSrc() {
    IpAccessListLine ipAccessListLine =
        acceptingHeaderSpace(
            HeaderSpace.builder().setSrcIps(new IpWildcard("1.2.3.0/24").toIpSpace()).build());

    assertThat(
        FULL_HEADERSPACE_SPECIALIZER.specialize(ipAccessListLine),
        equalTo(Optional.of(ipAccessListLine)));
    assertThat(
        EMPTY_HEADERSPACE_SPECIALIZER.specialize(ipAccessListLine), equalTo(Optional.empty()));

    // specialize to a headerspace that whitelists part of the srcIp
    BDD headerSpaceBDD = SRC_IP_SPACE_TO_BDD.toBDD(new Ip("1.2.3.4"));
    IpAccessListSpecializer specializer =
        new BDDIpAccessListSpecializer(PKT, headerSpaceBDD, ImmutableMap.of());
    assertThat(
        specializer.specialize(ipAccessListLine),
        equalTo(Optional.of(IpAccessListLine.ACCEPT_ALL)));

    // specialize to a headerspace that blacklists part of the srcIp
    specializer = new BDDIpAccessListSpecializer(PKT, headerSpaceBDD.not(), ImmutableMap.of());
    assertThat(specializer.specialize(ipAccessListLine), equalTo(Optional.of(ipAccessListLine)));
  }

  private interface HeaderSpaceSetter<T> {
    HeaderSpace.Builder set(HeaderSpace.Builder builder, Iterable<T> values);
  }

  private static void specializeSubRangeField(
      BDDInteger field, HeaderSpaceSetter<SubRange> setter) {
    BDD headerSpaceBDD = field.geq(100).and(field.leq(200));
    BDDIpAccessListSpecializer specializer =
        new BDDIpAccessListSpecializer(PKT, headerSpaceBDD, ImmutableMap.of());
    HeaderSpace original =
        setter
            .set(
                HeaderSpace.builder(),
                ImmutableList.of(
                    new SubRange(0, 10),
                    new SubRange(50, 100),
                    new SubRange(150, 200),
                    new SubRange(250, 300)))
            .build();
    HeaderSpace specialized =
        setter
            .set(
                HeaderSpace.builder(),
                ImmutableList.of(new SubRange(50, 100), new SubRange(150, 200)))
            .build();
    assertThat(specializer.specialize(original), equalTo(specialized));

    original =
        setter
            .set(
                HeaderSpace.builder(),
                ImmutableList.of(new SubRange(0, 10), new SubRange(250, 300)))
            .build();
    // all subranges removed
    assertThat(specializer.specialize(original), equalTo(UNCONSTRAINED));

    /*
     * Since none of the choices from the original headerspace is matchable, the line itself is
     * unmatchable and can be removed.
     */
    assertThat(specializer.specialize(acceptingHeaderSpace(original)), equalTo(Optional.empty()));
  }

  @Test
  public void specializeDstPorts() {
    specializeSubRangeField(PKT.getDstPort(), HeaderSpace.Builder::setDstPorts);
  }

  @Test
  public void specializeIpProtocols() {
    BDDInteger ipProtocol = PKT.getIpProtocol();
    BDD headerSpaceBDD =
        ipProtocol.value(IpProtocol.TCP.number()).or(ipProtocol.value(IpProtocol.UDP.number()));
    BDDIpAccessListSpecializer specializer =
        new BDDIpAccessListSpecializer(PKT, headerSpaceBDD, ImmutableMap.of());

    // Remove any outside the headerspace
    HeaderSpace original =
        HeaderSpace.builder()
            .setIpProtocols(
                ImmutableList.of(IpProtocol.TCP, IpProtocol.ICMP, IpProtocol.UDP, IpProtocol.IP))
            .build();
    HeaderSpace specialized =
        HeaderSpace.builder()
            .setIpProtocols(ImmutableList.of(IpProtocol.TCP, IpProtocol.UDP))
            .build();
    assertThat(specializer.specialize(original), equalTo(specialized));

    /*
     * If we remove all, then empty list. IpAccessListSpecializer will treat that as false
     * (an empty disjunction).
     */
    original = HeaderSpace.builder().setIpProtocols(ImmutableList.of(IpProtocol.ICMP)).build();
    assertThat(specializer.specialize(original), equalTo(UNCONSTRAINED));
  }

  @Test
  public void specializeIcmpCodes() {
    specializeSubRangeField(PKT.getIcmpCode(), HeaderSpace.Builder::setIcmpCodes);
  }

  @Test
  public void specializeIcmpTypes() {
    specializeSubRangeField(PKT.getIcmpType(), HeaderSpace.Builder::setIcmpTypes);
  }

  @Test
  public void specializeMatchSrcInterfaceSuperset() {
    // Line 1 matches a superset of the interfaces line 2 matches; should specialize to TrueExpr
    Set<String> interfaces = ImmutableSet.of("i1", "i2");
    AclLineMatchExpr line1 = new MatchSrcInterface(ImmutableList.of("i1", "i2"));
    AclLineMatchExpr line2 = new MatchSrcInterface(ImmutableList.of("i1"));
    assertThat(specializeTo(line1, line2, interfaces), equalTo(TRUE));
  }

  @Test
  public void specializeMatchSrcInterfaceSubset() {
    // Line 1 matches a subset of the interfaces line 2 matches; should remain the same
    Set<String> interfaces = ImmutableSet.of("i1", "i2");
    AclLineMatchExpr line1 = new MatchSrcInterface(ImmutableList.of("i1"));
    AclLineMatchExpr line2 = new MatchSrcInterface(ImmutableList.of("i1", "i2"));
    assertThat(specializeTo(line1, line2, interfaces), equalTo(line1));
  }

  @Test
  public void specializeMatchSrcInterfaceNoOverlap() {
    // Line 1 matches a set of interfaces with no overlap with those line 2 matches; should
    // specialize to FalseExpr
    Set<String> interfaces = ImmutableSet.of("i1", "i2");
    AclLineMatchExpr line1 = new MatchSrcInterface(ImmutableList.of("i1"));
    AclLineMatchExpr line2 = new MatchSrcInterface(ImmutableList.of("i2"));
    assertThat(specializeTo(line1, line2, interfaces), equalTo(FALSE));
  }

  @Test
  public void specializeMatchSrcInterfaceOverlap() {
    // Line 1 and line 2 have interfaces in common, but neither is a superset of the other; line 1
    // should specialize to a subset of line 2
    Set<String> interfaces = ImmutableSet.of("i1", "i2", "i3");
    AclLineMatchExpr line1 = new MatchSrcInterface(ImmutableList.of("i1", "i2"));
    AclLineMatchExpr line2 = new MatchSrcInterface(ImmutableList.of("i1", "i3"));
    assertThat(
        specializeTo(line1, line2, interfaces),
        equalTo(new MatchSrcInterface(ImmutableList.of("i1"))));
  }

  @Test
  public void specializeOriginatingFromDeviceRepeat() {
    // Both lines accept packets originating from device; line 1 should specialize to true expr
    AclLineMatchExpr line1 = OriginatingFromDevice.INSTANCE;
    AclLineMatchExpr line2 = OriginatingFromDevice.INSTANCE;
    assertThat(specializeTo(line1, line2, ImmutableSet.of()), equalTo(TRUE));
  }

  @Test
  public void specializeOriginatingFromDeviceToMatchSrcInterface() {
    // First line matches originating from device, second matches source interfaces; line 1 should
    // specialize to false expr
    AclLineMatchExpr line1 = OriginatingFromDevice.INSTANCE;
    AclLineMatchExpr line2 = new MatchSrcInterface(ImmutableSet.of("i1"));
    assertThat(specializeTo(line1, line2, ImmutableSet.of("i1")), equalTo(FALSE));
  }

  @Test
  public void specializeOriginatingFromDeviceWithOr() {
    // First line matches originating from device, second matches that or a source interface; line 1
    // should not change
    AclLineMatchExpr line1 = OriginatingFromDevice.INSTANCE;
    AclLineMatchExpr line2 =
        new OrMatchExpr(
            ImmutableSet.of(
                OriginatingFromDevice.INSTANCE, new MatchSrcInterface(ImmutableSet.of("i1"))));
    assertThat(
        specializeTo(line1, line2, ImmutableSet.of("i1")), equalTo(OriginatingFromDevice.INSTANCE));
  }

  @Test
  public void specializeOriginatingFromDeviceWithOr2() {
    // First line matches originating from device or src interface, second matches only originating
    // from device; line 1 should specialize to true expr
    AclLineMatchExpr line1 =
        new OrMatchExpr(
            ImmutableSet.of(
                OriginatingFromDevice.INSTANCE, new MatchSrcInterface(ImmutableSet.of("i1"))));
    AclLineMatchExpr line2 = OriginatingFromDevice.INSTANCE;
    assertThat(specializeTo(line1, line2, ImmutableSet.of("i1", "i2")), equalTo(TRUE));
  }

  private AclLineMatchExpr specializeTo(
      AclLineMatchExpr line1, AclLineMatchExpr line2, Set<String> interfaces) {
    BDDPacket pkt = new BDDPacket();
    BDDSourceManager sourceMgr = BDDSourceManager.forInterfaces(pkt, interfaces);
    BDD line2BDD =
        line2.accept(
            new AclLineMatchExprToBDD(
                pkt.getFactory(), pkt, ImmutableMap.of(), ImmutableMap.of(), sourceMgr));
    BDDIpAccessListSpecializer specializer =
        new BDDIpAccessListSpecializer(pkt, line2BDD, ImmutableMap.of(), sourceMgr);
    return line1.accept(specializer);
  }

  @Test
  public void specializeSrcOrDstIps_subSpace() {
    /* The srcOrDstIps of the headerspace we are specializing contain subspaces of
     * the BDD source and dst IpSpaces.
     */
    BDD headerSpaceBDD =
        DST_IP_SPACE_TO_BDD
            .toBDD(Prefix.parse("1.1.1.0/24"))
            .and(SRC_IP_SPACE_TO_BDD.toBDD(Prefix.parse("2.2.2.0/24")));

    Ip dstIp = new Ip("1.1.1.1");
    Ip srcIp = new Ip("2.2.2.2");
    Ip randomIp = new Ip("3.3.3.3");
    BDDIpAccessListSpecializer specializer =
        new BDDIpAccessListSpecializer(PKT, headerSpaceBDD, ImmutableMap.of());
    HeaderSpace original =
        HeaderSpace.builder()
            .setSrcOrDstIps(
                AclIpSpace.union(dstIp.toIpSpace(), srcIp.toIpSpace(), randomIp.toIpSpace()))
            .build();
    HeaderSpace specialized =
        HeaderSpace.builder()
            .setSrcOrDstIps(AclIpSpace.union(dstIp.toIpSpace(), srcIp.toIpSpace()))
            .build();
    assertThat(specializer.specialize(original), equalTo(specialized));
  }

  @Test
  public void specializeSrcOrDstIps_superSpace() {
    /* The srcOrDstIps of the headerspace we are specializing contain
     * the BDD source and dst IpSpaces.
     */
    BDD headerSpaceBDD =
        DST_IP_SPACE_TO_BDD
            .toBDD(new Ip("1.1.1.1"))
            .and(SRC_IP_SPACE_TO_BDD.toBDD(new Ip("2.2.2.2")));

    IpSpace dstIpSpace = Prefix.parse("1.1.1.0/24").toIpSpace();
    IpSpace srcIpSpace = Prefix.parse("2.2.2.0/24").toIpSpace();
    Ip randomIp = new Ip("3.3.3.3");
    BDDIpAccessListSpecializer specializer =
        new BDDIpAccessListSpecializer(PKT, headerSpaceBDD, ImmutableMap.of());
    HeaderSpace original =
        HeaderSpace.builder()
            .setSrcOrDstIps(AclIpSpace.union(dstIpSpace, srcIpSpace, randomIp.toIpSpace()))
            .build();
    HeaderSpace specialized =
        HeaderSpace.builder().setSrcOrDstIps(UniverseIpSpace.INSTANCE).build();
    assertThat(specializer.specialize(original), equalTo(specialized));
    assertThat(specializer.visitMatchHeaderSpace(match(original)), equalTo(TRUE));
  }

  @Test
  public void specializeSrcOrDstPorts() {
    BDDInteger srcPort = PKT.getSrcPort();
    BDDInteger dstPort = PKT.getDstPort();
    BDD headerSpaceBDD =
        srcPort.geq(5).and(srcPort.leq(55)).and(dstPort.geq(120)).and(dstPort.leq(170));
    BDDIpAccessListSpecializer specializer =
        new BDDIpAccessListSpecializer(PKT, headerSpaceBDD, ImmutableMap.of());
    HeaderSpace original =
        HeaderSpace.builder()
            .setSrcOrDstPorts(
                ImmutableList.of(
                    new SubRange(0, 10),
                    new SubRange(50, 100),
                    new SubRange(150, 200),
                    new SubRange(250, 300)))
            .build();
    HeaderSpace specialized =
        HeaderSpace.builder()
            .setSrcOrDstPorts(
                ImmutableList.of(
                    new SubRange(0, 10), new SubRange(50, 100), new SubRange(150, 200)))
            .build();
    assertThat(specializer.specialize(original), equalTo(specialized));

    original =
        HeaderSpace.builder()
            .setSrcOrDstPorts(ImmutableList.of(new SubRange(0, 3), new SubRange(250, 300)))
            .build();
    assertThat(specializer.specialize(original), equalTo(UNCONSTRAINED));
  }

  @Test
  public void specializeSrcPorts() {
    specializeSubRangeField(PKT.getSrcPort(), HeaderSpace.Builder::setSrcPorts);
  }

  @Test
  public void specializeTcpFlags() {
    BDD headerSpaceBDD = PKT.getTcpEce().and(PKT.getTcpFin().not());
    BDDIpAccessListSpecializer specializer =
        new BDDIpAccessListSpecializer(PKT, headerSpaceBDD, ImmutableMap.of());

    Iterable<TcpFlags> originalFlagsList =
        ImmutableList.of(
            TcpFlags.builder().setUseAck(true).setAck(true).build(),
            TcpFlags.builder().setUseAck(true).setAck(true).setUseEce(true).setEce(false).build(),
            TcpFlags.builder()
                .setUseEce(true)
                .setEce(true)
                .setUseFin(true)
                .setFin(true)
                .setUseRst(true)
                .setRst(true)
                .build());
    Iterable<TcpFlags> specializedFlagsList =
        ImmutableList.of(TcpFlags.builder().setUseAck(true).setAck(true).build());
    HeaderSpace original = HeaderSpace.builder().setTcpFlags(originalFlagsList).build();
    HeaderSpace specialized = HeaderSpace.builder().setTcpFlags(specializedFlagsList).build();
    assertThat(specializer.specialize(original), equalTo(specialized));
  }
}
