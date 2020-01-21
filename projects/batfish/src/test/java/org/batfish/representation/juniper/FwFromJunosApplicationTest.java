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
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Test;

/** Test for {@link FwFromJunosApplication} */
public class FwFromJunosApplicationTest {

  @Test
  public void testApplyTo_bgp() {
    FwFromJunosApplication from = new FwFromJunosApplication(JunosApplication.JUNOS_BGP);

    List<ExprAclLine> lines = new ArrayList<>();
    HeaderSpace.Builder hsBuilder = HeaderSpace.builder();

    from.applyTo(null, hsBuilder, LineAction.PERMIT, lines, null);

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
                                ImmutableSet.of(
                                    new SubRange(NamedPort.BGP.number(), NamedPort.BGP.number())))
                            .build()),
                    null,
                    new BaseApplication(JunosApplication.JUNOS_BGP.name()).getTraceElement()))));
  }

  @Test
  public void testApplyTo_any() {
    FwFromJunosApplication from = new FwFromJunosApplication(JunosApplication.ANY);

    List<ExprAclLine> lines = new ArrayList<>();
    HeaderSpace.Builder hsBuilder = HeaderSpace.builder();

    from.applyTo(null, hsBuilder, LineAction.PERMIT, lines, null);

    assertThat(
        lines,
        equalTo(
            ImmutableList.of(
                new ExprAclLine(
                    LineAction.PERMIT,
                    new MatchHeaderSpace(HeaderSpace.builder().build()),
                    null,
                    new BaseApplication(JunosApplication.ANY.name()).getTraceElement()))));
  }
}
