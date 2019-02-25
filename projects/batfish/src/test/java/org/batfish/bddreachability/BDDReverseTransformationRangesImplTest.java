package org.batfish.bddreachability;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.HeaderSpaceToBDD;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Vrf;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.PreInInterface;
import org.batfish.z3.state.PreOutEdge;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link BDDReverseTransformationRangesImpl}. */
public class BDDReverseTransformationRangesImplTest {
  private static final String HOSTNAME = "HOSTNAME";

  private BDDPacket _bddPacket;
  private Map<String, Configuration> _configs;
  private Interface.Builder _ib;
  private HeaderSpaceToBDD _headerSpaceToBDD;

  @Before
  public void setup() {
    _bddPacket = new BDDPacket();
    NetworkFactory nf = new NetworkFactory();
    Configuration config =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname(HOSTNAME)
            .build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    _ib = nf.interfaceBuilder().setOwner(config).setVrf(vrf).setActive(true);
    _configs = ImmutableMap.of(HOSTNAME, config);
    _headerSpaceToBDD = new HeaderSpaceToBDD(_bddPacket, ImmutableMap.of());
  }

  @Test
  public void testUnreachable() {
    /* Should get 0 BDD when the transformation was not reached by the forward analysis (i.e. there
     * is no entry for the corresponding state(s) in the reachability map.
     */

    Interface iface = _ib.build();

    BDDReverseTransformationRangesImpl ranges =
        new BDDReverseTransformationRangesImpl(_configs, ImmutableMap.of(), _bddPacket, null);

    assertTrue(ranges.reverseIncomingTransformationRange(HOSTNAME, iface.getName()).isZero());
    assertTrue(ranges.reverseOutgoingTransformationRange(HOSTNAME, iface.getName()).isZero());
  }

  @Test
  public void testIncomingTransformationRange() {
    Interface iface = _ib.build();

    BDD fwdPreInBdd = _headerSpaceToBDD.getDstIpSpaceToBdd().toBDD(Ip.parse("1.1.1.1"));
    BDD bwdPreInBdd = _headerSpaceToBDD.getSrcIpSpaceToBdd().toBDD(Ip.parse("1.1.1.1"));

    Map<StateExpr, BDD> forwardReach =
        ImmutableMap.of(new PreInInterface(HOSTNAME, iface.getName()), fwdPreInBdd);

    // without incoming filter
    {
      BDDReverseTransformationRangesImpl ranges =
          new BDDReverseTransformationRangesImpl(_configs, forwardReach, _bddPacket, null);
      assertThat(
          ranges.reverseIncomingTransformationRange(HOSTNAME, iface.getName()),
          equalTo(bwdPreInBdd));
    }

    // with incoming filter
    {
      String aclName = "ACL";
      iface.setIncomingFilter(IpAccessList.builder().setName(aclName).build());
      BDD fwdAclBdd =
          _headerSpaceToBDD.toBDD(
              HeaderSpace.builder().setDstPorts(ImmutableList.of(new SubRange(100, 200))).build());
      BDD bwdAclBdd =
          _headerSpaceToBDD.toBDD(
              HeaderSpace.builder().setSrcPorts(ImmutableList.of(new SubRange(100, 200))).build());

      BDDReverseTransformationRangesImpl ranges =
          new BDDReverseTransformationRangesImpl(
              _configs,
              forwardReach,
              _bddPacket,
              ImmutableMap.of(HOSTNAME, ImmutableMap.of(aclName, fwdAclBdd)));
      assertThat(
          ranges.reverseIncomingTransformationRange(HOSTNAME, iface.getName()),
          equalTo(bwdPreInBdd.and(bwdAclBdd)));
    }
  }

  @Test
  public void testOutgoingTransformationRange() {
    Interface iface = _ib.build();

    BDD fwdPreOutEdge1Bdd = _headerSpaceToBDD.getDstIpSpaceToBdd().toBDD(Ip.parse("1.1.1.1"));
    BDD fwdPreOutEdge2Bdd = _headerSpaceToBDD.getDstIpSpaceToBdd().toBDD(Ip.parse("1.1.1.2"));

    BDD reach =
        _headerSpaceToBDD
            .getSrcIpSpaceToBdd()
            .toBDD(Ip.parse("1.1.1.1"))
            .or(_headerSpaceToBDD.getSrcIpSpaceToBdd().toBDD(Ip.parse("1.1.1.2")));

    // two out-edges with iface at the head
    Map<StateExpr, BDD> forwardReach =
        ImmutableMap.of(
            new PreOutEdge(HOSTNAME, iface.getName(), "1", "1"),
            fwdPreOutEdge1Bdd,
            new PreOutEdge(HOSTNAME, iface.getName(), "2", "2"),
            fwdPreOutEdge2Bdd);

    // without pre-transformation outgoing filter
    {
      BDDReverseTransformationRangesImpl ranges =
          new BDDReverseTransformationRangesImpl(_configs, forwardReach, _bddPacket, null);
      assertThat(
          ranges.reverseOutgoingTransformationRange(HOSTNAME, iface.getName()), equalTo(reach));
    }

    // with pre-transformation outgoing filter
    {
      String aclName = "ACL";
      iface.setPreTransformationOutgoingFilter(IpAccessList.builder().setName(aclName).build());
      BDD fwdAclBdd =
          _headerSpaceToBDD.toBDD(
              HeaderSpace.builder().setDstPorts(ImmutableList.of(new SubRange(100, 200))).build());
      BDD bwdAclBdd =
          _headerSpaceToBDD.toBDD(
              HeaderSpace.builder().setSrcPorts(ImmutableList.of(new SubRange(100, 200))).build());

      BDDReverseTransformationRangesImpl ranges =
          new BDDReverseTransformationRangesImpl(
              _configs,
              forwardReach,
              _bddPacket,
              ImmutableMap.of(HOSTNAME, ImmutableMap.of(aclName, fwdAclBdd)));
      assertThat(
          ranges.reverseOutgoingTransformationRange(HOSTNAME, iface.getName()),
          equalTo(reach.and(bwdAclBdd)));
    }
  }
}
