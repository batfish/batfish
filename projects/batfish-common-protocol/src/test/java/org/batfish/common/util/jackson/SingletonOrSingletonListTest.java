package org.batfish.common.util.jackson;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SingletonOrSingletonListTest {
  @Test
  public void deserialize() {
    assertThat(SingletonOrSingletonList.deserialize(null, Integer.class), nullValue());
    assertThat(
        SingletonOrSingletonList.deserialize(
            new JsonNodeFactory(false).textNode("8"), Integer.class),
        equalTo(8));
    assertThat(
        SingletonOrSingletonList.deserialize(
            new JsonNodeFactory(false).arrayNode(1).add("8"), Integer.class),
        equalTo(8));
  }

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void deserializeNotAnInt() {
    _thrown.expectMessage("abc");
    SingletonOrSingletonList.deserialize(new JsonNodeFactory(false).textNode("abc"), Integer.class);
  }

  @Test
  public void deserializeShortArray() {
    _thrown.expectMessage("not with size 0");
    SingletonOrSingletonList.deserialize(new JsonNodeFactory(false).arrayNode(), Integer.class);
  }

  @Test
  public void deserializeLongArray() {
    _thrown.expectMessage("not with size 2");
    SingletonOrSingletonList.deserialize(
        new JsonNodeFactory(false).arrayNode().add("8").add("7"), Integer.class);
  }
}
