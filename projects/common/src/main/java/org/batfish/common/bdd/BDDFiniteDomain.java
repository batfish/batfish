package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;
import static org.batfish.common.util.CommonUtil.forEachWithIndex;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.math.IntMath;
import java.io.Serializable;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

/** Given a finite set of values, assigns each an integer id that can be tracked via BDD. */
@ParametersAreNonnullByDefault
public final class BDDFiniteDomain<V> implements Serializable {
  private final @Nonnull BiMap<V, BDD> _valueToBdd;
  private final @Nonnull BiMap<BDD, V> _bddToValue;
  private final @Nonnull BDD _isValidValue;
  private final @Nullable BDD _varBits;
  private final @Nonnull ImmutableBDDInteger _var;

  /** Allocate a variable sufficient for the given set of values. */
  public BDDFiniteDomain(BDDPacket pkt, String varName, Set<V> values) {
    this(pkt.allocateBDDInteger(varName, computeBitsRequired(values.size())), values);
  }

  /**
   * Allocate a variable, using the given {@link BDDFactory}, that is sufficient for the given set
   * of values.
   */
  public BDDFiniteDomain(BDDFactory factory, int index, Set<V> values) {
    this(
        ImmutableBDDInteger.makeFromIndex(factory, computeBitsRequired(values.size()), index),
        values);
  }

  /** Use the given variable to represent the given set of values. */
  public BDDFiniteDomain(ImmutableBDDInteger var, Set<V> values) {
    int size = values.size();
    BDD one = var.getFactory().one();
    _var = var;
    _varBits = var.getVars();
    if (size == 0) {
      _valueToBdd = ImmutableBiMap.of();
      _isValidValue = one;
    } else if (size == 1) {
      V value = values.iterator().next();
      _valueToBdd = ImmutableBiMap.of(value, one);
      _isValidValue = one;
    } else {
      int bitsRequired = computeBitsRequired(size);
      checkArgument(bitsRequired <= var.size());
      _valueToBdd = computeValueBdds(var, values);
      _isValidValue = var.leq(size - 1);
    }
    _bddToValue = _valueToBdd.inverse();
  }

  private static int computeBitsRequired(int size) {
    if (size < 2) {
      return 0;
    }
    return IntMath.log2(size, RoundingMode.CEILING);
  }

  private static <V> BiMap<V, BDD> computeValueBdds(BDDInteger var, Set<V> values) {
    ImmutableBiMap.Builder<V, BDD> builder = ImmutableBiMap.builder();
    forEachWithIndex(values, (idx, src) -> builder.put(src, var.value(idx)));
    return builder.build();
  }

  /**
   * Create multiple domains (never in use at the same time) backed by the same variable. For
   * example, we can use this to track domains that are per-node.
   */
  public static <K, V> Map<K, BDDFiniteDomain<V>> domainsWithSharedVariable(
      BDDPacket pkt, String varName, Map<K, Set<V>> values) {
    return domainsWithSharedVariable(pkt, varName, values, false);
  }

  /**
   * Create multiple domains (never in use at the same time) backed by the same variable. For
   * example, we can use this to track domains that are per-node.
   *
   * @param preferBeforePacketVars Whether the {@link BDD BDD} variables should be allocated before
   *     the variables used to encode packet headers. If true, and there are no remaining variables
   *     before the packet headers, will try to allocate after.
   */
  public static <K, V> Map<K, BDDFiniteDomain<V>> domainsWithSharedVariable(
      BDDPacket pkt, String varName, Map<K, Set<V>> values, boolean preferBeforePacketVars) {
    checkArgument(!values.isEmpty(), "empty values map");
    int maxSize = values.values().stream().mapToInt(Set::size).max().getAsInt();
    int bitsRequired = computeBitsRequired(maxSize);
    ImmutableBDDInteger var = pkt.allocateBDDInteger(varName, bitsRequired, preferBeforePacketVars);
    return toImmutableMap(
        values, Entry::getKey, entry -> new BDDFiniteDomain<>(var, entry.getValue()));
  }

  /**
   * Returns a new {@link BDD} equal to {@code bdd} with the variables in this {@link
   * BDDFiniteDomain domain} removed.
   *
   * <p>The input {@link BDD} is not modified. The output BDD is guaranteed to be different from the
   * input and can be safely modified or freed by the caller.
   */
  public BDD existsValue(BDD bdd) {
    return _varBits == null ? bdd.id() : bdd.exist(_varBits);
  }

  public BDD getConstraintForValue(V value) {
    return checkNotNull(_valueToBdd.get(value), "value not in domain");
  }

  public Map<V, BDD> getValueBdds() {
    return _valueToBdd;
  }

  public V getValueFromAssignment(BDD bdd) {
    checkArgument(bdd.isAssignment());

    // Exist turns the assignment into just the finite domain.
    V ret = _bddToValue.get(bdd.project(_varBits));
    checkArgument(ret != null, "No value for valid assignment");
    return ret;
  }

  public boolean isEmpty() {
    return _valueToBdd.isEmpty();
  }

  public BDD getIsValidConstraint() {
    return _isValidValue;
  }

  public @Nonnull ImmutableBDDInteger getVar() {
    return _var;
  }
}
