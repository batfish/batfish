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
import org.batfish.datamodel.visitors.GenericIp6SpaceVisitor;

/**
 * An ACL-based {@link Ip6Space}. An IPv6 address is permitted if it is in the space the ACL
 * represents, or denied if it is not.
 */
public class AclIp6Space extends Ip6Space {

  private static final String PROP_LINES = "lines";
  private final @Nonnull List<AclIp6SpaceLine> _lines;
  private transient int _hashCode;

  @JsonCreator
  private AclIp6Space(@JsonProperty(PROP_LINES) @Nullable List<AclIp6SpaceLine> lines) {
    checkArgument(lines != null, "Missing %s", PROP_LINES);
    _lines = lines;
  }

  public static class Builder {
    private final ImmutableList.Builder<AclIp6SpaceLine> _builderLines;
    private boolean _full;

    private Builder() {
      _builderLines = ImmutableList.builder();
    }

    public Ip6Space build() {
      List<AclIp6SpaceLine> lines = _builderLines.build();
      while (!lines.isEmpty() && lines.get(lines.size() - 1).getAction() == LineAction.DENY) {
        lines = lines.subList(0, lines.size() - 1);
      }
      if (lines.isEmpty()) {
        return EmptyIp6Space.INSTANCE;
      } else if (lines.size() == 1) {
        AclIp6SpaceLine line = lines.get(0);
        assert line.getAction() == LineAction.PERMIT;
        return line.getIp6Space();
      }
      return new AclIp6Space(lines);
    }

    public Builder then(AclIp6SpaceLine line) {
      if (_full) {
        return this;
      } else if (line.getIp6Space() instanceof EmptyIp6Space) {
        return this;
      }
      _full = line.getIp6Space() instanceof UniverseIp6Space;
      _builderLines.add(line);
      return this;
    }

    public Builder thenAction(LineAction action, Ip6Space space) {
      return then(AclIp6SpaceLine.builder().setAction(action).setIp6Space(space).build());
    }

    public Builder thenPermitting(Ip6Space... ip6Spaces) {
      return thenPermitting(Arrays.stream(ip6Spaces));
    }

    public Builder thenPermitting(Iterable<Ip6Space> ip6Spaces) {
      return thenPermitting(Streams.stream(ip6Spaces));
    }

    public Builder thenPermitting(Stream<Ip6Space> ip6Spaces) {
      if (_full) {
        return this;
      }
      ip6Spaces.map(AclIp6SpaceLine::permit).forEach(this::then);
      return this;
    }

    public Builder thenRejecting(Ip6Space... ip6Spaces) {
      return thenRejecting(Arrays.stream(ip6Spaces));
    }

    public Builder thenRejecting(Iterable<Ip6Space> ip6Spaces) {
      return thenRejecting(Streams.stream(ip6Spaces));
    }

    private Builder thenRejecting(Stream<Ip6Space> ip6Spaces) {
      if (_full) {
        return this;
      }
      ip6Spaces.map(AclIp6SpaceLine::reject).forEach(this::then);
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Ip6Space of(Iterable<AclIp6SpaceLine> lines) {
    Builder builder = builder();
    lines.forEach(builder::then);
    return builder.build();
  }

  public static Ip6Space of(AclIp6SpaceLine... lines) {
    return of(Arrays.asList(lines));
  }

  @Override
  public Ip6Space complement() {
    if (_lines.size() == 2
        && _lines.get(1).getAction() == LineAction.PERMIT
        && _lines.get(1).getIp6Space() == UniverseIp6Space.INSTANCE) {
      // This AclIp6Space is a complement already.
      assert _lines.get(0).getAction() == LineAction.DENY;
      return _lines.get(0).getIp6Space();
    }
    return super.complement();
  }

  /**
   * Set-theoretic difference between two Ip6Spaces.<br>
   * If both arguments are {@code null}, returns {@code null}.<br>
   * If just {@code ip6Space1} is {@code null}, treat it as {@link UniverseIp6Space}.<br>
   * If just {@code ip6Space2} is {@code null}, treat it as {@link EmptyIp6Space}.
   */
  public static @Nullable Ip6Space difference(
      @Nullable Ip6Space ip6Space1, @Nullable Ip6Space ip6Space2) {
    if (ip6Space1 == null && ip6Space2 == null) {
      return null;
    } else if (ip6Space2 == null) {
      return ip6Space1;
    } else if (EmptyIp6Space.INSTANCE.equals(ip6Space1)) {
      return EmptyIp6Space.INSTANCE;
    }
    return builder()
        .thenRejecting(ip6Space2)
        .thenPermitting(firstNonNull(ip6Space1, UniverseIp6Space.INSTANCE))
        .build();
  }

  /** Set-theoretic intersection of multiple Ip6Spaces */
  public static @Nullable Ip6Space intersection(Ip6Space... ip6Spaces) {
    return intersection(Arrays.spliterator(ip6Spaces));
  }

  /** Set-theoretic intersection of multiple Ip6Spaces */
  public static @Nullable Ip6Space intersection(Iterable<Ip6Space> ip6Spaces) {
    return intersection(ip6Spaces.spliterator());
  }

  private static @Nullable Ip6Space intersection(Spliterator<Ip6Space> ip6Spaces) {
    Ip6Space[] nonNullSpaces =
        StreamSupport.stream(ip6Spaces, false).filter(Objects::nonNull).toArray(Ip6Space[]::new);

    if (nonNullSpaces.length == 0) {
      // all null
      return null;
    }

    Ip6Space[] nonUniverseSpaces =
        Arrays.stream(nonNullSpaces)
            .filter(ip6Space -> ip6Space != UniverseIp6Space.INSTANCE)
            .toArray(Ip6Space[]::new);

    if (nonUniverseSpaces.length == 0) {
      // all UniverseIp6Space
      return UniverseIp6Space.INSTANCE;
    }

    if (nonUniverseSpaces.length == 1) {
      return nonUniverseSpaces[0];
    }

    // complement each concrete space.
    for (int i = 0; i < nonUniverseSpaces.length; i++) {
      if (nonUniverseSpaces[i] == EmptyIp6Space.INSTANCE) {
        return EmptyIp6Space.INSTANCE;
      }
      nonUniverseSpaces[i] = nonUniverseSpaces[i].complement();
    }

    return builder()
        .thenRejecting(nonUniverseSpaces)
        .thenPermitting(UniverseIp6Space.INSTANCE)
        .build();
  }

  public static Builder permitting(Ip6Space... ip6Spaces) {
    return new Builder().thenPermitting(ip6Spaces);
  }

  public static Builder permitting(Iterable<Ip6Space> ip6Spaces) {
    return new Builder().thenPermitting(ip6Spaces);
  }

  public static Builder permitting(Stream<Ip6Space> ip6Spaces) {
    return new Builder().thenPermitting(ip6Spaces);
  }

  public static Builder rejecting(Ip6Space... ip6Spaces) {
    return new Builder().thenRejecting(ip6Spaces);
  }

  public static Builder rejecting(Iterable<Ip6Space> ip6Spaces) {
    return new Builder().thenRejecting(ip6Spaces);
  }

  /**
   * Set-theoretic union of multiple {@link Ip6Space IP spaces}.<br>
   * {@code null} ip6Spaces are ignored. If all arguments are {@code null}, returns {@code null}.
   */
  public static @Nullable Ip6Space union(Ip6Space... ip6Spaces) {
    return union(Arrays.asList(ip6Spaces));
  }

  /**
   * When an {@link AclIp6Space} is a pure union (list of only permit lines), flattens it into the
   * permitted spaces.
   *
   * <p>This function makes {@code union(union(a, b), c)} equivalent to {@code union(a, b, c)}.
   */
  private static Stream<Ip6Space> flattenAclIp6SpacesForUnion(Ip6Space space) {
    if (!(space instanceof AclIp6Space)) {
      return Stream.of(space);
    }
    AclIp6Space aclSpace = (AclIp6Space) space;
    List<AclIp6SpaceLine> lines = aclSpace.getLines();
    if (lines.stream().allMatch(l -> l.getAction() == LineAction.PERMIT)) {
      // This is just a big union, flatten it into the list of spaces it unions.
      return lines.stream().map(AclIp6SpaceLine::getIp6Space);
    }
    // Not a pure union, so don't flatten.
    return Stream.of(aclSpace);
  }

  /**
   * Set-theoretic union of multiple {@link Ip6Space IP spaces}.<br>
   * {@code null} ip6Spaces are ignored. If all arguments are {@code null}, returns {@code null}.
   */
  public static @Nullable Ip6Space union(Iterable<Ip6Space> ip6Spaces) {
    // In one pass, determine if the iterable contains a universe, contains anything, and filter out
    // null/empty spaces. Have to do this complicated algorithm to properly flatten AclIp6Spaces.
    boolean[] hasUniverse = new boolean[] {false};
    boolean[] hasAnything = new boolean[] {false};
    Ip6Space[] nonEmptySpaces =
        StreamSupport.stream(ip6Spaces.spliterator(), false)
            .filter(Objects::nonNull)
            .flatMap(AclIp6Space::flattenAclIp6SpacesForUnion)
            .filter(
                s -> {
                  hasAnything[0] = true;
                  if (s instanceof UniverseIp6Space) {
                    hasUniverse[0] = true;
                    return false; // can't fully exit from here, but don't include in array
                  }
                  return !(s instanceof EmptyIp6Space);
                })
            .toArray(Ip6Space[]::new);

    if (hasUniverse[0]) {
      return UniverseIp6Space.INSTANCE;
    } else if (!hasAnything[0]) {
      // no constraint
      return null;
    } else if (nonEmptySpaces.length == 0) {
      return EmptyIp6Space.INSTANCE;
    } else if (nonEmptySpaces.length == 1) {
      return nonEmptySpaces[0];
    }
    return builder().thenPermitting(nonEmptySpaces).build();
  }

  @Override
  public <R> R accept(GenericIp6SpaceVisitor<R> ip6SpaceVisitor) {
    return ip6SpaceVisitor.visitAclIp6Space(this);
  }

  @Override
  protected int compareSameClass(Ip6Space o) {
    return Comparators.lexicographical(Ordering.<AclIp6SpaceLine>natural())
        .compare(_lines, ((AclIp6Space) o)._lines);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return _lines.equals(((AclIp6Space) o)._lines);
  }

  @JsonProperty(PROP_LINES)
  public @Nonnull List<AclIp6SpaceLine> getLines() {
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
  public @Nonnull String toString() {
    return MoreObjects.toStringHelper(getClass()).add(PROP_LINES, _lines).toString();
  }
}
