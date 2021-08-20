package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.batfish.common.Warnings;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class Statement implements Serializable {
  private static final String PROP_COMMENT = "comment";

  private String _comment;

  protected transient List<Statement> _simplified;

  public abstract <T, U> T accept(StatementVisitor<T, U> visitor, U arg);

  /**
   * Get all the routing-policies referenced by this statement.
   *
   * @return A {@link SortedSet} containing the names of each {@link RoutingPolicy} directly or
   *     indirectly referenced by this statement
   */
  public Set<String> collectSources(
      Set<String> parentSources, Map<String, RoutingPolicy> routingPolicies, Warnings w) {
    return Collections.emptySet();
  }

  @Override
  public abstract boolean equals(Object obj);

  public abstract Result execute(Environment environment);

  @JsonProperty(PROP_COMMENT)
  public final String getComment() {
    return _comment;
  }

  @Override
  public abstract int hashCode();

  @JsonProperty(PROP_COMMENT)
  public final void setComment(String comment) {
    _comment = comment;
  }

  public List<Statement> simplify() {
    if (_simplified == null) {
      _simplified = ImmutableList.of(this);
    }
    return _simplified;
  }

  @Override
  public String toString() {
    if (_comment != null) {
      return getClass().getSimpleName() + "<" + _comment + ">";
    } else {
      return super.toString();
    }
  }
}
