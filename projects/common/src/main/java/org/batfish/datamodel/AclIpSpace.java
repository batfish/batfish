package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

/**
 * An ACL-based {@link IpSpace}. An IP is permitted if it is in the space the ACL represents, or
 * denied if it is not.
 */
public class AclIpSpace extends IpSpace {

  public static class Builder {

    private final ImmutableList.Builder<AclIpSpaceLine> _lines;
    /*
     * Whether we know this Builder is full, aka whether it has an empty complement. If true, we
     * will stop adding new lines, since they could never match anything.
     */
    private boolean _full;

    private Builder() {
      _lines = ImmutableList.builder();
    }

    public IpSpace build() {
      List<AclIpSpaceLine> lines = _lines.build();
      while (!lines.isEmpty() && lines.get(lines.size() - 1).getAction() == LineAction.DENY) {
        lines = lines.subList(0, lines.size() - 1);
      }
      if (lines.isEmpty()) {
        return EmptyIpSpace.INSTANCE;
      } else if (lines.size() == 1) {
        AclIpSpaceLine line = lines.get(0);
        assert line.getAction() == LineAction.PERMIT;
        return line.getIpSpace();
      }
      return new AclIpSpace(lines);
    }

    public Builder then(AclIpSpaceLine line) {
      if (_full) {
        return this;
      } else if (line.getIpSpace() instanceof EmptyIpSpace) {
        return this;
      }
      _full = line.getIpSpace() instanceof UniverseIpSpace;
      _lines.add(line);
      return this;
    }

    public Builder thenAction(LineAction action, IpSpace space) {
      return then(AclIpSpaceLine.builder().setAction(action).setIpSpace(space).build());
    }

    public Builder thenPermitting(IpSpace... ipSpaces) {
      return thenPermitting(Arrays.stream(ipSpaces));
    }

    public Builder thenPermitting(Iterable<IpSpace> ipSpaces) {
      return thenPermitting(Streams.stream(ipSpaces));
    }

    public Builder thenPermitting(Stream<IpSpace> ipSpaces) {
      if (_full) {
        return this;
      }
      ipSpaces.map(AclIpSpaceLine::permit).forEach(this::then);
      return this;
    }

    public Builder thenRejecting(IpSpace... ipSpaces) {
      return thenRejecting(Arrays.stream(ipSpaces));
    }

    public Builder thenRejecting(Iterable<IpSpace> ipSpaces) {
      return thenRejecting(Streams.stream(ipSpaces));
    }

    private Builder thenRejecting(Stream<IpSpace> ipSpaces) {
      if (_full) {
        return this;
      }
      ipSpaces.map(AclIpSpaceLine::reject).forEach(this::then);
      return this;
    }
  }

  private static final String PROP_LINES = "lines";

  public static Builder builder() {
    return new Builder();
  }

  public static IpSpace of(Iterable<AclIpSpaceLine> lines) {
    Builder builder = builder();
    lines.forEach(builder::then);
    return builder.build();
  }

  public static IpSpace of(AclIpSpaceLine... lines) {
    return of(Arrays.asList(lines));
  }

  @Override
  public IpSpace complement() {
    if (_lines.size() == 2
        && _lines.get(1).getAction() == LineAction.PERMIT
        && _lines.get(1).getIpSpace() == UniverseIpSpace.INSTANCE) {
      // This AclIpSpace is a complement already.
      assert _lines.get(0).getAction() == LineAction.DENY;
      return _lines.get(0).getIpSpace();
    }
    return super.complement();
  }

  /**
   * Set-theoretic difference between two IpSpaces.<br>
   * If both arguments are {@code null}, returns {@code null}.<br>
   * If just {@code ipSpace1} is {@code null}, treat it as {@link UniverseIpSpace}.<br>
   * If just {@code ipSpace2} is {@code null}, treat it as {@link EmptyIpSpace}.
   */
  public static @Nullable IpSpace difference(
      @Nullable IpSpace ipSpace1, @Nullable IpSpace ipSpace2) {
    if (ipSpace1 == null && ipSpace2 == null) {
      return null;
    } else if (ipSpace2 == null) {
      return ipSpace1;
    } else if (EmptyIpSpace.INSTANCE.equals(ipSpace1)) {
      return EmptyIpSpace.INSTANCE;
    }
    return builder()
        .thenRejecting(ipSpace2)
        .thenPermitting(firstNonNull(ipSpace1, UniverseIpSpace.INSTANCE))
        .build();
  }

  /** Set-theoretic intersection of multiple IpSpaces */
  public static @Nullable IpSpace intersection(IpSpace... ipSpaces) {
    return intersection(Arrays.spliterator(ipSpaces));
  }

  /** Set-theoretic intersection of multiple IpSpaces */
  public static @Nullable IpSpace intersection(Iterable<IpSpace> ipSpaces) {
    return intersection(ipSpaces.spliterator());
  }

  private static @Nullable IpSpace intersection(Spliterator<IpSpace> ipSpaces) {
    IpSpace[] nonNullSpaces =
        StreamSupport.stream(ipSpaces, false).filter(Objects::nonNull).toArray(IpSpace[]::new);

    if (nonNullSpaces.length == 0) {
      // all null
      return null;
    }

    IpSpace[] nonUniverseSpaces =
        Arrays.stream(nonNullSpaces)
            .filter(ipSpace -> ipSpace != UniverseIpSpace.INSTANCE)
            .toArray(IpSpace[]::new);

    if (nonUniverseSpaces.length == 0) {
      // all UniverseIpSpace
      return UniverseIpSpace.INSTANCE;
    }

    if (nonUniverseSpaces.length == 1) {
      return nonUniverseSpaces[0];
    }

    // complement each concrete space.
    for (int i = 0; i < nonUniverseSpaces.length; i++) {
      if (nonUniverseSpaces[i] == EmptyIpSpace.INSTANCE) {
        return EmptyIpSpace.INSTANCE;
      }
      nonUniverseSpaces[i] = nonUniverseSpaces[i].complement();
    }

    return builder()
        .thenRejecting(nonUniverseSpaces)
        .thenPermitting(UniverseIpSpace.INSTANCE)
        .build();
  }

  public static Builder permitting(IpSpace... ipSpaces) {
    return new Builder().thenPermitting(ipSpaces);
  }

  public static Builder permitting(Iterable<IpSpace> ipSpaces) {
    return new Builder().thenPermitting(ipSpaces);
  }

  public static Builder permitting(Stream<IpSpace> ipSpaces) {
    return new Builder().thenPermitting(ipSpaces);
  }

  public static Builder rejecting(IpSpace... ipSpaces) {
    return new Builder().thenRejecting(ipSpaces);
  }

  public static Builder rejecting(Iterable<IpSpace> ipSpaces) {
    return new Builder().thenRejecting(ipSpaces);
  }

  /**
   * Set-theoretic union of multiple {@link IpSpace IP spaces}.<br>
   * {@code null} ipSpaces are ignored. If all arguments are {@code null}, returns {@code null}.
   */
  public static @Nullable IpSpace union(IpSpace... ipSpaces) {
    return union(Arrays.asList(ipSpaces));
  }

  /**
   * When an {@link AclIpSpace} is a pure union (list of only permit lines), flattens it into the
   * permitted spaces.
   *
   * <p>This function makes {@code union(union(a, b), c)} equivalent to {@code union(a, b, c)}.
   */
  private static Stream<IpSpace> flattenAclIpSpacesForUnion(IpSpace space) {
    if (!(space instanceof AclIpSpace)) {
      return Stream.of(space);
    }
    AclIpSpace aclSpace = (AclIpSpace) space;
    List<AclIpSpaceLine> lines = aclSpace.getLines();
    if (lines.stream().allMatch(l -> l.getAction() == LineAction.PERMIT)) {
      // This is just a big union, flatten it into the list of spaces it unions.
      return lines.stream().map(AclIpSpaceLine::getIpSpace);
    }
    // Not a pure union, so don't flatten.
    return Stream.of(aclSpace);
  }

  /**
   * Set-theoretic union of multiple {@link IpSpace IP spaces}.<br>
   * {@code null} ipSpaces are ignored. If all arguments are {@code null}, returns {@code null}.
   */
  public static @Nullable IpSpace union(Iterable<IpSpace> ipSpaces) {
    // In one pass, determine if the iterable contains a universe, contains anything, and filter out
    // null/empty spaces. Have to do this complicated algorithm to properly flatten AclIpSpaces.
    boolean[] hasUniverse = new boolean[] {false};
    boolean[] hasAnything = new boolean[] {false};
    IpSpace[] nonEmptySpaces =
        StreamSupport.stream(ipSpaces.spliterator(), false)
            .filter(Objects::nonNull)
            .flatMap(AclIpSpace::flattenAclIpSpacesForUnion)
            .filter(
                s -> {
                  hasAnything[0] = true;
                  if (s instanceof UniverseIpSpace) {
                    hasUniverse[0] = true;
                    return false; // can't fully exit from here, but don't include in array
                  }
                  return !(s instanceof EmptyIpSpace);
                })
            .toArray(IpSpace[]::new);

    if (hasUniverse[0]) {
      return UniverseIpSpace.INSTANCE;
    } else if (!hasAnything[0]) {
      // no constraint
      return null;
    } else if (nonEmptySpaces.length == 0) {
      return EmptyIpSpace.INSTANCE;
    } else if (nonEmptySpaces.length == 1) {
      return nonEmptySpaces[0];
    }
    return builder().thenPermitting(nonEmptySpaces).build();
  }

  private final @Nonnull List<AclIpSpaceLine> _lines;

  @JsonCreator
  private AclIpSpace(@JsonProperty(PROP_LINES) @Nullable List<AclIpSpaceLine> lines) {
    checkArgument(lines != null, "Missing %s", PROP_LINES);
    _lines = lines;
  }

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> ipSpaceVisitor) {
    return ipSpaceVisitor.visitAclIpSpace(this);
  }

  @Override
  protected int compareSameClass(IpSpace o) {
    return Comparators.lexicographical(Ordering.<AclIpSpaceLine>natural())
        .compare(_lines, ((AclIpSpace) o)._lines);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return _lines.equals(((AclIpSpace) o)._lines);
  }

  @JsonProperty(PROP_LINES)
  public @Nonnull List<AclIpSpaceLine> getLines() {
    return _lines;
  }

  @Override
  public int hashCode() {
    int hash = _hashCode;
    if (hash == 0) {
      hash = _lines.hashCode();
      _hashCode = hash;
    }
    return hash;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add(PROP_LINES, _lines).toString();
  }

  private int _hashCode;
}
