package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_SECURITY_GROUPS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

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
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

/** Tests for {@link SecurityGroup}. */
public class SecurityGroupsTest {

  @Test
  public void testIpPermToAcl() throws JSONException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/SecurityGroupTest.json");
    JSONObject jObj = new JSONObject(text);
    SecurityGroup sg =
        new SecurityGroup(jObj.getJSONArray(JSON_KEY_SECURITY_GROUPS).getJSONObject(0), null);

    List<IpAccessListLine> inboundRules = new LinkedList<>();
    List<IpAccessListLine> outboundRules = new LinkedList<>();

    sg.addInOutAccessLines(inboundRules, outboundRules);

    // for inbound rule srcIp/dstPort must be set and for outbound rule dstIp/dstPort
    // must be set
    assertThat(
        inboundRules,
        equalTo(
            Lists.newArrayList(
                IpAccessListLine.builder()
                    .setAction(LineAction.ACCEPT)
                    .setIpProtocols(Sets.newHashSet(IpProtocol.TCP))
                    .setSrcIps(Sets.newHashSet(new IpWildcard("0.0.0.0/0")))
                    .setDstPorts(Sets.newHashSet(new SubRange(12345, 12347)))
                    .build())));
    assertThat(
        outboundRules,
        equalTo(
            Lists.newArrayList(
                IpAccessListLine.builder()
                    .setAction(LineAction.ACCEPT)
                    .setDstIps(Sets.newHashSet(new IpWildcard("1.2.3.4/32")))
                    .setDstPorts(Sets.newHashSet(new SubRange(22, 22)))
                    .build())));
  }
}
