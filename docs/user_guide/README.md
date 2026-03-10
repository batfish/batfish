# Batfish User Guide

This section provides information for users of Batfish, focusing on practical usage rather than development.

## Getting Started

If you are new to Batfish, we recommend starting with the [official getting started guide](https://pybatfish.readthedocs.io/en/latest/getting_started.html).

## What Batfish Can Do

Batfish supports a wide range of network validation tasks:

### Configuration Compliance

- Verify that network configurations adhere to organizational policies
- Check for security vulnerabilities in configurations
- Ensure consistent configuration across devices
- Validate against regulatory requirements

### Correctness Checking

- Verify that the network will behave as intended
- Check for reachability between hosts
- Validate routing policies
- Ensure redundancy and failover mechanisms work correctly

### Change Impact Analysis

- Predict the impact of configuration changes before deployment
- Compare different versions of configurations
- Identify unintended consequences of changes
- Verify that changes achieve their intended purpose

### Troubleshooting

- Diagnose connectivity issues
- Identify routing problems
- Analyze ACL behavior
- Debug protocol-specific issues

## Key Concepts

### Snapshots

A snapshot is a point-in-time collection of network configuration files and other data that Batfish analyzes. Snapshots can represent:

- The current state of your network
- A planned future state
- A historical state for comparison

### Questions

Questions are the primary way to interact with Batfish. Each question performs a specific type of analysis on a snapshot. Questions can be:

- Run individually via the Pybatfish API
- Combined into workflows
- Customized with parameters

### Dataflow

Batfish analyzes how packets flow through your network by:

1. Building a model of the network from configurations
2. Computing the data plane (forwarding tables)
3. Analyzing packet flow through the network

## Common Workflows

### Validating Configuration Changes

1. Create a snapshot of the current network
2. Create a snapshot with proposed changes
3. Run differential questions to compare behavior
4. Identify any unintended consequences

### Troubleshooting Connectivity Issues

1. Create a snapshot of the network
2. Use traceroute questions to analyze packet flow
3. Identify where packets are dropped or misrouted
4. Analyze device configurations at problem points

### Verifying Security Policies

1. Create a snapshot of the network
2. Use reachability questions to verify isolation
3. Analyze ACL behavior with searchFilters questions
4. Verify that security zones are properly configured

## Best Practices

### Snapshot Management

- Use consistent naming conventions for snapshots
- Include metadata with snapshots (e.g., date, purpose)
- Retain important snapshots for historical comparison
- Automate snapshot creation as part of your workflow

### Question Selection

- Start with high-level questions before diving into details
- Use differential questions when comparing snapshots
- Combine questions into workflows for comprehensive analysis
- Customize question parameters to focus on areas of interest

### Integration with Workflows

- Integrate Batfish into CI/CD pipelines
- Automate regular compliance checks
- Include Batfish validation in change management processes
- Use Batfish for pre- and post-change verification

## Supported Devices

Batfish supports a wide range of network devices, including:

- Cisco IOS, IOS-XE, IOS-XR, NX-OS, ASA
- Juniper JunOS
- Palo Alto Networks PAN-OS
- Arista EOS
- F5 BIG-IP
- And many others

For a complete and up-to-date list of supported devices, please refer to the [official documentation](https://pybatfish.readthedocs.io).

## Additional Resources

- [Pybatfish Documentation](https://pybatfish.readthedocs.io)
- [Batfish GitHub Repository](https://github.com/batfish/batfish)
- [Example Notebooks](https://github.com/batfish/pybatfish/tree/master/jupyter_notebooks)
- [Community Support](https://join.slack.com/t/batfish-org/shared_invite/enQtMzA0Nzg2OTAzNzQ1LTcyYzY3M2Q0NWUyYTRhYjdlM2IzYzRhZGU1NWFlNGU2MzlhNDY3OTJmMDIyMjQzYmRlNjhkMTRjNWIwNTUwNTQ)
