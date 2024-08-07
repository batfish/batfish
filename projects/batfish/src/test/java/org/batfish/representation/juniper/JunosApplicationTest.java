package org.batfish.representation.juniper;

import static org.batfish.representation.juniper.JunosApplication.getTraceElementForBuiltInApplication;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.junit.Before;
import org.junit.Test;

/** Test for {@link JunosApplication} */
public class JunosApplicationTest {

  private final JuniperConfiguration _jc = new JuniperConfiguration();

  @Before
  public void setUp() {
    _jc.setFilename("host");
  }

  @Test
  public void testToAclLineMatchExpr_bgp() {
    assertThat(
        JunosApplication.JUNOS_BGP.toAclLineMatchExpr(_jc, null),
        equalTo(
            new OrMatchExpr(
                ImmutableList.of(
                    new MatchHeaderSpace(
                        HeaderSpace.builder()
                            .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
                            .setDstPorts(
                                ImmutableSet.of(SubRange.singleton(NamedPort.BGP.number())))
                            .build())),
                getTraceElementForBuiltInApplication(
                    JunosApplication.JUNOS_BGP.getJuniperName()))));
  }

  @Test
  public void testToAclLineMatchExpr_smb() {
    // this application has multiple terms. since all terms are generated by BF, the tracing element
    // should only show the application name
    assertThat(
        JunosApplication.JUNOS_SMB.toAclLineMatchExpr(_jc, null),
        equalTo(
            new OrMatchExpr(
                ImmutableList.of(
                    new MatchHeaderSpace(
                        HeaderSpace.builder()
                            .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
                            .setDstPorts(
                                ImmutableSet.of(
                                    SubRange.singleton(NamedPort.MICROSOFT_DS.number())))
                            .build()),
                    new MatchHeaderSpace(
                        HeaderSpace.builder()
                            .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
                            .setDstPorts(
                                ImmutableSet.of(SubRange.singleton(NamedPort.NETBIOS_SSN.number())))
                            .build())),
                getTraceElementForBuiltInApplication(
                    JunosApplication.JUNOS_SMB.getJuniperName()))));
  }

  @Test
  public void testToAclLineMatchExpr_any() {
    assertThat(
        JunosApplication.ANY.toAclLineMatchExpr(_jc, null),
        equalTo(
            new OrMatchExpr(
                ImmutableList.of(new MatchHeaderSpace(HeaderSpace.builder().build())),
                getTraceElementForBuiltInApplication(JunosApplication.ANY.getJuniperName()))));
  }
}
