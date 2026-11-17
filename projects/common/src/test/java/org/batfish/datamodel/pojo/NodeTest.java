package org.batfish.datamodel.pojo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.DeviceType;
import org.junit.jupiter.api.Test;

class NodeTest {

  @Test
  void constructorFail() {
    String nodeStr = "{\"nonamefield\" : \"nodeName\"}";
    assertThrows(
        com.fasterxml.jackson.databind.exc.ValueInstantiationException.class,
        () -> BatfishObjectMapper.mapper().readValue(nodeStr, Node.class));
  }

  @Test
  void constructorBasic() throws IOException {
    String nodeStr = "{\"name\" : \"nodeName\"}";
    Node node = BatfishObjectMapper.mapper().readValue(nodeStr, Node.class);

    assertThat(node.getId(), equalTo("node-nodename"));
    assertThat(node.getName(), equalTo("nodename"));
  }

  @Test
  void constructorProperties() throws IOException {
    String nodeStr = "{\"name\" : \"nodeName\", \"properties\" : { \"key\": \"value\"}}";
    Node node = BatfishObjectMapper.mapper().readValue(nodeStr, Node.class);

    assertThat(node.getId(), equalTo("node-nodename"));
    assertThat(node.getName(), equalTo("nodename"));
    assertThat(node.getProperties().size(), equalTo(1));
    assertThat(node.getProperties().get("key"), equalTo("value"));
  }

  @Test
  void serialization() {
    Node node = new Node("testnode", "myId", null, DeviceType.HOST);
    Map<String, String> properties = new HashMap<>();
    properties.put("key", "value");
    node.setProperties(properties);
    JsonNode jsonNode = BatfishObjectMapper.mapper().valueToTree(node);

    assertThat(jsonNode.get("id").asText(), equalTo("myId"));
    assertThat(jsonNode.get("name").asText(), equalTo("testnode"));
    assertThat(jsonNode.get("type").asText(), equalTo("HOST"));
    assertThat(jsonNode.get("properties").get("key").asText(), equalTo("value"));
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(new Node("foo", null, null, null), new Node("foo", null, null, null))
        .addEqualityGroup(new Node("bar", null, null, null))
        .addEqualityGroup(new Node("bar", "id", null, null))
        .addEqualityGroup(new Node("bar", "id", DeviceModel.ARISTA_UNSPECIFIED, null))
        .addEqualityGroup(new Node("bar", "id", DeviceModel.ARISTA_UNSPECIFIED, DeviceType.ROUTER))
        .testEquals();
  }
}
