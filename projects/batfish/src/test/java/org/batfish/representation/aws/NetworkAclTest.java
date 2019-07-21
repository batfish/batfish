package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_NETWORK_ACLS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Prefix;
import org.batfish.representation.aws.NetworkAcl.NetworkAclAssociation;
import org.junit.Test;

/** Tests for {@link NetworkAcl} */
public class NetworkAclTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/NetworkAclTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode array = (ArrayNode) json.get(JSON_KEY_NETWORK_ACLS);
    List<NetworkAcl> networkAcls = new LinkedList<>();

    for (int index = 0; index < array.size(); index++) {
      networkAcls.add(
          BatfishObjectMapper.mapper().convertValue(array.get(index), NetworkAcl.class));
    }

    assertThat(
        networkAcls,
        equalTo(
            ImmutableList.of(
                new NetworkAcl(
                    "acl-4db39c28",
                    "vpc-f8fad69d",
                    ImmutableList.of(new NetworkAclAssociation("subnet-1f315846")),
                    ImmutableList.of(
                        new NetworkAclEntry(Prefix.parse("0.0.0.0/0"), true, true, "-1", 100, null),
                        new NetworkAclEntry(
                            Prefix.parse("162.243.144.192/32"),
                            false,
                            false,
                            "6",
                            100,
                            new PortRange(3306, 3306)))))));
  }
}
