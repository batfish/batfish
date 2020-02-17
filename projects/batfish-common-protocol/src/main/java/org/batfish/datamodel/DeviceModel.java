package org.batfish.datamodel;

/**
 * A specific vendor and hardware (maybe virtual) type. Examples include Juniper SRX340, AWS EC2
 * Instance.
 *
 * @see DeviceType for the generic classification of a device.
 */
public enum DeviceModel {
  AWS_EC2_INSTANCE,
  AWS_ELASTICSEARCH_DOMAIN,
  AWS_INTERNET_GATEWAY,
  AWS_NAT_GATEWAY,
  AWS_RDS_INSTANCE,
  AWS_SUBNET,
  AWS_TRANSIT_GATEWAY,
  AWS_VPC,
  AWS_VPN_GATEWAY,
  BATFISH_INTERNET,
  BATFISH_ISP,
}
