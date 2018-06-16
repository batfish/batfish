package org.batfish.datamodel.ospf;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;

/** OSPF Area settings specific to a not-so-stubby stub area */
public class NssaSettings implements Serializable {

  public static class Builder {

    private OspfDefaultOriginateType _defaultOriginateType;

    private boolean _suppressType3;

    private Builder() {}

    public NssaSettings build() {
      return new NssaSettings(this);
    }

    public Builder setDefaultOriginateType(OspfDefaultOriginateType defaultOriginateType) {
      _defaultOriginateType = defaultOriginateType;
      return this;
    }

    public Builder setSuppressType3(boolean suppressType3) {
      _suppressType3 = suppressType3;
      return this;
    }
  }

  private static final String PROP_DEFAULT_ORIGINATE_TYPE = "defaultOriginateType";

  private static final String PROP_SUPPRESS_TYPE3 = "suppressType3";

  private static final long serialVersionUID = 1L;

  public static Builder builder() {
    return new Builder();
  }

  private final OspfDefaultOriginateType _defaultOriginateType;

  private final boolean _suppressType3;

  public NssaSettings(Builder builder) {
    _defaultOriginateType = builder._defaultOriginateType;
    _suppressType3 = builder._suppressType3;
  }

  @JsonCreator
  private NssaSettings(
      @JsonProperty(PROP_DEFAULT_ORIGINATE_TYPE) OspfDefaultOriginateType defaultOriginateType,
      @JsonProperty(PROP_SUPPRESS_TYPE3) boolean suppressType3) {
    _defaultOriginateType = firstNonNull(defaultOriginateType, OspfDefaultOriginateType.NONE);
    _suppressType3 = suppressType3;
  }

  @JsonProperty(PROP_DEFAULT_ORIGINATE_TYPE)
  public @Nonnull OspfDefaultOriginateType getDefaultOriginateType() {
    return _defaultOriginateType;
  }

  @JsonProperty(PROP_SUPPRESS_TYPE3)
  public boolean getSuppressType3() {
    return _suppressType3;
  }
}
