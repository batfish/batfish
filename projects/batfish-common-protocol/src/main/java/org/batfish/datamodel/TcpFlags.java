package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;

/** Collection of TCP flags. */
public final class TcpFlags implements Serializable, Comparable<TcpFlags> {
  /** Some legacy code requires TCP Flags to be present for non-TCP flows. */
  public static final TcpFlags FALSE =
      new TcpFlags(false, false, false, false, false, false, false, false);

  private static final String PROP_ACK = "ack";
  private static final String PROP_CWR = "cwr";
  private static final String PROP_ECE = "ece";
  private static final String PROP_FIN = "fin";
  private static final String PROP_PSH = "psh";
  private static final String PROP_RST = "rst";
  private static final String PROP_SYN = "syn";
  private static final String PROP_URG = "urg";

  private final boolean _ack;
  private final boolean _cwr;
  private final boolean _ece;
  private final boolean _fin;
  private final boolean _psh;
  private final boolean _rst;
  private final boolean _syn;
  private final boolean _urg;

  @JsonCreator
  public TcpFlags(
      @JsonProperty(PROP_ACK) boolean ack,
      @JsonProperty(PROP_CWR) boolean cwr,
      @JsonProperty(PROP_ECE) boolean ece,
      @JsonProperty(PROP_FIN) boolean fin,
      @JsonProperty(PROP_PSH) boolean psh,
      @JsonProperty(PROP_RST) boolean rst,
      @JsonProperty(PROP_SYN) boolean syn,
      @JsonProperty(PROP_URG) boolean urg) {
    _ack = ack;
    _cwr = cwr;
    _ece = ece;
    _fin = fin;
    _psh = psh;
    _rst = rst;
    _syn = syn;
    _urg = urg;
  }

  @Override
  public int compareTo(@Nonnull TcpFlags o) {
    return Comparator.comparing(TcpFlags::getAck)
        .thenComparing(TcpFlags::getCwr)
        .thenComparing(TcpFlags::getEce)
        .thenComparing(TcpFlags::getFin)
        .thenComparing(TcpFlags::getPsh)
        .thenComparing(TcpFlags::getRst)
        .thenComparing(TcpFlags::getUrg)
        .thenComparing(TcpFlags::getSyn)
        .compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TcpFlags)) {
      return false;
    }
    TcpFlags tcpFlags = (TcpFlags) o;
    return _ack == tcpFlags._ack
        && _cwr == tcpFlags._cwr
        && _ece == tcpFlags._ece
        && _fin == tcpFlags._fin
        && _psh == tcpFlags._psh
        && _rst == tcpFlags._rst
        && _syn == tcpFlags._syn
        && _urg == tcpFlags._urg;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ack, _cwr, _ece, _fin, _psh, _rst, _syn, _urg);
  }

  @JsonProperty(PROP_ACK)
  public boolean getAck() {
    return _ack;
  }

  @JsonProperty(PROP_CWR)
  public boolean getCwr() {
    return _cwr;
  }

  @JsonProperty(PROP_ECE)
  public boolean getEce() {
    return _ece;
  }

  @JsonProperty(PROP_FIN)
  public boolean getFin() {
    return _fin;
  }

  @JsonProperty(PROP_PSH)
  public boolean getPsh() {
    return _psh;
  }

  @JsonProperty(PROP_RST)
  public boolean getRst() {
    return _rst;
  }

  @JsonProperty(PROP_SYN)
  public boolean getSyn() {
    return _syn;
  }

  @JsonProperty(PROP_URG)
  public boolean getUrg() {
    return _urg;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Builder for {@link TcpFlags} */
  public static final class Builder {
    private boolean _ack;
    private boolean _cwr;
    private boolean _ece;
    private boolean _fin;
    private boolean _psh;
    private boolean _rst;
    private boolean _syn;
    private boolean _urg;

    private Builder() {}

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

    public TcpFlags build() {
      return new TcpFlags(_ack, _cwr, _ece, _fin, _psh, _rst, _syn, _urg);
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_ack", _ack)
        .add("_cwr", _cwr)
        .add("_ece", _ece)
        .add("_fin", _fin)
        .add("_psh", _psh)
        .add("_rst", _rst)
        .add("_syn", _syn)
        .add("_urg", _urg)
        .toString();
  }
}
