package org.batfish.minesweeper.bdd;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.minesweeper.bdd.BDDTunnelEncapsulationAttribute.Value.Type;

/**
 * Represents a symbolic {@link org.batfish.datamodel.bgp.TunnelEncapsulationAttribute} including
 * presence/absence.
 */
@ParametersAreNonnullByDefault
public final class BDDTunnelEncapsulationAttribute {
  /** Represents the possible values for the BGP Tunnel Encapsulation Attribute. */
  @AutoValue
  public abstract static class Value {
    enum Type {
      /** There is no Tunnel Encapsulation Attribute on the advertisement. */
      ABSENT,
      /** There is a specific known value. */
      LITERAL,
      /** There is a value, but it's not one of the ones we track. */
      UNKNOWN,
    }

    static @Nonnull Value create(@Nonnull Type type, @Nullable TunnelEncapsulationAttribute value) {
      return new AutoValue_BDDTunnelEncapsulationAttribute_Value(type, value);
    }

    abstract @Nonnull Type type();

    abstract @Nullable TunnelEncapsulationAttribute value();

    public static Value absent() {
      return ABSENT;
    }

    public static Value unknown() {
      return UNKNOWN;
    }

    public static Value literal(TunnelEncapsulationAttribute value) {
      return Value.create(Type.LITERAL, value);
    }

    private static final Value ABSENT = Value.create(Type.ABSENT, null);
    private static final Value UNKNOWN = Value.create(Type.UNKNOWN, null);
  }

  /**
   * Allocates a new {@link BDDTunnelEncapsulationAttribute} at the given variable index in the
   * given factory.
   */
  public static BDDTunnelEncapsulationAttribute create(
      BDDFactory factory, int index, List<TunnelEncapsulationAttribute> knownValues) {
    ImmutableList.Builder<Value> domainValues =
        ImmutableList.<Value>builderWithExpectedSize(knownValues.size() + 2).add(Value.ABSENT);
    for (TunnelEncapsulationAttribute value : knownValues) {
      domainValues.add(Value.literal(value));
    }
    domainValues.add(Value.UNKNOWN);
    return new BDDTunnelEncapsulationAttribute(
        new BDDDomain<>(factory, domainValues.build(), index));
  }

  /** Creates a copy of the argument. Each can be mutated separately. */
  public static @Nonnull BDDTunnelEncapsulationAttribute copyOf(
      BDDTunnelEncapsulationAttribute other) {
    return new BDDTunnelEncapsulationAttribute(new BDDDomain<>(other._domain));
  }

  /** Returns a new {@link BDDTunnelEncapsulationAttribute} restricted to the given predicate. */
  public @Nonnull BDDTunnelEncapsulationAttribute and(BDD pred) {
    return new BDDTunnelEncapsulationAttribute(new BDDDomain<>(pred, _domain));
  }

  public @Nonnull Value satAssignmentToValue(BDD model) {
    return _domain.satAssignmentToValue(model);
  }

  private BDDTunnelEncapsulationAttribute(BDDDomain<Value> domain) {
    _domain = domain;
  }

  public BDD getIsValidConstraint() {
    return _domain.getIsValidConstraint();
  }

  public int getNumBits() {
    return _domain.getInteger().size();
  }

  public void setValue(Value tunnelEncapsulationAttribute) {
    _domain.setValue(tunnelEncapsulationAttribute);
  }

  public BDD value(Value attr) {
    return _domain.value(attr);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof BDDTunnelEncapsulationAttribute)) {
      return false;
    }
    BDDTunnelEncapsulationAttribute that = (BDDTunnelEncapsulationAttribute) o;
    return _domain.equals(that._domain);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_domain);
  }

  private final @Nonnull BDDDomain<Value> _domain;
}
