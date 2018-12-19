package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.util.CommonUtil;
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

    public AclIpSpace build() {
      return new AclIpSpace(_lines.build());
    }

    public Builder then(AclIpSpaceLine line) {
      if (_full) {
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

  public static final AclIpSpace DENY_ALL = AclIpSpace.builder().build();

  public static final AclIpSpace PERMIT_ALL = AclIpSpace.of(AclIpSpaceLine.PERMIT_ALL);

  private static final String PROP_LINES = "lines";

  /** */
  private static final long serialVersionUID = 1L;

  public static Builder builder() {
    return new Builder();
  }

  public static AclIpSpace of(Iterable<AclIpSpaceLine> lines) {
    Builder builder = builder();
    lines.forEach(builder::then);
    return builder.build();
  }

  public static AclIpSpace of(AclIpSpaceLine... lines) {
    return of(Arrays.asList(lines));
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
   * Set-theoretic union of multiple {@link IpSpace IP spaces}.<br>
   * {@code null} ipSpaces are ignored. If all arguments are {@code null}, returns {@code null}.
   */
  public static @Nullable IpSpace union(Iterable<IpSpace> ipSpaces) {
    IpSpace[] nonNullSpaces =
        StreamSupport.stream(ipSpaces.spliterator(), false)
            .filter(Objects::nonNull)
            .toArray(IpSpace[]::new);
    if (nonNullSpaces.length == 0) {
      // no constraint
      return null;
    }
    IpSpace[] nonEmptySpaces =
        Arrays.stream(nonNullSpaces)
            .filter(ipSpace -> ipSpace != EmptyIpSpace.INSTANCE)
            .toArray(IpSpace[]::new);
    return nonEmptySpaces.length == 0 ? EmptyIpSpace.INSTANCE : unionNonnull(nonEmptySpaces);
  }

  private static @Nullable IpSpace unionNonnull(IpSpace... ipSpaces) {
    if (ipSpaces.length == 0) {
      return null;
    } else if (ipSpaces.length == 1) {
      return ipSpaces[0];
    } else {
      return builder().thenPermitting(ipSpaces).build();
    }
  }

  private final List<AclIpSpaceLine> _lines;

  @JsonCreator
  private AclIpSpace(@JsonProperty(PROP_LINES) List<AclIpSpaceLine> lines) {
    _lines = lines;
  }

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> ipSpaceVisitor) {
    return ipSpaceVisitor.visitAclIpSpace(this);
  }

  private LineAction action(Ip ip, Map<String, IpSpace> namedIpSpaces) {
    return _lines
        .stream()
        .filter(line -> line.getIpSpace().containsIp(ip, namedIpSpaces))
        .map(AclIpSpaceLine::getAction)
        .findFirst()
        .orElse(LineAction.DENY);
  }

  @Override
  protected int compareSameClass(IpSpace o) {
    return CommonUtil.compareIterable(_lines, ((AclIpSpace) o)._lines);
  }

  @Override
  public boolean containsIp(@Nonnull Ip ip, @Nonnull Map<String, IpSpace> namedIpSpaces) {
    return action(ip, namedIpSpaces) == LineAction.PERMIT;
  }

  @Override
  protected boolean exprEquals(Object o) {
    return Objects.equals(_lines, ((AclIpSpace) o)._lines);
  }

  @JsonProperty(PROP_LINES)
  public List<AclIpSpaceLine> getLines() {
    return _lines;
  }

  @Override
  public int hashCode() {
    return _lines.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add(PROP_LINES, _lines).toString();
  }
}
