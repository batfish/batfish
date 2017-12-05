package org.batfish.datamodel.pojo;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.DeviceType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NodeTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void nodeConstructorFail() throws IOException {
    String nodeStr = "{\"nonamefield\" : \"nodeName\"}";
    BatfishObjectMapper mapper = new BatfishObjectMapper();
    _thrown.expect(com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException.class);
    Node node = mapper.readValue(nodeStr, Node.class);
  }

  @Test
  public void nodeConstructorBasic() throws IOException {
    String nodeStr = "{\"name\" : \"nodeName\"}";
    BatfishObjectMapper mapper = new BatfishObjectMapper();
    Node node = mapper.readValue(nodeStr, Node.class);

    assertThat(node.getId(), equalTo(Node.getId("nodeName")));
    assertThat(node.getName(), equalTo("nodeName"));
  }

  @Test
  public void nodeConstructorProperties() throws IOException {
    String nodeStr = "{\"name\" : \"nodeName\", \"properties\" : { \"key\": \"value\"}}";
    BatfishObjectMapper mapper = new BatfishObjectMapper();
    Node node = mapper.readValue(nodeStr, Node.class);

    assertThat(node.getName(), equalTo("nodeName"));
    assertThat(node.getProperties().size(), equalTo(1));
    assertThat(node.getProperties().get("key"), equalTo("value"));
  }

  @Test
  public void nodeSerialization() {
    Node node = new Node("testnode", DeviceType.HOST);
    Map<String, String> properties = new HashMap<>();
    properties.put("key", "value");
    node.setProperties(properties);
    BatfishObjectMapper mapper = new BatfishObjectMapper();
    JsonNode jsonNode = mapper.valueToTree(node);

    assertThat(jsonNode.get("name").asText(), equalTo("testnode"));
    assertThat(jsonNode.get("type").asText(), equalTo("HOST"));
    assertThat(jsonNode.has("properties"), equalTo(true));
    assertThat(jsonNode.get("properties").get("key").asText(), equalTo("value"));
  }
}
