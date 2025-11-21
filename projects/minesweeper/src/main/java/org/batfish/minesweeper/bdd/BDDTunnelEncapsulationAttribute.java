package org.batfish.minesweeper.bdd;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.math.IntMath;
import java.math.RoundingMode;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;

/** Represents a symbolic {@link TunnelEncapsulationAttribute} including presence/absence. */
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
      /**
       * There is a value, but it's not one of the ones we track. Used to create a closed world and
       * to distinguish from {@link #ABSENT} even when there are no known literals.
       */
      OTHER,
    }

    static @Nonnull Value create(@Nonnull Type type, @Nullable TunnelEncapsulationAttribute value) {
      return new AutoValue_BDDTunnelEncapsulationAttribute_Value(type, value);
    }

    abstract @Nonnull Type type();

    abstract @Nullable TunnelEncapsulationAttribute value();

    /** There is no Tunnel Encapsulation Attribute on the route advertisement. */
    public static Value absent() {
      return ABSENT;
    }

    /**
     * There is a Tunnel Encapsulation Attribute on the route advertisement, with the given literal
     * value.
     */
    public static Value literal(TunnelEncapsulationAttribute value) {
      return Value.create(Type.LITERAL, value);
    }

    /**
     * There is a Tunnel Encapsulation Attribute on the route advertisement, but it's not one of the
     * known literals tracked in this symbolic attribute.
     */
    public static Value other() {
      return OTHER;
    }

    private static final Value ABSENT = Value.create(Type.ABSENT, null);
    private static final Value OTHER = Value.create(Type.OTHER, null);
  }

  /**
   * Allocates a new {@link BDDTunnelEncapsulationAttribute} at the given variable index in the
   * given factory.
   */
  public static BDDTunnelEncapsulationAttribute create(
      BDDFactory factory, int index, List<TunnelEncapsulationAttribute> knownValues) {
    // Note: the domain values are ordered so that ABSENT < any known value < OTHER so that
    // choosing the minimum value results in most understandable result.
    ImmutableList.Builder<Value> domainValues =
        ImmutableList.<Value>builderWithExpectedSize(knownValues.size() + 2).add(Value.ABSENT);
    for (TunnelEncapsulationAttribute value : knownValues) {
      domainValues.add(Value.literal(value));
    }
    domainValues.add(Value.OTHER);
    return new BDDTunnelEncapsulationAttribute(
        new BDDDomain<>(factory, domainValues.build(), index));
  }

  /** Creates a copy of the argument. Each can be mutated separately. */
  public static @Nonnull BDDTunnelEncapsulationAttribute copyOf(
      BDDTunnelEncapsulationAttribute other) {
    return new BDDTunnelEncapsulationAttribute(new BDDDomain<>(other._domain));
  }

  /**
   * Produces a BDD whose models represent all possible differences between this and the other
   * {@link BDDTunnelEncapsulationAttribute}.
   */
  public BDD allDifferences(BDDTunnelEncapsulationAttribute other) {
    return _domain.getInteger().allDifferences(other._domain.getInteger());
  }

  /**
   * Augments a given pairing to pair corresponding BDDs from the given
   * BDDTunnelEncapsulationAttribute with this one. The BDDs in the given
   * BDDTunnelEncapsulationAttribute should all be variables.
   *
   * @param other the BDDTunnelEncapsulationAttribute of variables
   * @param pairing the existing pairing
   */
  public void augmentPairing(BDDTunnelEncapsulationAttribute other, BDDPairing pairing) {
    _domain.augmentPairing(other._domain, pairing);
  }

  /**
   * Produces a BDD that represents the support (i.e., the set of BDD variables) of this attribute.
   */
  public BDD support() {
    return _domain.support();
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

  /**
   * Returns the number of bits needed to build a {@link BDDTunnelEncapsulationAttribute} with the
   * given set of known values.
   */
  public static int numBitsFor(List<TunnelEncapsulationAttribute> knownValues) {
    // ABSENT, known values, and OTHER value not in the known set.
    return IntMath.log2(knownValues.size() + 2, RoundingMode.CEILING);
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
    return _domain.hashCode();
  }

  // Note: the domain values are ordered so that ABSENT < any known value < OTHER so that
  // choosing the minimum value results in most understandable result.
  private final @Nonnull BDDDomain<Value> _domain;
}
