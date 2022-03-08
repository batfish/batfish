package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.HasWritableAsPath;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/** Replace every AS in a sequence of ASes with some AS */
@ParametersAreNonnullByDefault
public final class ReplaceAsesInAsSequence extends Statement {

  /** What to replace each AS in a matched sequence with. */
  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
  public interface AsReplacementExpr extends Serializable {
    <T> T accept(AsReplacementExprVisitor<T> visitor);
  }

  public interface AsReplacementExprVisitor<T> {
    default T visit(AsReplacementExpr asReplacementExpr) {
      return asReplacementExpr.accept(this);
    }

    T visitLocalAsOrConfedIfNeighborNotInConfed(
        LocalAsOrConfedIfNeighborNotInConfed localAsOrConfederationIfNeighborInConfederation);
  }

  /**
   * An expression for a sequence to match. Every AS in the matched sequence will be replaced with
   * the replacement AS.
   */
  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
  public interface AsSequenceExpr extends Serializable {
    <T> T accept(AsSequenceExprVisitor<T> visitor);
  }

  public interface AsSequenceExprVisitor<T> {
    default T visit(AsSequenceExpr asSequenceExpr) {
      return asSequenceExpr.accept(this);
    }

    T visitAnyAs(AnyAs anyAs);

    T visitAsPathSequence(AsSequence asSequence);
  }

  /** Match and replace every AS in the input path individually. */
  public static final class AnyAs implements AsSequenceExpr {

    @Override
    public <T> T accept(AsSequenceExprVisitor<T> visitor) {
      return visitor.visitAnyAs(this);
    }

    @Override
    public boolean equals(Object obj) {
      return this == obj || obj instanceof AnyAs;
    }

    @Override
    public int hashCode() {
      return 0x40BD5B64; // randomly generated
    }

    @JsonCreator
    @JsonValue
    private static @Nonnull AnyAs instance() {
      return INSTANCE;
    }

    private static final AnyAs INSTANCE = new AnyAs();
  }

  public static @Nonnull AsSequenceExpr anyAs() {
    return AnyAs.instance();
  }

  /**
   * Match a sequence of singleton AS numbers. Every AS in the matched sequence will be replaced
   * with the replacement AS.
   */
  public static final class AsSequence implements AsSequenceExpr {

    public AsSequence(List<Long> sequence) {
      _sequence = sequence;
    }

    @JsonCreator
    private static @Nonnull AsSequence create(
        @JsonProperty(PROP_SEQUENCE) @Nullable List<Long> sequence) {
      return new AsSequence(ImmutableList.copyOf(firstNonNull(sequence, ImmutableList.of())));
    }

    @Override
    public <T> T accept(AsSequenceExprVisitor<T> visitor) {
      return visitor.visitAsPathSequence(this);
    }

    @JsonProperty(PROP_SEQUENCE)
    public @Nonnull List<Long> getSequence() {
      return _sequence;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (!(o instanceof AsSequence)) {
        return false;
      }
      AsSequence that = (AsSequence) o;
      return _sequence.equals(that._sequence);
    }

    @Override
    public int hashCode() {
      return _sequence.hashCode();
    }

    private static final String PROP_SEQUENCE = "sequence";

    private final @Nonnull List<Long> _sequence;
  }

  public static @Nonnull AsSequenceExpr sequenceOf(List<Long> sequence) {
    checkArgument(!sequence.isEmpty(), "Sequence must be non-empty");
    return new AsSequence(sequence);
  }

  public static @Nonnull AsReplacementExpr localAsOrConfedIfNeighborNotInConfed() {
    return LOCAL_AS_OR_CONFED_IF_NEIGHBOR_NOT_IN_CONFED;
  }

  private static final @Nonnull AsReplacementExpr LOCAL_AS_OR_CONFED_IF_NEIGHBOR_NOT_IN_CONFED =
      new LocalAsOrConfedIfNeighborNotInConfed();

  /**
   * Replace each AS in the matched sequence with the local-AS if not in a confederation. If in a
   * confederation, use local-AS if neighbor is in the confederation, else use confederation AS
   * number.
   */
  public static final class LocalAsOrConfedIfNeighborNotInConfed implements AsReplacementExpr {

    @Override
    public <T> T accept(AsReplacementExprVisitor<T> visitor) {
      return visitor.visitLocalAsOrConfedIfNeighborNotInConfed(this);
    }

    @Override
    public boolean equals(Object obj) {
      return this == obj || obj instanceof LocalAsOrConfedIfNeighborNotInConfed;
    }

    @Override
    public int hashCode() {
      return 0x340FFF58; // randomly generated
    }

    @JsonCreator
    @JsonValue
    private static @Nonnull LocalAsOrConfedIfNeighborNotInConfed instance() {
      return INSTANCE;
    }

    private static final @Nonnull LocalAsOrConfedIfNeighborNotInConfed INSTANCE =
        new LocalAsOrConfedIfNeighborNotInConfed();
  }

  public ReplaceAsesInAsSequence(
      AsSequenceExpr asSequenceExpr, AsReplacementExpr asReplacementExpr) {
    _asReplacementExpr = asReplacementExpr;
    _asSequenceExpr = asSequenceExpr;
  }

  @JsonCreator
  private static @Nonnull ReplaceAsesInAsSequence create(
      @JsonProperty(PROP_AS_SEQUENCE_EXPR) @Nullable AsSequenceExpr asSequenceExpr,
      @JsonProperty(PROP_AS_REPLACEMENT_EXPR) @Nullable AsReplacementExpr asReplacementExpr) {
    checkArgument(asSequenceExpr != null, "Missing %s", PROP_AS_SEQUENCE_EXPR);
    checkArgument(asReplacementExpr != null, "Missing %s", PROP_AS_REPLACEMENT_EXPR);
    return new ReplaceAsesInAsSequence(asSequenceExpr, asReplacementExpr);
  }

  @JsonProperty(PROP_AS_SEQUENCE_EXPR)
  public @Nonnull AsSequenceExpr getAsSequenceExpr() {
    return _asSequenceExpr;
  }

  @JsonProperty(PROP_AS_REPLACEMENT_EXPR)
  public @Nonnull AsReplacementExpr getAsReplacementExpr() {
    return _asReplacementExpr;
  }

  private static final String PROP_AS_SEQUENCE_EXPR = "asSequenceExpr";
  private static final String PROP_AS_REPLACEMENT_EXPR = "asReplacementExpr";

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitReplaceAsesInAsSequence(this);
  }

  @Override
  public Result execute(Environment environment) {
    Optional<Long> maybeReplacementAs = replacement(environment);
    if (!maybeReplacementAs.isPresent()) {
      return new Result();
    }
    long replacementAs = maybeReplacementAs.get();
    if ((environment.getOutputRoute() instanceof HasWritableAsPath<?, ?>)) {
      HasWritableAsPath<?, ?> outputRoute = (HasWritableAsPath<?, ?>) environment.getOutputRoute();
      outputRoute.setAsPath(replace(outputRoute.getAsPath(), replacementAs));

      if (environment.getWriteToIntermediateBgpAttributes()) {
        BgpRoute.Builder<?, ?> ir = environment.getIntermediateBgpAttributes();
        ir.setAsPath(replace(ir.getAsPath(), replacementAs));
      }
    }
    return new Result();
  }

  private @Nonnull Optional<Long> replacement(Environment environment) {
    return new AsReplacementExprVisitor<Optional<Long>>() {
      @Override
      public Optional<Long> visitLocalAsOrConfedIfNeighborNotInConfed(
          LocalAsOrConfedIfNeighborNotInConfed localAsOrConfedForNonConfedNeighbor) {
        // TODO: return confederation if neighbor not in confederation
        return environment.getLocalAs();
      }
    }.visit(_asReplacementExpr);
  }

  private @Nonnull AsPath replace(AsPath asPath, long replacementAs) {
    return new AsSequenceExprVisitor<AsPath>() {
      @Override
      public AsPath visitAnyAs(AnyAs anyAs) {
        ImmutableList.Builder<AsSet> newAsSets = ImmutableList.builder();
        asPath.getAsSets().forEach(unusedAsSet -> newAsSets.add(AsSet.of(replacementAs)));
        return AsPath.of(newAsSets.build());
      }

      @Override
      @SuppressWarnings("PMD.AvoidReassigningLoopVariables")
      public AsPath visitAsPathSequence(AsSequence asSequence) {
        ImmutableList.Builder<AsSet> newAsSets = ImmutableList.builder();
        List<AsSet> sequenceAsAsSets =
            asSequence.getSequence().stream()
                .map(AsSet::of)
                .collect(ImmutableList.toImmutableList());
        for (int inputCursor = 0; inputCursor < asPath.length(); inputCursor++) {
          int endInclusive = inputCursor + sequenceAsAsSets.size() - 1;
          if (endInclusive < asPath.length()
              && sequenceAsAsSets.equals(
                  asPath.getAsSets().subList(inputCursor, endInclusive + 1))) {
            for (int sequenceCursor = 0;
                sequenceCursor < sequenceAsAsSets.size();
                sequenceCursor++) {
              newAsSets.add(AsSet.of(replacementAs));
              // Advance the cursor by 1 for every replacement
              inputCursor++;
            }
            // Rewind the cursor by 1, since it will be incremented at the beginning of the loop.
            inputCursor--;
          } else {
            newAsSets.add(asPath.getAsSets().get(inputCursor));
          }
        }
        return AsPath.of(newAsSets.build());
      }
    }.visit(_asSequenceExpr);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof ReplaceAsesInAsSequence)) {
      return false;
    }
    ReplaceAsesInAsSequence that = (ReplaceAsesInAsSequence) obj;
    return _asReplacementExpr.equals(that._asReplacementExpr)
        && _asSequenceExpr.equals(that._asSequenceExpr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_asSequenceExpr, _asReplacementExpr);
  }

  private final @Nonnull AsSequenceExpr _asSequenceExpr;
  private final @Nonnull AsReplacementExpr _asReplacementExpr;
}
