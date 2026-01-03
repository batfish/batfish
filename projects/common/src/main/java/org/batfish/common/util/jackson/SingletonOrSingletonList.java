package org.batfish.common.util.jackson;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.databind.JsonNode;
import javax.annotation.Nullable;
import org.batfish.common.util.BatfishObjectMapper;

/**
 * A Jackson utility class to support deserializing data that represents a singleton as either an
 * atom or a list.
 */
public final class SingletonOrSingletonList {
  public static <T> T deserialize(@Nullable JsonNode node, Class<T> clazz) {
    if (node == null) {
      return null;
    } else if (node.isArray()) {
      checkArgument(node.size() == 1, "Expecting a singleton list, not with size %s", node.size());
      return BatfishObjectMapper.mapper().convertValue(node.get(0), clazz);
    }
    return BatfishObjectMapper.mapper().convertValue(node, clazz);
  }
}
