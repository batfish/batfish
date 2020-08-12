package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;
import static org.batfish.common.util.CommonUtil.forEachWithIndex;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.math.IntMath;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;

/** Given a finite set of values, assigns each an integer id that can be tracked via BDD. */
@ParametersAreNonnullByDefault
public final class BDDFiniteDomain<V> {
  private @Nonnull final BiMap<V, BDD> _valueToBdd;
  private @Nonnull final BiMap<BDD, V> _bddToValue;
  private @Nonnull final BDD _isValidValue;
  private @Nullable final BDD _varBits;
  private @Nonnull final BDDInteger _var;

  /** Allocate a variable sufficient for the given set of values. */
  public BDDFiniteDomain(BDDPacket pkt, String varName, Set<V> values) {
    this(pkt.allocateBDDInteger(varName, computeBitsRequired(values.size()), false), values);
  }

  /** Use the given variable. */
  BDDFiniteDomain(BDDInteger var, Set<V> values) {
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
    checkArgument(!values.isEmpty(), "empty values map");
    int maxSize = values.values().stream().mapToInt(Set::size).max().getAsInt();
    int bitsRequired = computeBitsRequired(maxSize);
    BDDInteger var = pkt.allocateBDDInteger(varName, bitsRequired, false);
    return toImmutableMap(
        values, Entry::getKey, entry -> new BDDFiniteDomain<>(var, entry.getValue()));
  }

  public BDD existsValue(BDD bdd) {
    return _varBits == null ? bdd : bdd.exist(_varBits);
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
    V ret = _bddToValue.get(bdd.project(_var.getVars()));
    checkArgument(ret != null, "No value for valid assignment");
    return ret;
  }

  public boolean isEmpty() {
    return _valueToBdd.isEmpty();
  }

  public BDD getIsValidConstraint() {
    return _isValidValue;
  }

  @Nonnull
  public BDDInteger getVar() {
    return _var;
  }
}
