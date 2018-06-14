package org.batfish.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Map;

public class PrettyPrinter {

  public static String print(String prefixStr, JsonDiff jsonDiff) {
    final StringBuilder sb = new StringBuilder();
    for (String key : jsonDiff.getData().keySet()) {
      Object value = jsonDiff.getData().get(key);

      if (key.startsWith(JsonDiff.ADDED_ITEM_CODE)) {
        sb.append(prefixStr + "+[" + JsonDiff.getStringWithoutCode(key) + "]\n");
        sb.append(print(prefixStr + "  ", value));
      } else if (key.startsWith(JsonDiff.REMOVED_ITEM_CODE)) {
        sb.append(prefixStr + "-[" + JsonDiff.getStringWithoutCode(key) + "]\n");
        sb.append(print(prefixStr + "  ", value));
      } else if (key.startsWith(JsonDiff.COMMON_ITEM_CODE)) {
        sb.append(prefixStr + "~[" + JsonDiff.getStringWithoutCode(key) + "]\n");
        sb.append(print(prefixStr + "  ", value));
      } else if (key.startsWith(JsonDiff.CHANGED_ITEM_CODE)) {
        sb.append(prefixStr + "~[" + JsonDiff.getStringWithoutCode(key) + "]\n");
        Map<?, ?> map = (Map<?, ?>) value;

        Object baseValue = map.get(JsonDiff.CHANGED_ITEM_BASE);
        Object deltaValue = map.get(JsonDiff.CHANGED_ITEM_DELTA);

        sb.append(prefixStr + "BASE\n");
        sb.append(print(prefixStr + " ", baseValue));

        sb.append(prefixStr + "DELTA\n");
        sb.append(print(prefixStr + " ", deltaValue));
      } else {
        sb.append(prefixStr + key + "\n");
        sb.append(print(prefixStr + "  ", value));
      }
    }
    return sb.toString();
  }

  private static String print(String prefixStr, List<?> list) {
    final StringBuilder sb = new StringBuilder();
    for (Object obj : list) {
      sb.append(print(prefixStr, obj));
    }
    return sb.toString();
  }

  private static String print(String prefixStr, Object value) {
    final StringBuilder sb = new StringBuilder();
    if (value instanceof JsonDiff) {
      sb.append(((JsonDiff) value).prettyPrint(prefixStr + "  "));
    } else if (value instanceof Map<?, ?>) {
      sb.append(print(prefixStr + "  ", (Map<?, ?>) value));
    } else if (value instanceof List<?>) {
      sb.append(print(prefixStr + "  ", (List<?>) value));
    } else if (value instanceof String) {
      sb.append(prefixStr + "  " + value + "\n");
    } else {
      try {
        sb.append(BatfishObjectMapper.writePrettyString(value));
      } catch (JsonProcessingException e) {
        sb.append("Exception while pretty printing " + value + "\n" + e.getMessage());
      }
    }
    return sb.toString();
  }

  private static String print(String prefixStr, Map<?, ?> map) {
    final StringBuilder sb = new StringBuilder();
    for (Object keyObject : map.keySet()) {
      String key = (String) keyObject;
      Object value = map.get(key);

      sb.append(prefixStr + key + "\n");
      sb.append(print(prefixStr + "  ", value));
    }
    return sb.toString();
  }
}
