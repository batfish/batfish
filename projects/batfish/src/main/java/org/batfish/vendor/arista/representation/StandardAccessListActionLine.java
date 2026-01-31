package org.batfish.vendor.arista.representation;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;

@ParametersAreNonnullByDefault
public class StandardAccessListActionLine implements StandardAccessListLine {

  private final @Nonnull LineAction _action;
  private final @Nonnull String _name;
  private final @Nonnull IpWildcard _sourceIps;
  private final long _seq;

  public StandardAccessListActionLine(
      long seq, LineAction action, String name, IpWildcard sourceIps) {
    _seq = seq;
    _action = action;
    _name = name;
    _sourceIps = sourceIps;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public long getSeq() {
    return _seq;
  }

  public @Nonnull IpWildcard getSourceIps() {
    return _sourceIps;
  }

  @Override
  public @Nonnull Optional<ExtendedAccessListLine> toExtendedAccessListLine() {
    return Optional.ofNullable(
        ExtendedAccessListLine.builder()
            .setName(_name)
            .setAction(_action)
            .setSrcAddressSpecifier(new WildcardAddressSpecifier(_sourceIps))
            .setDstAddressSpecifier(new WildcardAddressSpecifier(IpWildcard.ANY))
            .setServiceSpecifier(StandardAccessListServiceSpecifier.INSTANCE)
            .build());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof StandardAccessListActionLine)) {
      return false;
    }
    StandardAccessListActionLine that = (StandardAccessListActionLine) o;
    return _seq == that._seq
        && _action == that._action
        && _name.equals(that._name)
        && _sourceIps.equals(that._sourceIps);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_action, _name, _sourceIps, _seq);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("seq", _seq)
        .add("action", _action)
        .add("name", _name)
        .add("sourceIps", _sourceIps)
        .toString();
  }
}
