package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Vendor-independent model for Cisco Telemetry. */
public final class Telemetry implements Serializable {

  private static final String PROP_SUBSCRIPTIONS = "subscriptions";

  private @Nonnull SortedMap<Integer, Subscription> _subscriptions;

  public Telemetry() {
    _subscriptions = new TreeMap<>();
  }

  @JsonProperty(PROP_SUBSCRIPTIONS)
  public @Nonnull SortedMap<Integer, Subscription> getSubscriptions() {
    return _subscriptions;
  }

  @JsonProperty(PROP_SUBSCRIPTIONS)
  public void setSubscriptions(@Nonnull SortedMap<Integer, Subscription> subscriptions) {
    _subscriptions = subscriptions;
  }

  /** A telemetry subscription. */
  public static final class Subscription implements Serializable {

    private static final String PROP_ENCODING = "encoding";
    private static final String PROP_FILTER = "filter";
    private static final String PROP_FILTER_TYPE = "filterType";
    private static final String PROP_FILTER_VALUE = "filterValue";
    private static final String PROP_SOURCE_ADDRESS = "sourceAddress";
    private static final String PROP_SOURCE_VRF = "sourceVrf";
    private static final String PROP_STREAM = "stream";
    private static final String PROP_UPDATE_POLICY = "updatePolicy";
    private static final String PROP_RECEIVERS = "receivers";

    private @Nullable String _encoding;
    private @Nullable String _filter;
    private @Nullable String _filterType;
    private @Nullable String _filterValue;
    private @Nullable Ip _sourceAddress;
    private @Nullable String _sourceVrf;
    private @Nullable String _stream;
    private @Nullable String _updatePolicy;
    private @Nonnull List<Receiver> _receivers;

    public Subscription() {
      _receivers = new ArrayList<>();
    }

    @JsonProperty(PROP_ENCODING)
    public @Nullable String getEncoding() {
      return _encoding;
    }

    @JsonProperty(PROP_ENCODING)
    public void setEncoding(@Nullable String encoding) {
      _encoding = encoding;
    }

    @JsonProperty(PROP_FILTER)
    public @Nullable String getFilter() {
      return _filter;
    }

    @JsonProperty(PROP_FILTER)
    public void setFilter(@Nullable String filter) {
      _filter = filter;
    }

    @JsonProperty(PROP_FILTER_TYPE)
    public @Nullable String getFilterType() {
      return _filterType;
    }

    @JsonProperty(PROP_FILTER_TYPE)
    public void setFilterType(@Nullable String filterType) {
      _filterType = filterType;
    }

    @JsonProperty(PROP_FILTER_VALUE)
    public @Nullable String getFilterValue() {
      return _filterValue;
    }

    @JsonProperty(PROP_FILTER_VALUE)
    public void setFilterValue(@Nullable String filterValue) {
      _filterValue = filterValue;
    }

    @JsonProperty(PROP_SOURCE_ADDRESS)
    public @Nullable Ip getSourceAddress() {
      return _sourceAddress;
    }

    @JsonProperty(PROP_SOURCE_ADDRESS)
    public void setSourceAddress(@Nullable Ip sourceAddress) {
      _sourceAddress = sourceAddress;
    }

    @JsonProperty(PROP_SOURCE_VRF)
    public @Nullable String getSourceVrf() {
      return _sourceVrf;
    }

    @JsonProperty(PROP_SOURCE_VRF)
    public void setSourceVrf(@Nullable String sourceVrf) {
      _sourceVrf = sourceVrf;
    }

    @JsonProperty(PROP_STREAM)
    public @Nullable String getStream() {
      return _stream;
    }

    @JsonProperty(PROP_STREAM)
    public void setStream(@Nullable String stream) {
      _stream = stream;
    }

    @JsonProperty(PROP_UPDATE_POLICY)
    public @Nullable String getUpdatePolicy() {
      return _updatePolicy;
    }

    @JsonProperty(PROP_UPDATE_POLICY)
    public void setUpdatePolicy(@Nullable String updatePolicy) {
      _updatePolicy = updatePolicy;
    }

    @JsonProperty(PROP_RECEIVERS)
    public @Nonnull List<Receiver> getReceivers() {
      return _receivers;
    }

    @JsonProperty(PROP_RECEIVERS)
    public void setReceivers(@Nonnull List<Receiver> receivers) {
      _receivers = receivers;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Subscription)) {
        return false;
      }
      Subscription that = (Subscription) o;
      return Objects.equals(_encoding, that._encoding)
          && Objects.equals(_filter, that._filter)
          && Objects.equals(_filterType, that._filterType)
          && Objects.equals(_filterValue, that._filterValue)
          && Objects.equals(_sourceAddress, that._sourceAddress)
          && Objects.equals(_sourceVrf, that._sourceVrf)
          && Objects.equals(_stream, that._stream)
          && Objects.equals(_updatePolicy, that._updatePolicy)
          && Objects.equals(_receivers, that._receivers);
    }

    @Override
    public int hashCode() {
      return Objects.hash(
          _encoding,
          _filter,
          _filterType,
          _filterValue,
          _sourceAddress,
          _sourceVrf,
          _stream,
          _updatePolicy,
          _receivers);
    }
  }

  /** A telemetry receiver. */
  public static final class Receiver implements Serializable {

    private static final String PROP_NAME = "name";
    private static final String PROP_HOST = "host";
    private static final String PROP_PORT = "port";
    private static final String PROP_PROTOCOL = "protocol";
    private static final String PROP_RECEIVER_TYPE = "receiverType";

    private @Nonnull String _name;
    private @Nullable String _host;
    private @Nullable Integer _port;
    private @Nullable String _protocol;
    private @Nullable String _receiverType;

    public Receiver(@Nonnull String name) {
      _name = name;
    }

    // JSON constructor
    private Receiver() {
      _name = "";
    }

    @JsonProperty(PROP_NAME)
    public @Nonnull String getName() {
      return _name;
    }

    @JsonProperty(PROP_NAME)
    public void setName(@Nonnull String name) {
      _name = name;
    }

    @JsonProperty(PROP_HOST)
    public @Nullable String getHost() {
      return _host;
    }

    @JsonProperty(PROP_HOST)
    public void setHost(@Nullable String host) {
      _host = host;
    }

    @JsonProperty(PROP_PORT)
    public @Nullable Integer getPort() {
      return _port;
    }

    @JsonProperty(PROP_PORT)
    public void setPort(@Nullable Integer port) {
      _port = port;
    }

    @JsonProperty(PROP_PROTOCOL)
    public @Nullable String getProtocol() {
      return _protocol;
    }

    @JsonProperty(PROP_PROTOCOL)
    public void setProtocol(@Nullable String protocol) {
      _protocol = protocol;
    }

    @JsonProperty(PROP_RECEIVER_TYPE)
    public @Nullable String getReceiverType() {
      return _receiverType;
    }

    @JsonProperty(PROP_RECEIVER_TYPE)
    public void setReceiverType(@Nullable String receiverType) {
      _receiverType = receiverType;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Receiver)) {
        return false;
      }
      Receiver receiver = (Receiver) o;
      return _name.equals(receiver._name)
          && Objects.equals(_host, receiver._host)
          && Objects.equals(_port, receiver._port)
          && Objects.equals(_protocol, receiver._protocol)
          && Objects.equals(_receiverType, receiver._receiverType);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_name, _host, _port, _protocol, _receiverType);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Telemetry)) {
      return false;
    }
    Telemetry telemetry = (Telemetry) o;
    return Objects.equals(_subscriptions, telemetry._subscriptions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_subscriptions);
  }
}
