# Active Development

This section provides information about the current state of Batfish development and known issues.

## Active development

As of May 2025, the most active area of development by the Batfish core contributors is in enhancing the symbolic policy analysis for Juniper and other operating systems. Recently, open source contributors have supplied improvements to AWS support and proposed support for modeling Azure cloud networks.

## Known Issues

This document tracks current known issues and limitations in Batfish.

### Known Limitations

1. **IPv6**

   - **Description**: Batfish has very limited support for IPv6, limited primarily to vendor-specific extraction of some concepts.
   - **Workaround**: None.
   - **Future Plans**: We want to improve IPv6 support, but so far have been unable to find dedicated funding for bringing IPv6 up to IPv4 standards. We would welcome contributions in this area!

2. **MPLS**

   - **Description**: Batfish has no support for MPLS at this time.
   - **Workaround**: In some networks, MPLS circuits can be mocked out by strategically adding L1 edges or by making small BGP configuration changes.
   - **Future Plans**: No planned develpoment at this time.

3. **IS-IS, RIP**
   - **Description**: Batfish has limited support for IS-IS and RIP at this time.
   - **Workaround**: These protocols are not heavily used in networks where dedicated Batfish development has brought them to parity.
   - **Future Plans**: No planned develpoment at this time.

### Reporting New Issues

Please file [bug reports](https://github.com/batfish/batfish/issues/new?template=bug_report.md) or other issues on [GitHub](https://github.com/batfish/batfish/issues/new/choose). Bug reports are most likely to be actioned if full reproducible instructions are shared using the provided bug report template.
