package org.batfish.representation.juniper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace.Builder;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;

public enum JunosApplicationSet implements ApplicationSetMember {
  JUNOS_CIFS,
  JUNOS_MGCP,
  JUNOS_MS_RPC,
  JUNOS_MS_RPC_ANY,
  JUNOS_MS_RPC_IIS_COM,
  JUNOS_MS_RPC_MSEXCHANGE,
  JUNOS_MS_RPC_WMIC,
  JUNOS_ROUTING_INBOUND,
  JUNOS_STUN,
  JUNOS_SUN_RPC,
  JUNOS_SUN_RPC_ANY,
  JUNOS_SUN_RPC_MOUNTD,
  JUNOS_SUN_RPC_NFS,
  JUNOS_SUN_RPC_NFS_ACCESS,
  JUNOS_SUN_RPC_NLOCKMGR,
  JUNOS_SUN_RPC_PORTMAP,
  JUNOS_SUN_RPC_RQUOTAD,
  JUNOS_SUN_RPC_RUSERD,
  JUNOS_SUN_RPC_SADMIND,
  JUNOS_SUN_RPC_SPRAYD,
  JUNOS_SUN_RPC_STATUS,
  JUNOS_SUN_RPC_WALLD,
  JUNOS_SUN_RPC_YPBIND,
  JUNOS_SUN_RPC_YPSERV;

  private final Supplier<ApplicationSet> _applicationSet;

  JunosApplicationSet() {
    _applicationSet = Suppliers.memoize(this::init);
  }

  public ApplicationSet getApplicationSet() {
    return _applicationSet.get();
  }

  private String convertToJuniperName() {
    return name().toLowerCase().replace("_", "-");
  }

  private ApplicationSet init() {
    ApplicationSet applicationSet = new ApplicationSet(convertToJuniperName(), true);

    List<JunosApplication> applications =
        switch (this) {
          case JUNOS_CIFS ->
              Arrays.asList(
                  JunosApplication.JUNOS_NETBIOS_SESSION, JunosApplication.JUNOS_SMB_SESSION);
          case JUNOS_MGCP ->
              Arrays.asList(JunosApplication.JUNOS_MGCP_CA, JunosApplication.JUNOS_MGCP_UA);
          case JUNOS_MS_RPC ->
              Arrays.asList(JunosApplication.JUNOS_MS_RPC_TCP, JunosApplication.JUNOS_MS_RPC_UDP);
          case JUNOS_MS_RPC_ANY ->
              Arrays.asList(
                  JunosApplication.JUNOS_MS_RPC_TCP,
                  JunosApplication.JUNOS_MS_RPC_UDP,
                  JunosApplication.JUNOS_MS_RPC_UUID_ANY_TCP,
                  JunosApplication.JUNOS_MS_RPC_UUID_ANY_UDP);
          case JUNOS_MS_RPC_IIS_COM ->
              Arrays.asList(
                  JunosApplication.JUNOS_MS_RPC_TCP,
                  JunosApplication.JUNOS_MS_RPC_IIS_COM_1,
                  JunosApplication.JUNOS_MS_RPC_IIS_COM_ADMINBASE);
          case JUNOS_MS_RPC_MSEXCHANGE ->
              Arrays.asList(
                  JunosApplication.JUNOS_MS_RPC_TCP,
                  JunosApplication.JUNOS_MS_RPC_UDP,
                  JunosApplication.JUNOS_MS_RPC_EPM,
                  JunosApplication.JUNOS_MS_RPC_MSEXCHANGE_DIRECTORY_RFR,
                  JunosApplication.JUNOS_MS_RPC_MSEXCHANGE_INFO_STORE,
                  JunosApplication.JUNOS_MS_RPC_MSEXCHANGE_DIRECTORY_NSP);
          case JUNOS_MS_RPC_WMIC ->
              Arrays.asList(
                  JunosApplication.JUNOS_MS_RPC_TCP,
                  JunosApplication.JUNOS_MS_RPC_WMIC_ADMIN,
                  JunosApplication.JUNOS_MS_RPC_WMIC_ADMIN2,
                  JunosApplication.JUNOS_MS_RPC_WMIC_WEBM_LEVEL1LOGIN,
                  JunosApplication.JUNOS_MS_RPC_WMIC_MGMT);
          case JUNOS_ROUTING_INBOUND ->
              Arrays.asList(
                  JunosApplication.JUNOS_BGP,
                  JunosApplication.JUNOS_RIP,
                  JunosApplication.JUNOS_LDP_TCP,
                  JunosApplication.JUNOS_LDP_UDP);
          case JUNOS_STUN ->
              Arrays.asList(JunosApplication.JUNOS_STUN_TCP, JunosApplication.JUNOS_STUN_UDP);
          case JUNOS_SUN_RPC ->
              Arrays.asList(JunosApplication.JUNOS_SUN_RPC_TCP, JunosApplication.JUNOS_SUN_RPC_UDP);
          case JUNOS_SUN_RPC_ANY ->
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_ANY_TCP,
                  JunosApplication.JUNOS_SUN_RPC_ANY_UDP);
          case JUNOS_SUN_RPC_MOUNTD ->
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_MOUNTD_TCP,
                  JunosApplication.JUNOS_SUN_RPC_MOUNTD_UDP);
          case JUNOS_SUN_RPC_NFS ->
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_NFS_TCP,
                  JunosApplication.JUNOS_SUN_RPC_NFS_UDP);
          case JUNOS_SUN_RPC_NFS_ACCESS ->
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_NFS_TCP,
                  JunosApplication.JUNOS_SUN_RPC_NFS_UDP,
                  JunosApplication.JUNOS_SUN_RPC_MOUNTD_TCP,
                  JunosApplication.JUNOS_SUN_RPC_MOUNTD_UDP);
          case JUNOS_SUN_RPC_NLOCKMGR ->
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_NLOCKMGR_TCP,
                  JunosApplication.JUNOS_SUN_RPC_NLOCKMGR_UDP);
          case JUNOS_SUN_RPC_PORTMAP ->
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP);
          case JUNOS_SUN_RPC_RQUOTAD ->
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_RQUOTAD_TCP,
                  JunosApplication.JUNOS_SUN_RPC_RQUOTAD_UDP);
          case JUNOS_SUN_RPC_RUSERD ->
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_RUSERD_TCP,
                  JunosApplication.JUNOS_SUN_RPC_RUSERD_UDP);
          case JUNOS_SUN_RPC_SADMIND ->
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_SADMIND_TCP,
                  JunosApplication.JUNOS_SUN_RPC_SADMIND_UDP);
          case JUNOS_SUN_RPC_SPRAYD ->
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_SPRAYD_TCP,
                  JunosApplication.JUNOS_SUN_RPC_SPRAYD_UDP);
          case JUNOS_SUN_RPC_STATUS ->
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_STATUS_TCP,
                  JunosApplication.JUNOS_SUN_RPC_STATUS_UDP);
          case JUNOS_SUN_RPC_WALLD ->
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_WALLD_TCP,
                  JunosApplication.JUNOS_SUN_RPC_WALLD_UDP);
          case JUNOS_SUN_RPC_YPBIND ->
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_YPBIND_TCP,
                  JunosApplication.JUNOS_SUN_RPC_YPBIND_UDP);
          case JUNOS_SUN_RPC_YPSERV ->
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_YPSERV_TCP,
                  JunosApplication.JUNOS_SUN_RPC_YPSERV_UDP);
        };

    ImmutableList.Builder<ApplicationSetMemberReference> applicationSetMemberBuilder =
        ImmutableList.builder();

    for (JunosApplication app : applications) {
      applicationSetMemberBuilder.add(new JunosApplicationReference(app));
    }

    applicationSet.setMembers(applicationSetMemberBuilder.build());
    return applicationSet;
  }

  public boolean hasDefinition() {
    return _applicationSet.get() != null;
  }

  @Override
  public void applyTo(
      JuniperConfiguration jc,
      Builder srcHeaderSpaceBuilder,
      LineAction action,
      List<? super ExprAclLine> lines,
      Warnings w) {
    throw new BatfishException("not implemented");
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Warnings w) {
    if (!hasDefinition()) {
      return new FalseExpr(getTraceElement(convertToJuniperName()));
    }
    return _applicationSet.get().toAclLineMatchExpr(jc, w);
  }

  @Override
  public boolean isBuiltIn() {
    return true;
  }

  @VisibleForTesting
  public static TraceElement getTraceElement(String name) {
    return TraceElement.of(String.format("Matched built-in application-set %s", name));
  }
}
