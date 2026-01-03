package org.batfish.datamodel;

/**
 * A generic classification of device function. See
 * https://pybatfish.readthedocs.io/en/latest/specifiers.html#node-specifier for more information.
 *
 * @see DeviceModel for the specific vendor name and model of a device (e.g., Juniper SRX340).
 */
public enum DeviceType {
  HOST,
  INTERNET,
  ISP,
  ROUTER,
  SWITCH
}
