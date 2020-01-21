package org.batfish.representation.juniper;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Test;

/** Test for {@link FwFromHostProtocol} */
public class FwFromHostProtocolTest {

  @Test
  public void testApplyTo_bgp() {
    FwFromHostProtocol from = new FwFromHostProtocol(HostProtocol.BGP);
    List<ExprAclLine> lines = new ArrayList<>();
    from.applyTo(lines, null);

    assertThat(
        lines,
        equalTo(
            ImmutableList.of(
                new ExprAclLine(
                    LineAction.PERMIT,
                    new MatchHeaderSpace(
                        HeaderSpace.builder()
                            .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
                            .setDstPorts(
                                ImmutableSet.of(SubRange.singleton(NamedPort.BGP.number())))
                            .build()),
                    null,
                    TraceElement.of("Matched host-inbound-traffic protocol BGP")))));
  }

  @Test
  public void testApplyTo_all() {
    FwFromHostProtocol from = new FwFromHostProtocol(HostProtocol.ALL);
    List<ExprAclLine> lines = new ArrayList<>();
    from.applyTo(lines, null);

    assertThat(lines, equalTo(HostProtocol.ALL.getLines()));
  }
}
