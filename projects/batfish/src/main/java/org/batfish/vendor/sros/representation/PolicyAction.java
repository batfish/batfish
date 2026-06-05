package org.batfish.vendor.sros.representation;

/**
 * An SR-OS routing-policy {@code action-type} (used by both entry {@code action} and {@code
 * default-action}). {@code nokia-conf-policy-options.yang}.
 */
public enum PolicyAction {
  ACCEPT,
  REJECT,
  NEXT_ENTRY,
  NEXT_POLICY;
}
