package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;

/** Allows matching flows with specified TCP flags */
public final class TcpFlagsMatchConditions
    implements Serializable, Comparable<TcpFlagsMatchConditions> {
  private static final String PROP_TCP_FLAGS = "tcpFlags";
  private static final String PROP_USE_ACK = "useAck";
  private static final String PROP_USE_CWR = "useCwr";
  private static final String PROP_USE_ECE = "useEce";
  private static final String PROP_USE_FIN = "useFin";
  private static final String PROP_USE_PSH = "usePsh";
  private static final String PROP_USE_RST = "useRst";
  private static final String PROP_USE_SYN = "useSyn";
  private static final String PROP_USE_URG = "useUrg";

  private final TcpFlags _tcpFlags;

  /** Builder for {@link TcpFlags} */
  public static class Builder {
    private TcpFlags _tcpFlags;
    private boolean _useAck;
    private boolean _useCwr;
    private boolean _useEce;
    private boolean _useFin;
    private boolean _usePsh;
    private boolean _useRst;
    private boolean _useSyn;
    private boolean _useUrg;

    private Builder() {}

    public TcpFlagsMatchConditions build() {
      return new TcpFlagsMatchConditions(
          _tcpFlags, _useAck, _useCwr, _useEce, _useFin, _usePsh, _useRst, _useSyn, _useUrg);
    }

    public Builder setTcpFlags(@Nonnull TcpFlags tcpFlags) {
      _tcpFlags = tcpFlags;
      return this;
    }

    public Builder setUseAck(boolean useAck) {
      _useAck = useAck;
      return this;
    }

    public Builder setUseCwr(boolean useCwr) {
      _useCwr = useCwr;
      return this;
    }

    public Builder setUseEce(boolean useEce) {
      _useEce = useEce;
      return this;
    }

    public Builder setUseFin(boolean useFin) {
      _useFin = useFin;
      return this;
    }

    public Builder setUsePsh(boolean usePsh) {
      _usePsh = usePsh;
      return this;
    }

    public Builder setUseRst(boolean useRst) {
      _useRst = useRst;
      return this;
    }

    public Builder setUseSyn(boolean useSyn) {
      _useSyn = useSyn;
      return this;
    }

    public Builder setUseUrg(boolean useUrg) {
      _useUrg = useUrg;
      return this;
    }
  }

  /**
   * Shorthand for match conditions for a ACK (acknowledgement) packet. Other bits are
   * unconstrained.
   */
  public static final TcpFlagsMatchConditions ACK_TCP_FLAG =
      builder().setTcpFlags(TcpFlags.builder().setAck(true).build()).setUseAck(true).build();

  /** Shorthand for match conditions for a RST (reset) packet. Other bits are unconstrained. */
  public static final TcpFlagsMatchConditions RST_TCP_FLAG =
      builder().setTcpFlags(TcpFlags.builder().setRst(true).build()).setUseRst(true).build();

  /** Shorthand for match conditions for a SYN-ACK packet. Other bits are unconstrained. */
  public static final TcpFlagsMatchConditions SYN_ACK_TCP_FLAG =
      builder()
          .setTcpFlags(TcpFlags.builder().setAck(true).setSyn(true).build())
          .setUseAck(true)
          .setUseSyn(true)
          .build();

  /** Shorthand for match conditions for a SYN-only packet, with all other bits cleared */
  public static final TcpFlagsMatchConditions SYN_ONLY_TCP_FLAG =
      builder()
          .setTcpFlags(TcpFlags.builder().setSyn(true).build())
          .setUseAck(true)
          .setUseCwr(true)
          .setUseFin(true)
          .setUseEce(true)
          .setUsePsh(true)
          .setUseRst(true)
          .setUseSyn(true)
          .setUseUrg(true)
          .build();

  private static final Comparator<TcpFlagsMatchConditions> COMPARATOR =
      Comparator.comparing(TcpFlagsMatchConditions::getTcpFlags)
          .thenComparing(TcpFlagsMatchConditions::getUseAck)
          .thenComparing(TcpFlagsMatchConditions::getUseCwr)
          .thenComparing(TcpFlagsMatchConditions::getUseEce)
          .thenComparing(TcpFlagsMatchConditions::getUseFin)
          .thenComparing(TcpFlagsMatchConditions::getUsePsh)
          .thenComparing(TcpFlagsMatchConditions::getUseRst)
          .thenComparing(TcpFlagsMatchConditions::getUseSyn)
          .thenComparing(TcpFlagsMatchConditions::getUseUrg);

  public static Builder builder() {
    return new Builder();
  }

  private final boolean _useAck;
  private final boolean _useCwr;
  private final boolean _useEce;
  private final boolean _useFin;
  private final boolean _usePsh;
  private final boolean _useRst;
  private final boolean _useSyn;
  private final boolean _useUrg;

  @JsonCreator
  private TcpFlagsMatchConditions(
      @JsonProperty(PROP_TCP_FLAGS) TcpFlags tcpFlags,
      @JsonProperty(PROP_USE_ACK) boolean useAck,
      @JsonProperty(PROP_USE_CWR) boolean useCwr,
      @JsonProperty(PROP_USE_ECE) boolean useEce,
      @JsonProperty(PROP_USE_FIN) boolean useFin,
      @JsonProperty(PROP_USE_PSH) boolean usePsh,
      @JsonProperty(PROP_USE_RST) boolean useRst,
      @JsonProperty(PROP_USE_SYN) boolean useSyn,
      @JsonProperty(PROP_USE_URG) boolean useUrg) {
    _tcpFlags = firstNonNull(tcpFlags, TcpFlags.builder().build());
    _useAck = useAck;
    _useCwr = useCwr;
    _useEce = useEce;
    _useFin = useFin;
    _usePsh = usePsh;
    _useRst = useRst;
    _useSyn = useSyn;
    _useUrg = useUrg;
  }

  /** Check if any flags are using for matching */
  public boolean anyUsed() {
    return _useAck || _useCwr || _useEce || _useFin || _usePsh || _useRst || _useSyn || _useUrg;
  }

  @Override
  public int compareTo(TcpFlagsMatchConditions o) {
    return COMPARATOR.compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TcpFlagsMatchConditions)) {
      return false;
    }
    TcpFlagsMatchConditions other = (TcpFlagsMatchConditions) o;
    return Objects.equals(_tcpFlags, other._tcpFlags)
        && _useAck == other._useAck
        && _useCwr == other._useCwr
        && _useEce == other._useEce
        && _useFin == other._useFin
        && _usePsh == other._usePsh
        && _useRst == other._useRst
        && _useSyn == other._useSyn
        && _useUrg == other._useUrg;
  }

  @JsonProperty(PROP_TCP_FLAGS)
  public @Nonnull TcpFlags getTcpFlags() {
    return _tcpFlags;
  }

  @JsonProperty(PROP_USE_ACK)
  public boolean getUseAck() {
    return _useAck;
  }

  @JsonProperty(PROP_USE_CWR)
  public boolean getUseCwr() {
    return _useCwr;
  }

  @JsonProperty(PROP_USE_ECE)
  public boolean getUseEce() {
    return _useEce;
  }

  @JsonProperty(PROP_USE_FIN)
  public boolean getUseFin() {
    return _useFin;
  }

  @JsonProperty(PROP_USE_PSH)
  public boolean getUsePsh() {
    return _usePsh;
  }

  @JsonProperty(PROP_USE_RST)
  public boolean getUseRst() {
    return _useRst;
  }

  @JsonProperty(PROP_USE_SYN)
  public boolean getUseSyn() {
    return _useSyn;
  }

  @JsonProperty(PROP_USE_URG)
  public boolean getUseUrg() {
    return _useUrg;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _tcpFlags, _useAck, _useCwr, _useEce, _useFin, _usePsh, _useRst, _useSyn, _useUrg);
  }

  /** Create new {@link TcpFlagsMatchConditions} that matches all of the given {@code flags}. */
  public static TcpFlagsMatchConditions matchAllFlags(TcpFlags flags) {
    return new TcpFlagsMatchConditions(flags, true, true, true, true, true, true, true, true);
  }

  /**
   * Returns {@code true} iff the TCP flags for which matching is enabled match the TCP flags in the
   * given {@link Flow}.
   *
   * <p>Note this function will return {@code true} if no bits are used.
   */
  public boolean match(Flow flow) {
    TcpFlags flags = firstNonNull(flow.getTcpFlags(), TcpFlags.FALSE);
    return !(_useAck && _tcpFlags.getAck() ^ flags.getAck())
        && !(_useCwr && _tcpFlags.getCwr() ^ flags.getCwr())
        && !(_useEce && _tcpFlags.getEce() ^ flags.getEce())
        && !(_useFin && _tcpFlags.getFin() ^ flags.getFin())
        && !(_usePsh && _tcpFlags.getPsh() ^ flags.getPsh())
        && !(_useRst && _tcpFlags.getRst() ^ flags.getRst())
        && !(_useSyn && _tcpFlags.getSyn() ^ flags.getSyn())
        && !(_useUrg && _tcpFlags.getUrg() ^ flags.getUrg());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_tcpFlags", _tcpFlags)
        .add("_useAck", _useAck)
        .add("_useCwr", _useCwr)
        .add("_useEce", _useEce)
        .add("_useFin", _useFin)
        .add("_usePsh", _usePsh)
        .add("_useRst", _useRst)
        .add("_useSyn", _useSyn)
        .add("_useUrg", _useUrg)
        .toString();
  }
}
