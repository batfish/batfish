package org.batfish.representation.terraform;

import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents information about a Terraform provider */
@ParametersAreNonnullByDefault
public class PlanProvider implements Serializable {

  @Nonnull private final Map<String, PlanConfigurationValue> _values;

  //  @Nonnull private final Map<String, String> _resolvedValues;

  PlanProvider(Map<String, Object> values) {
    //    _resolvedValues = new HashMap<>();
    ImmutableMap.Builder<String, PlanConfigurationValue> builder = ImmutableMap.builder();
    for (String key : values.keySet()) {
      if (key.equals("expressions")) {
        @SuppressWarnings("unchecked")
        Map<String, Object> expressions = (Map<String, Object>) values.get("expressions");
        for (String subKey : expressions.keySet()) {
          builder.put(subKey, PlanConfigurationValue.create(expressions.get(subKey)));
        }
      } else {
        PlanConfigurationValue originalValue = PlanConfigurationValue.create(values.get(key));
        builder.put(key, originalValue);
        //        if (originalValue.getType() == Type.CONSTANT) {
        //          _resolvedValues.put(key, originalValue.getValue());
        //        }
      }
    }
    _values = builder.build();
  }

  @Nonnull
  public Map<String, PlanConfigurationValue> getValues() {
    return _values;
  }

  //  public void resolveUsingVariables(Map<String, String> variables, Warnings warnings) {
  //    _originalValues.forEach(
  //        (k, v) -> {
  //          if (v.getType() == Type.VARIABLE && variables.containsKey(v.getValue())) {
  //            resolveValue(k, variables.get(v.getValue()), warnings);
  //          }
  //        });
  //  }
  //
  //  public void resolveValue(String key, String value, Warnings warnings) {
  //    if (_resolvedValues.containsKey(key)) {
  //      if (!_resolvedValues.get(key).equals(value)) {
  //        warnings.redFlag(
  //            String.format(
  //                "Inconsistent resolution for provider variable: %s vs %s",
  //                _resolvedValues.get(key), value));
  //      }
  //    } else {
  //      _resolvedValues.put(key, value);
  //    }
  //  }
}
