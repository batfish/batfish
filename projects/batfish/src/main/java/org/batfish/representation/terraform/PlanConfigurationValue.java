package org.batfish.representation.terraform;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
abstract class PlanConfigurationValue implements Serializable {

  @ParametersAreNonnullByDefault
  static class StringValue extends PlanConfigurationValue {
    @Nonnull private final String _value;

    StringValue(String value) {
      _value = value;
    }

    @Nonnull
    public String getValue() {
      return _value;
    }
  }

  @ParametersAreNonnullByDefault
  static class References extends PlanConfigurationValue {
    @Nonnull private final List<String> _values;

    References(List<String> values) {
      _values = values;
    }

    @Nonnull
    public List<String> getValues() {
      return _values;
    }
  }

  // Possible inputs:
  //     "value"
  // OR
  // "references": [
  //       "var.varname"
  //   ]
  // OR
  // "references": [
  //       "aws_security_group.multi_iface_sg_new"
  //   ]
  // OR
  //   "constant_value": "10.1.1.100"
  @SuppressWarnings("unchecked")
  static PlanConfigurationValue create(Object originalObject) {
    if (originalObject instanceof String) {
      return new StringValue(originalObject.toString());
    } else if (originalObject instanceof Map) {
      Map<String, Object> objectMap = (Map<String, Object>) originalObject;
      if (objectMap.containsKey("references") && objectMap.get("references") instanceof List) {
        return new References((List<String>) objectMap.get("references"));
      } else if (objectMap.containsKey("constant_value")) {
        return new StringValue(objectMap.get("constant_value").toString());
      }
    }
    throw new IllegalArgumentException(
        String.format(
            "Don't know how to extract plan configuration value from %s", originalObject));
  }
}
