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
public final class LiteralAsList extends AsPathListExpr {
  private static final String PROP_LIST = "list";

  private @Nonnull List<AsExpr> _list;

  @JsonCreator
  private static LiteralAsList jsonCreator(@JsonProperty(PROP_LIST) @Nullable List<AsExpr> list) {
    return new LiteralAsList(firstNonNull(list, ImmutableList.of()));
  }

  public LiteralAsList(List<AsExpr> list) {
    _list = ImmutableList.copyOf(list);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    LiteralAsList other = (LiteralAsList) obj;
    return _list.equals(other._list);
  }

  @Override
  public List<Long> evaluate(Environment environment) {
    return _list.stream()
        .map(expr -> expr.evaluate(environment))
        .collect(ImmutableList.toImmutableList());
  }

  @JsonProperty(PROP_LIST)
  public @Nonnull List<AsExpr> getList() {
    return _list;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _list.hashCode();
    return result;
  }

  public void setList(List<AsExpr> list) {
    _list = ImmutableList.copyOf(list);
  }
}
