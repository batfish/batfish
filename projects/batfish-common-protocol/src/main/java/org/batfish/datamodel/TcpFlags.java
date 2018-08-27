package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.io.Serializable;
import java.util.Comparator;

public final class TcpFlags implements Serializable, Comparable<TcpFlags> {

  private static final String PROP_ACK = "ack";
  private static final String PROP_CWR = "cwr";
  private static final String PROP_ECE = "ece";
  private static final String PROP_FIN = "fin";
  private static final String PROP_PSH = "psh";
  private static final String PROP_RST = "rst";
  private static final String PROP_SYN = "syn";
  private static final String PROP_URG = "urg";
  private static final String PROP_USE_ACK = "useAck";
  private static final String PROP_USE_CWR = "useCwr";
  private static final String PROP_USE_ECE = "useEce";
  private static final String PROP_USE_FIN = "useFin";
  private static final String PROP_USE_PSH = "usePsh";
  private static final String PROP_USE_RST = "useRst";
  private static final String PROP_USE_SYN = "useSyn";
  private static final String PROP_USE_URG = "useUrg";

  public static class Builder {
    private boolean _ack;

    private boolean _cwr;

    private boolean _ece;

    private boolean _fin;

    private boolean _psh;

    private boolean _rst;

    private boolean _syn;

    private boolean _urg;

    private boolean _useAck;

    private boolean _useCwr;

    private boolean _useEce;

    private boolean _useFin;

    private boolean _usePsh;

    private boolean _useRst;

    private boolean _useSyn;

    private boolean _useUrg;

    private Builder() {}

    public TcpFlags build() {
      return new TcpFlags(
          _ack, _cwr, _ece, _fin, _psh, _rst, _syn, _urg, _useAck, _useCwr, _useEce, _useFin,
          _usePsh, _useRst, _useSyn, _useUrg);
    }

    public Builder setAck(boolean ack) {
      _ack = ack;
      return this;
    }

    public Builder setCwr(boolean cwr) {
      _cwr = cwr;
      return this;
    }

    public Builder setEce(boolean ece) {
      _ece = ece;
      return this;
    }

    public Builder setFin(boolean fin) {
      _fin = fin;
      return this;
    }

    public Builder setPsh(boolean psh) {
      _psh = psh;
      return this;
    }

    public Builder setRst(boolean rst) {
      _rst = rst;
      return this;
    }

    public Builder setSyn(boolean syn) {
      _syn = syn;
      return this;
    }

    public Builder setUrg(boolean urg) {
      _urg = urg;
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

  public static final int ACK = 0x10;

  public static final TcpFlags ACK_TCP_FLAG = builder().setAck(true).setUseAck(true).build();

  private static final Comparator<TcpFlags> COMPARATOR =
      Comparator.comparing(TcpFlags::getAck)
          .thenComparing(TcpFlags::getCwr)
          .thenComparing(TcpFlags::getEce)
          .thenComparing(TcpFlags::getFin)
          .thenComparing(TcpFlags::getPsh)
          .thenComparing(TcpFlags::getRst)
          .thenComparing(TcpFlags::getSyn)
          .thenComparing(TcpFlags::getUrg)
          .thenComparing(TcpFlags::getUseAck)
          .thenComparing(TcpFlags::getUseCwr)
          .thenComparing(TcpFlags::getUseEce)
          .thenComparing(TcpFlags::getUseFin)
          .thenComparing(TcpFlags::getUsePsh)
          .thenComparing(TcpFlags::getUseRst)
          .thenComparing(TcpFlags::getUseSyn)
          .thenComparing(TcpFlags::getUseUrg);

  public static final int CWR = 0x80;

  public static final int ECE = 0x40;

  public static final int FIN = 0x01;

  public static final int PSH = 0x08;

  public static final int RST = 0x04;

  /** */
  private static final long serialVersionUID = 1L;

  public static final int SYN = 0x02;

  public static final int URG = 0x20;

  public static Builder builder() {
    return new Builder();
  }

  private boolean _ack;

  private boolean _cwr;

  private boolean _ece;

  private boolean _fin;

  private boolean _psh;

  private boolean _rst;

  private boolean _syn;

  private boolean _urg;

  private boolean _useAck;

  private boolean _useCwr;

  private boolean _useEce;

  private boolean _useFin;

  private boolean _usePsh;

  private boolean _useRst;

  private boolean _useSyn;

  private boolean _useUrg;

  @JsonCreator
  public TcpFlags() {}

  private TcpFlags(
      boolean ack,
      boolean cwr,
      boolean ece,
      boolean fin,
      boolean psh,
      boolean rst,
      boolean syn,
      boolean urg,
      boolean useAck,
      boolean useCwr,
      boolean useEce,
      boolean useFin,
      boolean usePsh,
      boolean useRst,
      boolean useSyn,
      boolean useUrg) {
    _ack = ack;
    _cwr = cwr;
    _ece = ece;
    _fin = fin;
    _psh = psh;
    _rst = rst;
    _syn = syn;
    _urg = urg;
    _useAck = useAck;
    _useCwr = useCwr;
    _useEce = useEce;
    _useFin = useFin;
    _usePsh = usePsh;
    _useRst = useRst;
    _useSyn = useSyn;
    _useUrg = useUrg;
  }

  public boolean anyUsed() {
    return _useAck || _useCwr || _useEce || _useFin || _usePsh || _useRst || _useSyn || _useUrg;
  }

  @Override
  public int compareTo(TcpFlags o) {
    return COMPARATOR.compare(this, o);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof TcpFlags)) {
      return false;
    }
    TcpFlags other = (TcpFlags) obj;
    return other.toString().equals(this.toString());
  }

  @JsonPropertyDescription("Value for ACK bit if used (true->1/false->0)")
  @JsonProperty(PROP_ACK)
  public boolean getAck() {
    return _ack;
  }

  @JsonPropertyDescription("Value for CWR bit if used (true->1/false->0)")
  @JsonProperty(PROP_CWR)
  public boolean getCwr() {
    return _cwr;
  }

  @JsonPropertyDescription("Value for ECE bit if used (true->1/false->0)")
  @JsonProperty(PROP_ECE)
  public boolean getEce() {
    return _ece;
  }

  @JsonPropertyDescription("Value for FIN bit if used (true->1/false->0)")
  @JsonProperty(PROP_FIN)
  public boolean getFin() {
    return _fin;
  }

  @JsonPropertyDescription("Value for PSH bit if used (true->1/false->0)")
  @JsonProperty(PROP_PSH)
  public boolean getPsh() {
    return _psh;
  }

  @JsonPropertyDescription("Value for RST bit if used (true->1/false->0)")
  @JsonProperty(PROP_RST)
  public boolean getRst() {
    return _rst;
  }

  @JsonPropertyDescription("Value for SYN bit if used (true->1/false->0)")
  @JsonProperty(PROP_SYN)
  public boolean getSyn() {
    return _syn;
  }

  @JsonPropertyDescription("Value for URG bit if used (true->1/false->0)")
  @JsonProperty(PROP_URG)
  public boolean getUrg() {
    return _urg;
  }

  @JsonPropertyDescription("Whether or not to match against the ACK bit")
  @JsonProperty(PROP_USE_ACK)
  public boolean getUseAck() {
    return _useAck;
  }

  @JsonPropertyDescription("Whether or not to match against the CWR bit")
  @JsonProperty(PROP_USE_CWR)
  public boolean getUseCwr() {
    return _useCwr;
  }

  @JsonPropertyDescription("Whether or not to match against the ECE bit")
  @JsonProperty(PROP_USE_ECE)
  public boolean getUseEce() {
    return _useEce;
  }

  @JsonPropertyDescription("Whether or not to match against the FIN bit")
  @JsonProperty(PROP_USE_FIN)
  public boolean getUseFin() {
    return _useFin;
  }

  @JsonPropertyDescription("Whether or not to match against the PSH bit")
  @JsonProperty(PROP_USE_PSH)
  public boolean getUsePsh() {
    return _usePsh;
  }

  @JsonPropertyDescription("Whether or not to match against the RST bit")
  @JsonProperty(PROP_USE_RST)
  public boolean getUseRst() {
    return _useRst;
  }

  @JsonPropertyDescription("Whether or not to match against the SYN bit")
  @JsonProperty(PROP_USE_SYN)
  public boolean getUseSyn() {
    return _useSyn;
  }

  @JsonPropertyDescription("Whether or not to match against the URG bit")
  @JsonProperty(PROP_USE_URG)
  public boolean getUseUrg() {
    return _useUrg;
  }

  @Override
  public int hashCode() {
    // TODO: implement better hashcode
    return 0;
  }

  /**
   * Returns {@code true} iff the TCP flags used in this object (configured via e.g. {@link
   * #setUseAck}) match the TCP flags in the given {@link Flow}.
   *
   * <p>Note this function will return {@code true} if no bits are used.
   */
  public boolean match(Flow flow) {
    return !(_useAck && _ack ^ (flow.getTcpFlagsAck() == 1))
        && !(_useCwr && _cwr ^ (flow.getTcpFlagsCwr() == 1))
        && !(_useEce && _ece ^ (flow.getTcpFlagsEce() == 1))
        && !(_useFin && _fin ^ (flow.getTcpFlagsFin() == 1))
        && !(_usePsh && _psh ^ (flow.getTcpFlagsPsh() == 1))
        && !(_useRst && _rst ^ (flow.getTcpFlagsRst() == 1))
        && !(_useSyn && _syn ^ (flow.getTcpFlagsSyn() == 1))
        && !(_useUrg && _urg ^ (flow.getTcpFlagsUrg() == 1));
  }

  /**
   * Returns {@code true} iff the TCP flags used in this object (configured via e.g. {@link
   * #setUseAck}) match the TCP flags in the given {@link Flow6}.
   *
   * <p>Note this function will return {@code true} if no bits are used.
   */
  public boolean match(Flow6 flow6) {
    return !(_useAck && _ack ^ (flow6.getTcpFlagsAck() == 1))
        && !(_useCwr && _cwr ^ (flow6.getTcpFlagsCwr() == 1))
        && !(_useEce && _ece ^ (flow6.getTcpFlagsEce() == 1))
        && !(_useFin && _fin ^ (flow6.getTcpFlagsFin() == 1))
        && !(_usePsh && _psh ^ (flow6.getTcpFlagsPsh() == 1))
        && !(_useRst && _rst ^ (flow6.getTcpFlagsRst() == 1))
        && !(_useSyn && _syn ^ (flow6.getTcpFlagsSyn() == 1))
        && !(_useUrg && _urg ^ (flow6.getTcpFlagsUrg() == 1));
  }

  @JsonProperty(PROP_ACK)
  public void setAck(boolean ack) {
    _ack = ack;
  }

  @JsonProperty(PROP_CWR)
  public void setCwr(boolean cwr) {
    _cwr = cwr;
  }

  @JsonProperty(PROP_ECE)
  public void setEce(boolean ece) {
    _ece = ece;
  }

  @JsonProperty(PROP_FIN)
  public void setFin(boolean fin) {
    _fin = fin;
  }

  @JsonProperty(PROP_PSH)
  public void setPsh(boolean psh) {
    _psh = psh;
  }

  @JsonProperty(PROP_RST)
  public void setRst(boolean rst) {
    _rst = rst;
  }

  @JsonProperty(PROP_SYN)
  public void setSyn(boolean syn) {
    _syn = syn;
  }

  @JsonProperty(PROP_URG)
  public void setUrg(boolean urg) {
    _urg = urg;
  }

  @JsonProperty(PROP_USE_ACK)
  public void setUseAck(boolean useAck) {
    _useAck = useAck;
  }

  @JsonProperty(PROP_USE_CWR)
  public void setUseCwr(boolean useCwr) {
    _useCwr = useCwr;
  }

  @JsonProperty(PROP_USE_ECE)
  public void setUseEce(boolean useEce) {
    _useEce = useEce;
  }

  @JsonProperty(PROP_USE_FIN)
  public void setUseFin(boolean useFin) {
    _useFin = useFin;
  }

  @JsonProperty(PROP_USE_PSH)
  public void setUsePsh(boolean usePsh) {
    _usePsh = usePsh;
  }

  @JsonProperty(PROP_USE_RST)
  public void setUseRst(boolean useRst) {
    _useRst = useRst;
  }

  @JsonProperty(PROP_USE_SYN)
  public void setUseSyn(boolean useSyn) {
    _useSyn = useSyn;
  }

  @JsonProperty(PROP_USE_URG)
  public void setUseUrg(boolean useUrg) {
    _useUrg = useUrg;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(toString(_cwr, _useCwr));
    sb.append(toString(_ece, _useEce));
    sb.append(toString(_urg, _useUrg));
    sb.append(toString(_ack, _useAck));
    sb.append(toString(_psh, _usePsh));
    sb.append(toString(_rst, _useRst));
    sb.append(toString(_syn, _useSyn));
    sb.append(toString(_fin, _useFin));
    return sb.toString();
  }

  private static String toString(boolean bit, boolean useBit) {
    if (useBit) {
      if (bit) {
        return "1";
      } else {
        return "0";
      }
    } else {
      return "x";
    }
  }
}
