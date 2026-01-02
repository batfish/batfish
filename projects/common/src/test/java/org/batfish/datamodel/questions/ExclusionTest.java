package org.batfish.datamodel.questions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class ExclusionTest {

  @Test
  public void firstCoversSecondArrayCovers() {
    JsonNode node1 = BatfishObjectMapper.mapper().createArrayNode().add(new TextNode("value"));
    JsonNode node2 =
        BatfishObjectMapper.mapper()
            .createArrayNode()
            .add(new TextNode("value2"))
            .add(new TextNode("value"));

    boolean result = Exclusion.firstCoversSecond(node1, node2);

    assertThat(result, equalTo(true));
  }

  @Test
  public void firstCoversSecondObjectCovers() {
    JsonNode node1 =
        BatfishObjectMapper.mapper().createObjectNode().set("key1", new TextNode("value"));
    JsonNode node2 =
        BatfishObjectMapper.mapper().createObjectNode().set("key1", new TextNode("value"));
    // we couldn't just chain the two sets because set returns JsonNode, not ObjectNode
    ((ObjectNode) node2).set("key2", new TextNode("value"));

    boolean result = Exclusion.firstCoversSecond(node1, node2);

    assertThat(result, equalTo(true));
  }

  @Test
  public void firstCoversSecondObjectMismatchType() {
    JsonNode node1 =
        BatfishObjectMapper.mapper().createObjectNode().set("key1", new TextNode("value"));
    JsonNode node2 = BatfishObjectMapper.mapper().createArrayNode().add(new TextNode("value"));

    boolean result = Exclusion.firstCoversSecond(node1, node2);

    assertThat(result, equalTo(false));
  }

  @Test
  public void firstCoversSecondObjectMissingKey() {
    JsonNode node1 =
        BatfishObjectMapper.mapper().createObjectNode().set("key1", new TextNode("value"));
    JsonNode node2 =
        BatfishObjectMapper.mapper().createObjectNode().set("key2", new TextNode("value"));

    boolean result = Exclusion.firstCoversSecond(node1, node2);

    assertThat(result, equalTo(false));
  }

  @Test
  public void firstCoversSecondValueMatch() {
    JsonNode node1 = new TextNode("abc");
    JsonNode node2 = new TextNode("abc");

    boolean result = Exclusion.firstCoversSecond(node1, node2);

    assertThat(result, equalTo(true));
  }

  @Test
  public void firstCoversSecondValueMismatch() {
    JsonNode node1 = new TextNode("2");
    JsonNode node2 = new IntNode(2); // different type

    boolean result = Exclusion.firstCoversSecond(node1, node2);

    assertThat(result, equalTo(false));
  }
}
