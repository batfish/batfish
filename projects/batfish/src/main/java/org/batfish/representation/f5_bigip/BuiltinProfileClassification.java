package org.batfish.representation.f5_bigip;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Enumeration of built-in ltm profile classification configurations */
@ParametersAreNonnullByDefault
public enum BuiltinProfileClassification implements BuiltinProfile {
  CLASSIFICATION("classification"),
  CLASSIFICATION_APM_SWG("classification_apm_swg"),
  CLASSIFICATION_PEM("classification_pem");

  private static final Map<String, BuiltinProfileClassification> FOR_NAME_MAP =
      Arrays.stream(values())
          .collect(
              ImmutableMap.toImmutableMap(
                  BuiltinProfileClassification::getName, Function.identity()));

  public static @Nullable BuiltinProfileClassification forName(String name) {
    return FOR_NAME_MAP.get(name);
  }

  private final @Nonnull String _name;

  private BuiltinProfileClassification(String name) {
    _name = name;
  }

  @Override
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public F5BigipStructureType getType() {
    return F5BigipStructureType.PROFILE_CLASSIFICATION;
  }
}
