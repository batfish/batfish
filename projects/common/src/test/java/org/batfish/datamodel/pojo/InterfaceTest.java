package org.batfish.datamodel.pojo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.InterfaceType;
import org.junit.jupiter.api.Test;

class InterfaceTest {

  @Test
  void constructorFail() {
    String ifaceStr = "{\"nofield\" : \"test\"}";
    assertThrows(
        com.fasterxml.jackson.databind.exc.ValueInstantiationException.class,
        () -> BatfishObjectMapper.mapper().readValue(ifaceStr, Interface.class));
  }

  @Test
  void constructorBasic() throws IOException {
    String ifaceStr = "{\"nodeId\" : \"node\", \"name\" : \"iname\"}";
    Interface iface = BatfishObjectMapper.mapper().readValue(ifaceStr, Interface.class);

    assertThat(iface.getId(), equalTo(Interface.getId("node", "iname")));
    assertThat(iface.getNodeId(), equalTo("node"));
    assertThat(iface.getName(), equalTo("iname"));
  }

  @Test
  void constructorProperties() throws IOException {
    String ifaceStr =
        "{\"nodeId\" : \"node\", \"name\" : \"name\", \"properties\" : { \"key\": \"value\"}}";
    Interface iface = BatfishObjectMapper.mapper().readValue(ifaceStr, Interface.class);

    assertThat(iface.getId(), equalTo(Interface.getId("node", "name")));
    assertThat(iface.getNodeId(), equalTo("node"));
    assertThat(iface.getName(), equalTo("name"));
    assertThat(iface.getProperties().size(), equalTo(1));
    assertThat(iface.getProperties().get("key"), equalTo("value"));
  }

  @Test
  void serialization() {
    Interface iface = new Interface("node", "name", InterfaceType.PHYSICAL);
    Map<String, String> properties = new HashMap<>();
    properties.put("key", "value");
    iface.setProperties(properties);
    JsonNode jsonNode = BatfishObjectMapper.mapper().valueToTree(iface);

    assertThat(jsonNode.get("id").asText(), equalTo(Interface.getId("node", "name")));
    assertThat(jsonNode.get("nodeId").asText(), equalTo("node"));
    assertThat(jsonNode.get("name").asText(), equalTo("name"));
    assertThat(jsonNode.get("type").asText(), equalTo("PHYSICAL"));
    assertThat(jsonNode.get("properties").get("key").asText(), equalTo("value"));
  }
}
