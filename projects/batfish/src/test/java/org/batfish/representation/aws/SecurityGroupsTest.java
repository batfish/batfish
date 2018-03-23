package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_SECURITY_GROUPS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.SubRange;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link SecurityGroup}. */
public class SecurityGroupsTest {

  private JSONArray _securityGroups;
  private Warnings _warnings;

  @Before
  public void setup() throws JSONException {
    _securityGroups =
        new JSONObject(
                CommonUtil.readResource("org/batfish/representation/aws/SecurityGroupTest.json"))
            .getJSONArray(JSON_KEY_SECURITY_GROUPS);
    _warnings = new Warnings();
  }

  @Test
  public void testSinglePort() throws JSONException {
    SecurityGroup sg = new SecurityGroup(_securityGroups.getJSONObject(0), null);

    List<IpAccessListLine> inboundRules = new LinkedList<>();
    List<IpAccessListLine> outboundRules = new LinkedList<>();

    sg.addInOutAccessLines(inboundRules, outboundRules, _warnings);

    assertThat(
        inboundRules,
        equalTo(
            Lists.newArrayList(
                IpAccessListLine.builder()
                    .setAction(LineAction.ACCEPT)
                    .setIpProtocols(Sets.newHashSet(IpProtocol.TCP))
                    .setSrcIps(Sets.newHashSet(new IpWildcard("1.2.3.4/32")))
                    .setDstPorts(Sets.newHashSet(new SubRange(22, 22)))
                    .build())));
  }

  @Test
  public void testBeginningHalfOpenInterval() throws JSONException {
    SecurityGroup sg = new SecurityGroup(_securityGroups.getJSONObject(1), null);

    List<IpAccessListLine> inboundRules = new LinkedList<>();
    List<IpAccessListLine> outboundRules = new LinkedList<>();

    sg.addInOutAccessLines(inboundRules, outboundRules, _warnings);

    assertThat(
        inboundRules,
        equalTo(
            Lists.newArrayList(
                IpAccessListLine.builder()
                    .setAction(LineAction.ACCEPT)
                    .setIpProtocols(Sets.newHashSet(IpProtocol.TCP))
                    .setSrcIps(Sets.newHashSet(new IpWildcard("1.2.3.4/32")))
                    .setDstPorts(Sets.newHashSet(new SubRange(0, 22)))
                    .build())));
  }

  @Test
  public void testEndHalfOpenInterval() throws JSONException {
    SecurityGroup sg = new SecurityGroup(_securityGroups.getJSONObject(2), null);

    List<IpAccessListLine> inboundRules = new LinkedList<>();
    List<IpAccessListLine> outboundRules = new LinkedList<>();

    sg.addInOutAccessLines(inboundRules, outboundRules, _warnings);

    assertThat(
        inboundRules,
        equalTo(
            Lists.newArrayList(
                IpAccessListLine.builder()
                    .setAction(LineAction.ACCEPT)
                    .setIpProtocols(Sets.newHashSet(IpProtocol.TCP))
                    .setSrcIps(Sets.newHashSet(new IpWildcard("1.2.3.4/32")))
                    .setDstPorts(Sets.newHashSet(new SubRange(65530, 65535)))
                    .build())));
  }

  @Test
  public void testFullInterval() throws JSONException {
    SecurityGroup sg = new SecurityGroup(_securityGroups.getJSONObject(3), null);

    List<IpAccessListLine> inboundRules = new LinkedList<>();
    List<IpAccessListLine> outboundRules = new LinkedList<>();

    sg.addInOutAccessLines(inboundRules, outboundRules, _warnings);

    assertThat(
        inboundRules,
        equalTo(
            Lists.newArrayList(
                IpAccessListLine.builder()
                    .setAction(LineAction.ACCEPT)
                    .setIpProtocols(Sets.newHashSet(IpProtocol.TCP))
                    .setSrcIps(Sets.newHashSet(new IpWildcard("1.2.3.4/32")))
                    .setDstPorts(Sets.newHashSet(new SubRange(0, 65535)))
                    .build())));
  }

  @Test
  public void testAllTrafficAllowed() throws JSONException {
    SecurityGroup sg = new SecurityGroup(_securityGroups.getJSONObject(4), null);

    List<IpAccessListLine> inboundRules = new LinkedList<>();
    List<IpAccessListLine> outboundRules = new LinkedList<>();

    sg.addInOutAccessLines(inboundRules, outboundRules, _warnings);

    assertThat(
        inboundRules,
        equalTo(
            Lists.newArrayList(
                IpAccessListLine.builder()
                    .setAction(LineAction.ACCEPT)
                    .setSrcIps(Sets.newHashSet(new IpWildcard("0.0.0.0/0")))
                    .setDstPorts(Sets.newHashSet())
                    .build())));
  }

  @Test
  public void testClosedInterval() throws JSONException {
    SecurityGroup sg = new SecurityGroup(_securityGroups.getJSONObject(5), null);

    List<IpAccessListLine> inboundRules = new LinkedList<>();
    List<IpAccessListLine> outboundRules = new LinkedList<>();

    sg.addInOutAccessLines(inboundRules, outboundRules, _warnings);

    assertThat(
        inboundRules,
        equalTo(
            Lists.newArrayList(
                IpAccessListLine.builder()
                    .setAction(LineAction.ACCEPT)
                    .setIpProtocols(Sets.newHashSet(IpProtocol.TCP))
                    .setSrcIps(Sets.newHashSet(new IpWildcard("1.2.3.4/32")))
                    .setDstPorts(Sets.newHashSet(new SubRange(45, 50)))
                    .build())));
  }
}
