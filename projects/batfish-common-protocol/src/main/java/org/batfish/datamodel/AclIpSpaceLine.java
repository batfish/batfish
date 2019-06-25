package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.Serializable;
import java.util.Comparator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AclIpSpaceLine implements Comparable<AclIpSpaceLine>, Serializable {

  public static class Builder {

    private LineAction _action;

    private IpSpace _ipSpace;

    private String _srcText;

    private Builder() {
      _action = LineAction.PERMIT;
    }

    public AclIpSpaceLine build() {
      return new AclIpSpaceLine(_ipSpace, _action, _srcText);
    }

    public Builder setAction(@Nonnull LineAction action) {
      _action = action;
      return this;
    }

    public Builder setIpSpace(@Nonnull IpSpace ipSpace) {
      _ipSpace = ipSpace;
      return this;
    }

    public Builder setSrcText(@Nullable String srcText) {
      _srcText = srcText;
      return this;
    }
  }

  public static final AclIpSpaceLine DENY_ALL = AclIpSpaceLine.reject(UniverseIpSpace.INSTANCE);

  public static final AclIpSpaceLine PERMIT_ALL = AclIpSpaceLine.permit(UniverseIpSpace.INSTANCE);
  private static final String PROP_ACTION = "action";
  private static final String PROP_IP_SPACE = "ipSpace";
  private static final String PROP_SRC_TEXT = "srcText";

  public static Builder builder() {
    return new Builder();
  }

  public static AclIpSpaceLine permit(IpSpace ipSpace) {
    return builder().setIpSpace(ipSpace).build();
  }

  public static AclIpSpaceLine reject(IpSpace ipSpace) {
    return builder().setIpSpace(ipSpace).setAction(LineAction.DENY).build();
  }

  @Nonnull private final LineAction _action;

  @Nonnull private final IpSpace _ipSpace;

  private final String _srcText;

  @JsonCreator
  private AclIpSpaceLine(
      @JsonProperty(PROP_IP_SPACE) @Nonnull IpSpace ipSpace,
      @JsonProperty(PROP_ACTION) @Nonnull LineAction action,
      @JsonProperty(PROP_SRC_TEXT) String srcText) {
    _ipSpace = ipSpace;
    _action = action;
    _srcText = srcText;
  }

  @Override
  public int compareTo(AclIpSpaceLine o) {
    return Comparator.comparing(AclIpSpaceLine::getAction)
        .thenComparing(AclIpSpaceLine::getIpSpace)
        .compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof AclIpSpaceLine)) {
      return false;
    }
    AclIpSpaceLine rhs = (AclIpSpaceLine) o;
    return _action == rhs._action && _ipSpace.equals(rhs._ipSpace);
  }

  @Nonnull
  @JsonProperty(PROP_ACTION)
  public LineAction getAction() {
    return _action;
  }

  @Nonnull
  @JsonProperty(PROP_IP_SPACE)
  public IpSpace getIpSpace() {
    return _ipSpace;
  }

  @JsonProperty(PROP_SRC_TEXT)
  public String getSrcText() {
    return _srcText;
  }

  @Override
  public int hashCode() {
    return 31 * _action.ordinal() + _ipSpace.hashCode();
  }

  public Builder toBuilder() {
    return builder().setAction(_action).setIpSpace(_ipSpace);
  }

  @Override
  public String toString() {
    ToStringHelper helper = MoreObjects.toStringHelper(getClass());
    if (_action != LineAction.PERMIT) {
      helper.add(PROP_ACTION, _action);
    }
    helper.add(PROP_IP_SPACE, _ipSpace);
    return helper.toString();
  }
}
