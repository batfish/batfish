package org.batfish.datamodel;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Objects;
import javax.annotation.Nonnull;

public class AclIpSpaceLine {

  @Override
  public String toString() {
    ToStringHelper helper = MoreObjects.toStringHelper(getClass());
    if (_action != LineAction.ACCEPT) {
      helper.add("action", _action);
    }
    helper.add("ipSpace", _ipSpace);
    if (_matchComplement) {
      helper.add("matchComplement", _matchComplement);
    }
    return helper.toString();
  }

  public static class Builder {

    private LineAction _action;

    private IpSpace _ipSpace;

    private boolean _matchComplement;

    private Builder() {
      _action = LineAction.ACCEPT;
      _matchComplement = false;
    }

    public AclIpSpaceLine build() {
      return new AclIpSpaceLine(_ipSpace, _action, _matchComplement);
    }

    public Builder setAction(@Nonnull LineAction action) {
      _action = action;
      return this;
    }

    public Builder setIpSpace(@Nonnull IpSpace ipSpace) {
      _ipSpace = ipSpace;
      return this;
    }

    public Builder setMatchComplement(boolean matchComplement) {
      _matchComplement = matchComplement;
      return this;
    }
  }

  public static final AclIpSpaceLine PERMIT_ALL =
      AclIpSpaceLine.builder().setIpSpace(UniverseIpSpace.INSTANCE).build();

  public static Builder builder() {
    return new Builder();
  }

  private final LineAction _action;

  private final IpSpace _ipSpace;

  private final boolean _matchComplement;

  private AclIpSpaceLine(
      @Nonnull IpSpace ipSpace, @Nonnull LineAction action, boolean matchComplement) {
    _ipSpace = ipSpace;
    _action = action;
    _matchComplement = matchComplement;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !(o instanceof AclIpSpaceLine)) {
      return false;
    }
    AclIpSpaceLine rhs = (AclIpSpaceLine) o;
    return Objects.equals(_action, rhs._action)
        && Objects.equals(_ipSpace, rhs._ipSpace)
        && _matchComplement == rhs._matchComplement;
  }

  public LineAction getAction() {
    return _action;
  }

  public IpSpace getIpSpace() {
    return _ipSpace;
  }

  /** Match the complement of the {@code IpSpace} of this line. */
  public boolean getMatchComplement() {
    return _matchComplement;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_action.ordinal(), _ipSpace, _matchComplement);
  }
}
