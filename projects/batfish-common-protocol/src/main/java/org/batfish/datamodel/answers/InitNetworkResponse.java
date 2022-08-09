package org.batfish.datamodel.answers;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Response to v2 init_network request */
@ParametersAreNonnullByDefault
public final class InitNetworkResponse {

  public static @Nonnull InitNetworkResponse of(String outputNetworkName) {
    return new InitNetworkResponse(outputNetworkName);
  }

  @JsonProperty(PROP_OUTPUT_NETWORK_NAME)
  public @Nonnull String getOutputNetworkName() {
    return _outputNetworkName;
  }

  @JsonCreator
  private static @Nonnull InitNetworkResponse create(
      @JsonProperty(PROP_OUTPUT_NETWORK_NAME) @Nullable String outputNetworkName) {
    checkArgument(outputNetworkName != null, "Missing %s", PROP_OUTPUT_NETWORK_NAME);
    return of(outputNetworkName);
  }

  private InitNetworkResponse(String outputNetworkName) {
    _outputNetworkName = outputNetworkName;
  }

  private static final String PROP_OUTPUT_NETWORK_NAME = "outputNetworkName";
  private final @Nonnull String _outputNetworkName;
}
