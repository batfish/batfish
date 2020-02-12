package org.batfish.datamodel;

/**
 * A specific vendor and hardware (maybe virtual) type. Examples include Juniper SRX340, AWS EC2
 * Instance.
 *
 * @see DeviceType for the generic classification of a device.
 */
public enum DeviceModel {
  AWS_VPC,
  AWS_EC2_INSTANCE,
  BATFISH_INTERNET,
  BATFISH_ISP,
}
