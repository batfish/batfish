package org.batfish.common.util.jackson;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.Test;

class SingletonOrSingletonListTest {
  @Test
  void deserialize() {
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

  @Test
  void deserializeNotAnInt() {
    Throwable exception =
        assertThrows(
            Exception.class,
            () ->
                SingletonOrSingletonList.deserialize(
                    new JsonNodeFactory(false).textNode("abc"), Integer.class));
    assertThat(exception.getMessage(), containsString("abc"));
  }

  @Test
  void deserializeShortArray() {
    Throwable exception =
        assertThrows(
            Exception.class,
            () ->
                SingletonOrSingletonList.deserialize(
                    new JsonNodeFactory(false).arrayNode(), Integer.class));
    assertThat(exception.getMessage(), containsString("not with size 0"));
  }

  @Test
  void deserializeLongArray() {
    Throwable exception =
        assertThrows(
            Exception.class,
            () ->
                SingletonOrSingletonList.deserialize(
                    new JsonNodeFactory(false).arrayNode().add("8").add("7"), Integer.class));
    assertThat(exception.getMessage(), containsString("not with size 2"));
  }
}
