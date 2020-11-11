package org.batfish.dataplane.ibdp;

import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.eigrp.EigrpProcess;
import org.batfish.datamodel.eigrp.EigrpProcessMode;
import org.batfish.dataplane.rib.RibDelta;
import org.junit.Before;
import org.junit.Test;

/** Tests of {@link EigrpRoutingProcess} */
public class EigrpRoutingProcessTest {

  EigrpProcess _process;
  EigrpRoutingProcess _routingProcess;

  @Before
  public void setUp() {
    _process =
        EigrpProcess.builder()
            .setAsNumber(1)
            .setMode(EigrpProcessMode.CLASSIC)
            .setRouterId(Ip.ZERO)
            .build();
    _routingProcess =
        new EigrpRoutingProcess(
            _process, "vrf", new Configuration("host", ConfigurationFormat.CISCO_IOS));
  }

  @Test
  public void testRedistributeNoPolicy() {
    // Do not crash
    _routingProcess.redistribute(
        RibDelta.adding(
            new AnnotatedRoute<>(
                ConnectedRoute.builder()
                    .setNetwork(Prefix.parse("1.1.1.0/24"))
                    .setNextHopInterface("Eth0")
                    .build(),
                "vrf")));
  }
}
