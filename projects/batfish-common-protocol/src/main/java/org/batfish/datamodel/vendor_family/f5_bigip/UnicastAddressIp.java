package org.batfish.datamodel.vendor_family.f5_bigip;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

/** A concrete or reference IP assigned to a {@link Device} within a {@link UnicastAddress}. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public interface UnicastAddressIp extends Serializable {}
