package org.batfish.bddreachability.transition;

import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.transformation.Noop.NOOP_SOURCE_NAT;
import static org.batfish.datamodel.transformation.Transformation.always;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourcePort;
import static org.batfish.datamodel.transformation.TransformationStep.shiftDestinationIp;
import static org.batfish.datamodel.transformation.TransformationStep.shiftSourceIp;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.BDDReachabilityUtils;
import org.batfish.common.bdd.BDDInteger;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.transformation.Transformation;
import org.junit.Before;
import org.junit.Test;

/** Tests of {@link TransformationToTransition}. */
public class TransformationToTransitionTest {
  private BDDPacket _pkt;
  private IpSpaceToBDD _dstIpSpaceToBdd;
  private IpSpaceToBDD _srcIpSpaceToBdd;
  private TransformationToTransition _toTransition;
  private BDD _one;
  private BDD _zero;
  BDD _portTransformationProtocols;

  @Before
  public void setup() {
    _pkt = new BDDPacket();
    _one = _pkt.getFactory().one();
    _zero = _pkt.getFactory().zero();
    _portTransformationProtocols =
        BDDReachabilityUtils.computePortTransformationProtocolsBdd(_pkt.getIpProtocol());
    _dstIpSpaceToBdd = new IpSpaceToBDD(_pkt.getDstIp());
    _srcIpSpaceToBdd = new IpSpaceToBDD(_pkt.getSrcIp());
    _toTransition =
        new TransformationToTransition(
            _pkt,
            new IpAccessListToBddImpl(
                _pkt, BDDSourceManager.empty(_pkt), ImmutableMap.of(), ImmutableMap.of()));
  }

  @Test
  public void testShiftIpAddressIntoSubnet() {
    Prefix shiftIntoPrefix = Prefix.parse("5.5.0.0/16");
    Transformation transformation = always().apply(shiftDestinationIp(shiftIntoPrefix)).build();
    Transition transition = _toTransition.toTransition(transformation);

    // forward -- unconstrained
    BDD expectedOut = _dstIpSpaceToBdd.toBDD(shiftIntoPrefix);
    BDD actualOut = transition.transitForward(_one);
    assertThat(actualOut, equalTo(expectedOut));

    // forward -- outside prefix
    BDD in = _dstIpSpaceToBdd.toBDD(Prefix.parse("1.2.3.0/24"));
    expectedOut = _dstIpSpaceToBdd.toBDD(Prefix.parse("5.5.3.0/24"));
    actualOut = transition.transitForward(in);
    assertThat(actualOut, equalTo(expectedOut));

    // forward -- inside prefix
    in = _dstIpSpaceToBdd.toBDD(Prefix.parse("5.5.3.0/24"));
    expectedOut = in;
    actualOut = transition.transitForward(in);
    assertThat(actualOut, equalTo(expectedOut));

    // backward -- unconstrained
    BDD expectedIn = _one;
    BDD actualIn = transition.transitBackward(_one);
    assertThat(actualIn, equalTo(expectedIn));

    // backward -- constrained
    expectedIn =
        _dstIpSpaceToBdd.toBDD(
            IpWildcard.ipWithWildcardMask(Ip.parse("0.0.3.0"), Ip.parse("255.255.0.255")));
    actualIn = transition.transitBackward(expectedOut);
    assertThat(actualIn, equalTo(expectedIn));
  }

  @Test
  public void testShiftIpAddressIntoSubnet2() {
    Prefix shiftIntoPrefix = Prefix.parse("5.5.0.32/27");
    Transformation transformation = always().apply(shiftDestinationIp(shiftIntoPrefix)).build();
    Transition transition = _toTransition.toTransition(transformation);

    // forward -- unconstrained
    BDD expectedOut = _dstIpSpaceToBdd.toBDD(shiftIntoPrefix);
    BDD actualOut = transition.transitForward(_one);
    assertThat(actualOut, equalTo(expectedOut));

    // forward -- outside prefix
    BDD in = _dstIpSpaceToBdd.toBDD(Prefix.parse("1.2.3.12/30"));
    expectedOut = _dstIpSpaceToBdd.toBDD(Prefix.parse("5.5.0.44/30"));
    actualOut = transition.transitForward(in);
    assertThat(actualOut, equalTo(expectedOut));

    // forward -- inside prefix
    actualOut = transition.transitForward(expectedOut);
    assertThat(actualOut, equalTo(expectedOut));

    // backward -- unconstrained
    BDD expectedIn = _one;
    BDD actualIn = transition.transitBackward(_one);
    assertThat(actualIn, equalTo(expectedIn));

    // backward -- constrained
    Ip address = Ip.parse("0.0.0.12");
    // all bits wild except 28,29,30
    Ip mask = Ip.parse("255.255.255.227");
    expectedIn = _dstIpSpaceToBdd.toBDD(IpWildcard.ipWithWildcardMask(address, mask));
    actualIn = transition.transitBackward(expectedOut);
    assertThat(actualIn, equalTo(expectedIn));
  }

  @Test
  public void testShiftIpAddressIntoSubnetMax() {
    Prefix shiftIntoPrefix = Prefix.parse("5.5.0.32/32");
    Transformation transformation = always().apply(shiftDestinationIp(shiftIntoPrefix)).build();
    Transition transition = _toTransition.toTransition(transformation);

    // forward -- unconstrained
    BDD expectedOut = _dstIpSpaceToBdd.toBDD(shiftIntoPrefix);
    BDD actualOut = transition.transitForward(_one);
    assertThat(actualOut, equalTo(expectedOut));

    // forward -- outside prefix
    BDD in = _dstIpSpaceToBdd.toBDD(Prefix.parse("1.2.3.12/32"));
    expectedOut = _dstIpSpaceToBdd.toBDD(Prefix.parse("5.5.0.32/32"));
    actualOut = transition.transitForward(in);
    assertThat(actualOut, equalTo(expectedOut));

    // forward -- inside prefix
    actualOut = transition.transitForward(expectedOut);
    assertThat(actualOut, equalTo(expectedOut));

    // backward -- unconstrained
    BDD expectedIn = _one;
    BDD actualIn = transition.transitBackward(_one);
    assertThat(actualIn, equalTo(expectedIn));

    // backward -- constrained
    expectedIn = _one;
    actualIn = transition.transitBackward(expectedOut);
    assertThat(actualIn, equalTo(expectedIn));
  }

  @Test
  public void testGuardAndTransformSameField() {
    Prefix guardPrefix = Prefix.parse("1.0.0.0/8");
    Prefix shiftIntoPrefix = Prefix.parse("5.5.0.0/16");
    Transformation transformation =
        when(matchDst(guardPrefix)).apply(shiftDestinationIp(shiftIntoPrefix)).build();
    Transition transition = _toTransition.toTransition(transformation);

    BDD guardBdd = _dstIpSpaceToBdd.toBDD(guardPrefix);

    // forward -- unconstrained
    BDD expectedOut = guardBdd.not();
    BDD actualOut = transition.transitForward(_one);
    assertThat(actualOut, equalTo(expectedOut));

    // forward -- matching guard
    BDD in = _dstIpSpaceToBdd.toBDD(Prefix.parse("1.2.3.0/24"));
    expectedOut = _dstIpSpaceToBdd.toBDD(Prefix.parse("5.5.3.0/24"));
    actualOut = transition.transitForward(in);
    assertThat(actualOut, equalTo(expectedOut));

    // forward -- not matching guard
    in = _dstIpSpaceToBdd.toBDD(Prefix.parse("2.2.3.0/24"));
    expectedOut = in;
    actualOut = transition.transitForward(in);
    assertThat(actualOut, equalTo(expectedOut));

    // backward -- unconstrained
    BDD expectedIn = _one;
    BDD actualIn = transition.transitBackward(_one);
    assertThat(actualIn, equalTo(expectedIn));

    // backward -- matched and transformed or not matched
    BDD out = _dstIpSpaceToBdd.toBDD(Prefix.parse("5.5.3.0/24"));
    expectedIn =
        out.or(
            _dstIpSpaceToBdd.toBDD(
                IpWildcard.ipWithWildcardMask(Ip.parse("1.0.3.0"), Ip.parse("0.255.0.255"))));
    actualIn = transition.transitBackward(out);
    assertThat(actualIn, equalTo(expectedIn));
  }

  @Test
  public void testGuardAndTransformDifferentFields() {
    Prefix guardPrefix = Prefix.parse("1.0.0.0/8");
    Prefix shiftIntoPrefix = Prefix.parse("5.5.0.0/16");
    Transformation transformation =
        when(matchSrc(guardPrefix)).apply(shiftDestinationIp(shiftIntoPrefix)).build();
    Transition transition = _toTransition.toTransition(transformation);

    BDD guardBdd = _srcIpSpaceToBdd.toBDD(guardPrefix);
    BDD shiftIntoBdd = _dstIpSpaceToBdd.toBDD(shiftIntoPrefix);

    // forward -- unconstrained
    BDD expectedOut = guardBdd.imp(shiftIntoBdd);
    BDD actualOut = transition.transitForward(_one);
    assertThat(actualOut, equalTo(expectedOut));

    // forward -- matching guard
    BDD in = _dstIpSpaceToBdd.toBDD(Prefix.parse("1.2.3.0/24"));
    expectedOut = guardBdd.ite(_dstIpSpaceToBdd.toBDD(Prefix.parse("5.5.3.0/24")), in);
    actualOut = transition.transitForward(in);
    assertThat(actualOut, equalTo(expectedOut));

    // forward -- not matching guard
    in = _srcIpSpaceToBdd.toBDD(Prefix.parse("2.2.3.0/24"));
    expectedOut = in;
    actualOut = transition.transitForward(in);
    assertThat(actualOut, equalTo(expectedOut));

    // backward -- unconstrained
    BDD expectedIn = _one;
    BDD actualIn = transition.transitBackward(_one);
    assertThat(actualIn, equalTo(expectedIn));

    // backward -- matched and transformed or not matched
    BDD out = _dstIpSpaceToBdd.toBDD(Prefix.parse("5.5.3.0/24"));
    IpWildcard preTransformationDestIps =
        IpWildcard.ipWithWildcardMask(Ip.parse("0.0.3.0"), Ip.parse("255.255.0.255"));
    expectedIn = guardBdd.ite(_dstIpSpaceToBdd.toBDD(preTransformationDestIps), out);
    actualIn = transition.transitBackward(out);
    assertThat(actualIn, equalTo(expectedIn));
  }

  @Test
  public void testAssignFromPool() {
    Ip poolStart = Ip.parse("1.1.1.5");
    Ip poolEnd = Ip.parse("1.1.1.13");
    Transformation transformation = always().apply(assignSourceIp(poolStart, poolEnd)).build();
    Transition transition = _toTransition.toTransition(transformation);

    // the entire pool as a BDD
    BDD poolBdd =
        _pkt.getSrcIp().geq(poolStart.asLong()).and(_pkt.getSrcIp().leq(poolEnd.asLong()));
    // one IP in the pool as a BDD
    BDD poolIpBdd = _pkt.getSrcIp().value(poolStart.asLong() + 2);
    BDD nonPoolIpBdd = _pkt.getSrcIp().value(poolEnd.asLong() + 2);

    // forward -- unconstrainted
    BDD expectedOut = poolBdd;
    BDD actualOut = transition.transitForward(_one);
    assertThat(actualOut, equalTo(expectedOut));

    // forward -- already in pool
    expectedOut = poolBdd;
    actualOut = transition.transitForward(poolIpBdd);
    assertThat(actualOut, equalTo(expectedOut));

    // backward -- inside of pool
    BDD expectedIn = _one;
    BDD actualIn = transition.transitBackward(poolIpBdd);
    assertThat(actualIn, equalTo(expectedIn));

    // backward -- outside of pool
    expectedIn = _zero;
    actualIn = transition.transitBackward(nonPoolIpBdd);
    assertThat(actualIn, equalTo(expectedIn));
  }

  @Test
  public void testMultipleSteps() {
    Ip dstPoolIp = Ip.parse("1.1.1.1");
    Ip srcPoolIp = Ip.parse("2.2.2.2");

    Transformation transformation =
        always()
            .apply(assignDestinationIp(dstPoolIp, dstPoolIp), assignSourceIp(srcPoolIp, srcPoolIp))
            .build();

    Transition transition = _toTransition.toTransition(transformation);

    BDD dstPoolIpBdd = _pkt.getDstIp().value(dstPoolIp.asLong());
    BDD srcPoolIpBdd = _pkt.getSrcIp().value(srcPoolIp.asLong());

    assertThat(transition.transitForward(_one), equalTo(dstPoolIpBdd.and(srcPoolIpBdd)));
  }

  @Test
  public void testMultipleTransformations() {
    Ip matchDstIp = Ip.parse("1.1.1.1");
    Ip matchSrcIp = Ip.parse("2.2.2.2");
    Ip truePoolIp = Ip.parse("3.3.3.3");
    Ip falsePoolIp = Ip.parse("4.4.4.4");
    Transformation transformation =
        when(matchDst(matchDstIp))
            .apply(NOOP_SOURCE_NAT)
            .setAndThen(
                when(matchSrc(matchSrcIp)).apply(assignSourceIp(truePoolIp, truePoolIp)).build())
            .setOrElse(
                when(matchSrc(matchSrcIp)).apply(assignSourceIp(falsePoolIp, falsePoolIp)).build())
            .build();

    Transition transition = _toTransition.toTransition(transformation);

    BDD matchDstIpBdd = _pkt.getDstIp().value(matchDstIp.asLong());
    BDD notMatchDstIpBdd = matchDstIpBdd.not();
    BDD matchSrcIpBdd = _pkt.getSrcIp().value(matchSrcIp.asLong());
    BDD notMatchSrcIpBdd = matchSrcIpBdd.not();
    BDD truePoolIpBdd = _pkt.getSrcIp().value(truePoolIp.asLong());
    BDD falsePoolIpBdd = _pkt.getSrcIp().value(falsePoolIp.asLong());

    assertThat(
        transition.transitForward(matchDstIpBdd.and(matchSrcIpBdd)),
        equalTo(matchDstIpBdd.and(truePoolIpBdd)));

    assertThat(
        transition.transitForward(matchDstIpBdd.and(notMatchSrcIpBdd)),
        equalTo(matchDstIpBdd.and(notMatchSrcIpBdd)));

    assertThat(
        transition.transitForward(notMatchDstIpBdd.and(matchSrcIpBdd)),
        equalTo(notMatchDstIpBdd.and(falsePoolIpBdd)));

    assertThat(
        transition.transitForward(notMatchDstIpBdd.and(notMatchSrcIpBdd)),
        equalTo(notMatchDstIpBdd.and(notMatchSrcIpBdd)));
  }

  @Test
  public void testAssignFromPatPool() {
    int poolStart = 2000;
    int poolEnd = 3000;
    Transformation transformation = always().apply(assignSourcePort(poolStart, poolEnd)).build();
    Transition transition = _toTransition.toTransition(transformation);

    // the entire pool as a BDD
    BDD poolBdd = _pkt.getSrcPort().geq(poolStart).and(_pkt.getSrcPort().leq(poolEnd));
    // one port in the pool as a BDD
    BDD nonPoolPortBdd = _pkt.getSrcPort().value(poolEnd + 2);

    // forward -- unconstrainted
    BDD expectedOut = _portTransformationProtocols.imp(poolBdd);
    BDD actualOut = transition.transitForward(_one);
    assertThat(actualOut, equalTo(expectedOut));

    // forward -- already in pool
    expectedOut = _portTransformationProtocols.imp(poolBdd);
    actualOut = transition.transitForward(expectedOut);
    assertThat(actualOut, equalTo(expectedOut));

    // forward -- protocol with no ports
    expectedOut = _portTransformationProtocols.not();
    actualOut = transition.transitForward(expectedOut);
    assertThat(actualOut, equalTo(expectedOut));

    // backward -- inside of pool if protocol has ports
    BDD expectedIn = _one;
    BDD actualIn = transition.transitBackward(_portTransformationProtocols.imp(poolBdd));
    assertThat(actualIn, equalTo(expectedIn));

    // backward -- outside of pool
    expectedIn = _zero;
    actualIn = transition.transitBackward(_portTransformationProtocols.and(nonPoolPortBdd));
    assertThat(actualIn, equalTo(expectedIn));

    // backward -- protocol with no ports
    expectedIn = _portTransformationProtocols.not();
    actualIn = transition.transitBackward(expectedIn);
    assertThat(actualIn, equalTo(expectedIn));
  }

  @Test
  public void testAssignFromPoolBothIpAndPort() {
    int poolPort = 2000;
    Ip poolIp = Ip.parse("1.1.1.1");
    Transformation transformation =
        always()
            .apply(assignSourceIp(poolIp, poolIp), assignSourcePort(poolPort, poolPort))
            .build();

    Transition transition = _toTransition.toTransition(transformation);

    BDD ipPoolBdd = _pkt.getSrcIp().value(poolIp.asLong());
    BDD portPoolBdd = _pkt.getSrcPort().value(poolPort);
    BDD nonIpPoolBdd = _pkt.getSrcIp().value(poolIp.asLong() + 1);
    BDD nonPortPoolBdd = _pkt.getSrcPort().value(poolPort + 1);
    BDD actualOut = transition.transitForward(_one);
    assertThat(actualOut, equalTo(ipPoolBdd.and(_portTransformationProtocols.imp(portPoolBdd))));

    BDD expectedIn = _one;
    BDD actualIn =
        transition.transitBackward(ipPoolBdd.and(_portTransformationProtocols.imp(portPoolBdd)));
    assertThat(actualIn, equalTo(expectedIn));

    expectedIn = _zero;
    actualIn =
        transition.transitBackward(ipPoolBdd.and(_portTransformationProtocols).and(nonPortPoolBdd));
    assertThat(actualIn, equalTo(expectedIn));

    expectedIn = _zero;
    actualIn = transition.transitBackward(nonIpPoolBdd.and(portPoolBdd));
    assertThat(actualIn, equalTo(expectedIn));
  }

  @Test
  public void testReturnFlowTransition() {
    IpAccessListToBddImpl aclToBdd =
        new IpAccessListToBddImpl(
            _pkt, BDDSourceManager.empty(_pkt), ImmutableMap.of(), ImmutableMap.of());
    TransformationToTransition toTransition = new TransformationToTransition(_pkt, aclToBdd);
    IpSpaceToBDD dstToBdd = aclToBdd.getHeaderSpaceToBDD().getDstIpSpaceToBdd();

    // Shift source into prefix
    {
      Prefix shiftPrefix = Prefix.parse("6.6.0.0/16");
      Transition transition =
          toTransition.toReturnFlowTransition(always().apply(shiftSourceIp(shiftPrefix)).build());

      // everything gets shifted into the prefix
      assertThat(transition.transitForward(dstToBdd.toBDD(shiftPrefix)), equalTo(_one));
      assertThat(transition.transitForward(dstToBdd.toBDD(shiftPrefix).not()), equalTo(_zero));
      assertThat(
          transition.transitForward(dstToBdd.toBDD(Ip.parse("6.6.6.6"))),
          equalTo(
              dstToBdd.toBDD(
                  IpWildcard.ipWithWildcardMask(Ip.parse("0.0.6.6"), Ip.parse("255.255.0.0")))));

      assertEquals(
          transition.transitBackward(dstToBdd.toBDD(Ip.parse("5.5.6.6"))),
          dstToBdd.toBDD(Ip.parse("6.6.6.6")));
    }

    // Assign source from pool
    {
      Ip poolStart = Ip.parse("6.6.6.3");
      Ip poolEnd = Ip.parse("6.6.6.9");
      Transition transition =
          toTransition.toReturnFlowTransition(
              always().apply(assignSourceIp(poolStart, poolEnd)).build());
      BDDInteger dstIp = dstToBdd.getBDDInteger();
      BDD poolBdd = dstIp.geq(poolStart.asLong()).and(dstIp.leq(poolEnd.asLong()));

      // everything gets mapped into the pool
      assertThat(transition.transitForward(poolBdd), equalTo(_one));
      assertThat(transition.transitForward(poolBdd.not()), equalTo(_zero));

      assertEquals(transition.transitBackward(dstToBdd.toBDD(Ip.parse("5.5.6.6"))), poolBdd);
    }
  }
}
