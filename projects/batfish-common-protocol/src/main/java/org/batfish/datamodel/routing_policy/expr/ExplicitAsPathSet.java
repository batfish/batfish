package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;

@ParametersAreNonnullByDefault
public final class ExplicitAsPathSet extends AsPathSetExpr {
  private static final String PROP_ELEMS = "elems";

  /** */
  private static final long serialVersionUID = 1L;

  @Nonnull private List<AsPathSetElem> _elems;

  @JsonCreator
  private static ExplicitAsPathSet jsonCreator(
      @Nullable @JsonProperty(PROP_ELEMS) List<AsPathSetElem> elems) {
    return new ExplicitAsPathSet(firstNonNull(elems, ImmutableList.of()));
  }

  public ExplicitAsPathSet(List<AsPathSetElem> elems) {
    _elems = elems;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof ExplicitAsPathSet)) {
      return false;
    }
    ExplicitAsPathSet other = (ExplicitAsPathSet) obj;
    return _elems.equals(other._elems);
  }

  @JsonProperty(PROP_ELEMS)
  @Nonnull
  public List<AsPathSetElem> getElems() {
    return _elems;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _elems.hashCode();
    return result;
  }

  @Override
  public boolean matches(Environment environment) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  public void setElems(List<AsPathSetElem> elems) {
    _elems = elems;
  }
}
