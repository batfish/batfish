package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.acl.GenericAclLineVisitor;

@ParametersAreNonnullByDefault
public final class AndAclLine extends AclLine {
  @Nonnull private final List<AclLine> _conjuncts;

  public AndAclLine(
      @Nullable String name, @Nullable TraceElement traceElement, AclLine... conjuncts) {
    this(name, traceElement, ImmutableList.copyOf(conjuncts));
  }

  public AndAclLine(
      @Nullable String name, @Nullable TraceElement traceElement, List<AclLine> conjuncts) {
    super(name, traceElement);
    _conjuncts = ImmutableList.copyOf(conjuncts);
  }

  @Nonnull
  public List<AclLine> getConjuncts() {
    return _conjuncts;
  }

  @Override
  public <R> R accept(GenericAclLineVisitor<R> visitor) {
    return null;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return builder().setName(_name).setTraceElement(_traceElement).setConjuncts(_conjuncts);
  }

  public static class Builder {
    @Nullable private String _name;
    @Nullable private TraceElement _traceElement;
    @Nullable private List<AclLine> _conjuncts;

    public AndAclLine build() {
      return new AndAclLine(_name, _traceElement, firstNonNull(_conjuncts, ImmutableList.of()));
    }

    public Builder setConjuncts(List<AclLine> conjuncts) {
      _conjuncts = conjuncts;
      return this;
    }

    public Builder setName(@Nullable String name) {
      _name = name;
      return this;
    }

    public Builder setTraceElement(@Nullable TraceElement traceElement) {
      _traceElement = traceElement;
      return this;
    }

    private Builder() {}
  }
}
