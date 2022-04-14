package org.batfish.common.topology.bridge_domain.function;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.ALL_VLAN_IDS;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.L1ToL2;
import org.batfish.common.topology.bridge_domain.edge.L1ToL3;
import org.batfish.common.topology.bridge_domain.edge.NonVlanAwareBridgeDomainToL3;
import org.batfish.datamodel.IntegerSpace;

/**
 * A filter {@link StateFunction} that accepts a state with any of a space of allowed outer tags or
 * optionally no outer tag.
 */
public interface FilterByOuterTag
    extends L1ToL2.Function, L1ToL3.Function, NonVlanAwareBridgeDomainToL3.Function {

  static @Nonnull FilterByOuterTag of(IntegerSpace allowedOuterTags, boolean allowUntagged) {
    if (allowedOuterTags.contains(ALL_VLAN_IDS) && allowUntagged) {
      return identity();
    } else if (allowedOuterTags.isEmpty() && allowUntagged) {
      return FilterByOuterTagImpl.ALLOW_ONLY_UNTAGGED;
    } else {
      return new FilterByOuterTagImpl(allowedOuterTags, allowUntagged);
    }
  }

  final class FilterByOuterTagImpl implements FilterByOuterTag {

    @Override
    public <T, U> T accept(StateFunctionVisitor<T, U> visitor, U arg) {
      return visitor.visitFilterByOuterTag(this, arg);
    }

    /** Outer tags accepted by this filter, assuming outer tag is present. */
    public @Nonnull IntegerSpace getAllowedOuterTags() {
      return _allowedOuterTags;
    }

    /** Whether this filter accepts state with no outer tag. */
    public boolean getAllowUntagged() {
      return _allowUntagged;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      } else if (!(o instanceof FilterByOuterTagImpl)) {
        return false;
      }
      FilterByOuterTagImpl that = (FilterByOuterTagImpl) o;
      return _allowUntagged == that._allowUntagged
          && _allowedOuterTags.equals(that._allowedOuterTags);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_allowedOuterTags, _allowUntagged);
    }

    private static final FilterByOuterTag ALLOW_ONLY_UNTAGGED =
        new FilterByOuterTagImpl(IntegerSpace.EMPTY, true);

    private FilterByOuterTagImpl(IntegerSpace allowedOuterTags, boolean allowUntagged) {
      _allowedOuterTags = allowedOuterTags;
      _allowUntagged = allowUntagged;
    }

    private final @Nonnull IntegerSpace _allowedOuterTags;
    private final boolean _allowUntagged;
  }
}
