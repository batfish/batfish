package org.batfish.bddreachability;

import static org.batfish.bddreachability.TestNetworkIndirection.DST_PREFIX;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Ip;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.symbolic.bdd.BDDInteger;
import org.batfish.symbolic.bdd.BDDPacket;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.PreOutEdge;
import org.batfish.z3.state.PreOutVrf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class BDDReachabilityAnalysisFactoryTest {
  @Rule public TemporaryFolder temp = new TemporaryFolder();

  private static final BDDPacket PKT = new BDDPacket();

  private BDDReachabilityAnalysisFactory _graphFactory;
  private TestNetworkIndirection _net;
  private Batfish _batfish;

  private Ip _dstIfaceIp;
  private String _dstName;
  private String _linkDstName;
  private String _srcName;
  private PreOutEdge _srcPreOutEdge;
  private PreOutVrf _srcPreOutVrf;

  @Before
  public void setup() throws IOException {
    _net = new TestNetworkIndirection();
    _batfish = BatfishTestUtils.getBatfish(_net._configs, temp);

    _batfish.computeDataPlane(false);
    DataPlane dataPlane = _batfish.loadDataPlane();
    _graphFactory =
        new BDDReachabilityAnalysisFactory(PKT, _net._configs, dataPlane.getForwardingAnalysis());

    _dstName = _net._dstNode.getHostname();
    _dstIfaceIp = DST_PREFIX.getStartIp();
    _linkDstName = _net._linkDst.getName();

    _srcName = _net._srcNode.getHostname();
    _srcPreOutEdge = new PreOutEdge(_srcName, _net._linkSrc.getName(), _dstName, _linkDstName);
    _srcPreOutVrf = new PreOutVrf(_srcName, DEFAULT_VRF_NAME);
  }

  private List<Ip> bddIps(BDD bdd) {
    BDDInteger bddInteger = _graphFactory.getIpSpaceToBDD().getBDDInteger();

    return bddInteger
        .getValuesSatisfying(bdd, 10)
        .stream()
        .map(Ip::new)
        .collect(Collectors.toList());
  }

  private BDD bddTransition(StateExpr preState, StateExpr postState) {
    return _graphFactory.getBDDTransitions().get(preState).get(postState);
  }

  @Test
  public void testBDDIps() {
    BDD preOutEdge = bddTransition(_srcPreOutVrf, _srcPreOutEdge);

    assertThat(
        bddIps(preOutEdge), containsInAnyOrder(_dstIfaceIp, _net._linkDst.getAddress().getIp()));
  }
}
