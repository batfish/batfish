package org.batfish.representation.juniper;

import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.matchers.IpAccessListMatchers;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

public class JunosApplicationTest {

  private HeaderSpace.Builder _hb;

  private List<IpAccessListLine> _lines;

  private IpAccessList.Builder _aclb;

  private Flow.Builder _fb;

  private Warnings _w = new Warnings();

  @Before
  public void setup() {
    _hb = HeaderSpace.builder();
    _lines = new ArrayList<>();
    _fb = Flow.builder().setTag("").setIngressNode("");
    _aclb = IpAccessList.builder().setName("acl");

  }

  private IpAccessList makeAcl(JunosApplication application) {
    application.applyTo(null, _hb, LineAction.ACCEPT, _lines, _w);
    return _aclb.setLines(_lines).build();
  }

  private static Matcher<IpAccessList> accepts(Flow flow) {
    return IpAccessListMatchers.accepts(flow, null, ImmutableMap.of(), ImmutableMap.of());
  }

  private static Matcher<IpAccessList> rejects(Flow flow) {
    return IpAccessListMatchers.rejects(flow, null, ImmutableMap.of(), ImmutableMap.of());
  }

  @Test
  public void testAolDefinition() {
    IpAccessList acl = makeAcl(JunosApplication.JUNOS_AOL);
    Flow startOfRange = _fb.setDstPort(NamedPort.AOL.number()).setIpProtocol(IpProtocol.TCP).build();
    Flow endOfRange = _fb.setDstPort(NamedPort.AOL.number()+3).setIpProtocol(IpProtocol.TCP).build();
    Flow invalid = _fb.setDstPort(5193).setIpProtocol(IpProtocol.UDP).build();
    assertThat(acl, accepts(startOfRange));
    assertThat(acl, accepts(endOfRange));
    assertThat(acl, rejects(invalid));

  }
}
