package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class TelemetrySubscription implements Serializable {

  public static class Receiver implements Serializable {

    private final String _name;
    private String _protocol;
    private String _host;
    private String _receiverType;
    private int _port;

    public Receiver(String name) {
      _name = name;
    }

    public String getName() {
      return _name;
    }

    public @Nullable String getProtocol() {
      return _protocol;
    }

    public void setProtocol(@Nullable String protocol) {
      _protocol = protocol;
    }

    public @Nullable String getHost() {
      return _host;
    }

    public void setHost(@Nullable String host) {
      _host = host;
    }

    public @Nullable String getReceiverType() {
      return _receiverType;
    }

    public void setReceiverType(@Nullable String receiverType) {
      _receiverType = receiverType;
    }

    public int getPort() {
      return _port;
    }

    public void setPort(int port) {
      _port = port;
    }
  }

  private final int _id;
  private String _encoding;
  private String _filter;
  private String _filterType;
  private String _filterValue;
  private String _sourceAddress;
  private String _sourceVrf;
  private String _stream;
  private String _updatePolicy;
  private final List<Receiver> _receivers;

  public TelemetrySubscription(int id) {
    _id = id;
    _receivers = new ArrayList<>();
  }

  public int getId() {
    return _id;
  }

  public @Nullable String getEncoding() {
    return _encoding;
  }

  public void setEncoding(@Nullable String encoding) {
    _encoding = encoding;
  }

  public @Nullable String getFilter() {
    return _filter;
  }

  public void setFilter(@Nullable String filter) {
    _filter = filter;
  }

  public @Nullable String getFilterType() {
    return _filterType;
  }

  public void setFilterType(@Nullable String filterType) {
    _filterType = filterType;
  }

  public @Nullable String getFilterValue() {
    return _filterValue;
  }

  public void setFilterValue(@Nullable String filterValue) {
    _filterValue = filterValue;
  }

  public @Nullable String getSourceAddress() {
    return _sourceAddress;
  }

  public void setSourceAddress(@Nullable String sourceAddress) {
    _sourceAddress = sourceAddress;
  }

  public @Nullable String getSourceVrf() {
    return _sourceVrf;
  }

  public void setSourceVrf(@Nullable String sourceVrf) {
    _sourceVrf = sourceVrf;
  }

  public @Nullable String getStream() {
    return _stream;
  }

  public void setStream(@Nullable String stream) {
    _stream = stream;
  }

  public @Nullable String getUpdatePolicy() {
    return _updatePolicy;
  }

  public void setUpdatePolicy(@Nullable String updatePolicy) {
    _updatePolicy = updatePolicy;
  }

  public List<Receiver> getReceivers() {
    return _receivers;
  }

  public void addReceiver(Receiver receiver) {
    _receivers.add(receiver);
  }
}
