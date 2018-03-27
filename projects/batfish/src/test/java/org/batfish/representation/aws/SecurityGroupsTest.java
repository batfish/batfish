package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_SECURITY_GROUPS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlags;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link SecurityGroup}. */
public class SecurityGroupsTest {

  private JSONArray _securityGroups;
  private IpAccessListLine _rejectSynOnly;
  private IpAccessListLine _allowAllReverseOutboundRule;

  @Before
  public void setup() throws JSONException {
    _securityGroups =
        new JSONObject(
                CommonUtil.readResource("org/batfish/representation/aws/SecurityGroupTest.json"))
            .getJSONArray(JSON_KEY_SECURITY_GROUPS);
    _rejectSynOnly =
        IpAccessListLine.builder()
            .setTcpFlags(ImmutableSet.of(TcpFlags.builder().setAck(false).setSyn(true).build()))
            .setAction(LineAction.REJECT)
            .build();
    _allowAllReverseOutboundRule =
        IpAccessListLine.builder()
            .setAction(LineAction.ACCEPT)
            .setSrcIps(Sets.newHashSet(new IpWildcard("0.0.0.0/0")))
            .build();
  }

  @Test
  public void testSinglePort() throws JSONException {
    SecurityGroup sg = new SecurityGroup(_securityGroups.getJSONObject(0), null);

    List<IpAccessListLine> inboundRules = new LinkedList<>();
    List<IpAccessListLine> outboundRules = new LinkedList<>();

    sg.addInOutAccessLines(inboundRules, outboundRules);

    assertThat(
        inboundRules,
        equalTo(
            Lists.newArrayList(
                IpAccessListLine.builder()
                    .setAction(LineAction.ACCEPT)
                    .setIpProtocols(Sets.newHashSet(IpProtocol.TCP))
                    .setSrcIps(Sets.newHashSet(new IpWildcard("1.2.3.4/32")))
                    .setDstPorts(Sets.newHashSet(new SubRange(22, 22)))
                    .build(),
                _rejectSynOnly,
                _allowAllReverseOutboundRule)));
  }

  @Test
  public void testBeginningHalfOpenInterval() throws JSONException {
    SecurityGroup sg = new SecurityGroup(_securityGroups.getJSONObject(1), null);

    List<IpAccessListLine> inboundRules = new LinkedList<>();
    List<IpAccessListLine> outboundRules = new LinkedList<>();

    sg.addInOutAccessLines(inboundRules, outboundRules);

    assertThat(
        inboundRules,
        equalTo(
            Lists.newArrayList(
                IpAccessListLine.builder()
                    .setAction(LineAction.ACCEPT)
                    .setIpProtocols(Sets.newHashSet(IpProtocol.TCP))
                    .setSrcIps(Sets.newHashSet(new IpWildcard("1.2.3.4/32")))
                    .setDstPorts(Sets.newHashSet(new SubRange(0, 22)))
                    .build(),
                _rejectSynOnly,
                _allowAllReverseOutboundRule)));
  }

  @Test
  public void testEndHalfOpenInterval() throws JSONException {
    SecurityGroup sg = new SecurityGroup(_securityGroups.getJSONObject(2), null);

    List<IpAccessListLine> inboundRules = new LinkedList<>();
    List<IpAccessListLine> outboundRules = new LinkedList<>();

    sg.addInOutAccessLines(inboundRules, outboundRules);

    assertThat(
        inboundRules,
        equalTo(
            Lists.newArrayList(
                IpAccessListLine.builder()
                    .setAction(LineAction.ACCEPT)
                    .setIpProtocols(Sets.newHashSet(IpProtocol.TCP))
                    .setSrcIps(Sets.newHashSet(new IpWildcard("1.2.3.4/32")))
                    .setDstPorts(Sets.newHashSet(new SubRange(65530, 65535)))
                    .build(),
                _rejectSynOnly,
                _allowAllReverseOutboundRule)));
  }

  @Test
  public void testFullInterval() throws JSONException {
    SecurityGroup sg = new SecurityGroup(_securityGroups.getJSONObject(3), null);

    List<IpAccessListLine> inboundRules = new LinkedList<>();
    List<IpAccessListLine> outboundRules = new LinkedList<>();

    sg.addInOutAccessLines(inboundRules, outboundRules);

    assertThat(
        inboundRules,
        equalTo(
            Lists.newArrayList(
                IpAccessListLine.builder()
                    .setAction(LineAction.ACCEPT)
                    .setIpProtocols(Sets.newHashSet(IpProtocol.TCP))
                    .setSrcIps(Sets.newHashSet(new IpWildcard("1.2.3.4/32")))
                    .build(),
                _rejectSynOnly,
                _allowAllReverseOutboundRule)));
  }

  @Test
  public void testAllTrafficAllowed() throws JSONException {
    SecurityGroup sg = new SecurityGroup(_securityGroups.getJSONObject(4), null);

    List<IpAccessListLine> inboundRules = new LinkedList<>();
    List<IpAccessListLine> outboundRules = new LinkedList<>();

    sg.addInOutAccessLines(inboundRules, outboundRules);

    assertThat(
        inboundRules,
        equalTo(
            Lists.newArrayList(
                IpAccessListLine.builder()
                    .setAction(LineAction.ACCEPT)
                    .setSrcIps(Sets.newHashSet(new IpWildcard("0.0.0.0/0")))
                    .setDstPorts(Sets.newHashSet())
                    .build(),
                _rejectSynOnly,
                _allowAllReverseOutboundRule)));
  }

  @Test
  public void testClosedInterval() throws JSONException {
    SecurityGroup sg = new SecurityGroup(_securityGroups.getJSONObject(5), null);

    List<IpAccessListLine> inboundRules = new LinkedList<>();
    List<IpAccessListLine> outboundRules = new LinkedList<>();

    sg.addInOutAccessLines(inboundRules, outboundRules);

    assertThat(
        inboundRules,
        equalTo(
            Lists.newArrayList(
                IpAccessListLine.builder()
                    .setAction(LineAction.ACCEPT)
                    .setIpProtocols(Sets.newHashSet(IpProtocol.TCP))
                    .setSrcIps(Sets.newHashSet(new IpWildcard("1.2.3.4/32")))
                    .setDstPorts(Sets.newHashSet(new SubRange(45, 50)))
                    .build(),
                _rejectSynOnly,
                _allowAllReverseOutboundRule)));
  }

  @Test
  public void testInvalidStartInterval() throws JSONException {
    SecurityGroup sg = new SecurityGroup(_securityGroups.getJSONObject(6), null);

    List<IpAccessListLine> inboundRules = new LinkedList<>();
    List<IpAccessListLine> outboundRules = new LinkedList<>();

    sg.addInOutAccessLines(inboundRules, outboundRules);

    assertThat(
        inboundRules,
        equalTo(
            Lists.newArrayList(
                IpAccessListLine.builder()
                    .setAction(LineAction.ACCEPT)
                    .setIpProtocols(Sets.newHashSet(IpProtocol.TCP))
                    .setSrcIps(Sets.newHashSet(new IpWildcard("1.2.3.4/32")))
                    .setDstPorts(Sets.newHashSet(new SubRange(0, 50)))
                    .build(),
                _rejectSynOnly,
                _allowAllReverseOutboundRule)));
  }

  @Test
  public void testInvalidEndInterval() throws JSONException {
    SecurityGroup sg = new SecurityGroup(_securityGroups.getJSONObject(7), null);

    List<IpAccessListLine> inboundRules = new LinkedList<>();
    List<IpAccessListLine> outboundRules = new LinkedList<>();

    sg.addInOutAccessLines(inboundRules, outboundRules);

    assertThat(
        inboundRules,
        equalTo(
            Lists.newArrayList(
                IpAccessListLine.builder()
                    .setAction(LineAction.ACCEPT)
                    .setIpProtocols(Sets.newHashSet(IpProtocol.TCP))
                    .setSrcIps(Sets.newHashSet(new IpWildcard("1.2.3.4/32")))
                    .setDstPorts(Sets.newHashSet(new SubRange(30, 65535)))
                    .build(),
                _rejectSynOnly,
                _allowAllReverseOutboundRule)));
  }

  @Test
  public void testStatefulTcpRules() throws JSONException {
    SecurityGroup sg = new SecurityGroup(_securityGroups.getJSONObject(8), null);

    List<IpAccessListLine> inboundRules = new LinkedList<>();
    List<IpAccessListLine> outboundRules = new LinkedList<>();

    sg.addInOutAccessLines(inboundRules, outboundRules);

    assertThat(
        inboundRules,
        equalTo(
            Lists.newArrayList(
                IpAccessListLine.builder()
                    .setAction(LineAction.ACCEPT)
                    .setIpProtocols(Sets.newHashSet(IpProtocol.TCP))
                    .setSrcIps(Sets.newHashSet(new IpWildcard("1.2.3.4/32")))
                    .setDstPorts(Sets.newHashSet(new SubRange(22, 22)))
                    .build(),
                _rejectSynOnly,
                // reverse of outbound rule
                IpAccessListLine.builder()
                    .setAction(LineAction.ACCEPT)
                    .setIpProtocols(Sets.newHashSet(IpProtocol.TCP))
                    .setSrcIps(Sets.newHashSet(new IpWildcard("5.6.7.8/32")))
                    .setSrcPorts(Sets.newHashSet(new SubRange(80, 80)))
                    .build())));
    assertThat(
        outboundRules,
        equalTo(
            Lists.newArrayList(
                IpAccessListLine.builder()
                    .setAction(LineAction.ACCEPT)
                    .setIpProtocols(Sets.newHashSet(IpProtocol.TCP))
                    .setDstIps(Sets.newHashSet(new IpWildcard("5.6.7.8/32")))
                    .setDstPorts(Sets.newHashSet(new SubRange(80, 80)))
                    .build(),
                _rejectSynOnly,
                // reverse of inbound rule
                IpAccessListLine.builder()
                    .setAction(LineAction.ACCEPT)
                    .setIpProtocols(Sets.newHashSet(IpProtocol.TCP))
                    .setDstIps(Sets.newHashSet(new IpWildcard("1.2.3.4/32")))
                    .setSrcPorts(Sets.newHashSet(new SubRange(22, 22)))
                    .build())));
  }
}
