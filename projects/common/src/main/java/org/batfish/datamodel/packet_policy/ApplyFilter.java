package org.batfish.datamodel.packet_policy;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.acl.DeniedByAcl;
import org.batfish.datamodel.flow.FilterStep;

/**
 * Apply {@link IpAccessList} with the specified {@link ApplyFilter#getFilter() filter name}. If the
 * ACL denies the flow, return {@link Drop}, otherwise fall through.
 *
 * <p>Differs from an {@link If} containing a {@link DeniedByAcl} in that {@link ApplyFilter} will
 * generate a {@link FilterStep} in the traceroute trace.
 */
public class ApplyFilter implements Statement {

  private static final String PROP_FILTER = "filter";

  private final @Nonnull String _filter;

  public ApplyFilter(String filter) {
    _filter = filter;
  }

  @JsonProperty(PROP_FILTER)
  public @Nonnull String getFilter() {
    return _filter;
  }

  @Override
  public <T> T accept(StatementVisitor<T> visitor) {
    return visitor.visitApplyFilter(this);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ApplyFilter)) {
      return false;
    }
    ApplyFilter that = (ApplyFilter) o;
    return _filter.equals(that._filter);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_filter);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(ApplyFilter.class).add(PROP_FILTER, _filter).toString();
  }

  @JsonCreator
  private static ApplyFilter create(@JsonProperty(PROP_FILTER) @Nullable String filter) {
    checkArgument(filter != null, "Missing %s", PROP_FILTER);
    return new ApplyFilter(filter);
  }
}
