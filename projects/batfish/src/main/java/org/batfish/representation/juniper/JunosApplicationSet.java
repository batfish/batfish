package org.batfish.representation.juniper;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public enum JunosApplicationSet {
  JUNOS_CIFS,
  JUNOS_MGCP,
  JUNOS_MS_RPC,
  JUNOS_MS_RPC_ANY,
  JUNOS_MS_RPC_IIS_COM,
  JUNOS_MS_RPC_MSEXCHANGE,
  JUNOS_MS_RPC_WMIC,
  JUNOS_ROUTING_INBOUND,
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

  private ApplicationSet init() {
    ApplicationSet applicationSet = new ApplicationSet(name());

    List<JunosApplication> applications;

    switch (this) {
      case JUNOS_CIFS:
        {
          applications =
              Arrays.asList(
                  JunosApplication.JUNOS_NETBIOS_SESSION, JunosApplication.JUNOS_SMB_SESSION);
          break;
        }

      case JUNOS_MGCP:
        {
          applications =
              Arrays.asList(JunosApplication.JUNOS_MGCP_CA, JunosApplication.JUNOS_MGCP_UA);
          break;
        }

      case JUNOS_MS_RPC:
        {
          applications =
              Arrays.asList(JunosApplication.JUNOS_MS_RPC_TCP, JunosApplication.JUNOS_MS_RPC_UDP);
          break;
        }

      case JUNOS_MS_RPC_ANY:
        {
          applications =
              Arrays.asList(
                  JunosApplication.JUNOS_MS_RPC_TCP,
                  JunosApplication.JUNOS_MS_RPC_UDP,
                  JunosApplication.JUNOS_MS_RPC_UUID_ANY_TCP,
                  JunosApplication.JUNOS_MS_RPC_UUID_ANY_UDP);
          break;
        }

      case JUNOS_MS_RPC_IIS_COM:
        {
          applications =
              Arrays.asList(
                  JunosApplication.JUNOS_MS_RPC_TCP,
                  JunosApplication.JUNOS_MS_RPC_IIS_COM_1,
                  JunosApplication.JUNOS_MS_RPC_IIS_COM_ADMINBASE);
          break;
        }

      case JUNOS_MS_RPC_MSEXCHANGE:
        {
          applications =
              Arrays.asList(
                  JunosApplication.JUNOS_MS_RPC_TCP,
                  JunosApplication.JUNOS_MS_RPC_UDP,
                  JunosApplication.JUNOS_MS_RPC_EPM,
                  JunosApplication.JUNOS_MS_RPC_MSEXCHANGE_DIRECTORY_RFR,
                  JunosApplication.JUNOS_MS_RPC_MSEXCHANGE_INFO_STORE,
                  JunosApplication.JUNOS_MS_RPC_MSEXCHANGE_DIRECTORY_NSP);
          break;
        }

      case JUNOS_MS_RPC_WMIC:
        {
          applications =
              Arrays.asList(
                  JunosApplication.JUNOS_MS_RPC_TCP,
                  JunosApplication.JUNOS_MS_RPC_WMIC_ADMIN,
                  JunosApplication.JUNOS_MS_RPC_WMIC_ADMIN2,
                  JunosApplication.JUNOS_MS_RPC_WMIC_WEBM_LEVEL1LOGIN,
                  JunosApplication.JUNOS_MS_RPC_WMIC_MGMT);
          break;
        }

      case JUNOS_ROUTING_INBOUND:
        {
          applications =
              Arrays.asList(
                  JunosApplication.JUNOS_BGP,
                  JunosApplication.JUNOS_RIP,
                  JunosApplication.JUNOS_LDP_TCP,
                  JunosApplication.JUNOS_LDP_UDP);
          break;
        }

      case JUNOS_SUN_RPC:
        {
          applications =
              Arrays.asList(JunosApplication.JUNOS_SUN_RPC_TCP, JunosApplication.JUNOS_SUN_RPC_UDP);
          break;
        }

      case JUNOS_SUN_RPC_ANY:
        {
          applications =
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_ANY_TCP,
                  JunosApplication.JUNOS_SUN_RPC_ANY_UDP);
          break;
        }

      case JUNOS_SUN_RPC_MOUNTD:
        {
          applications =
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_MOUNTD_TCP,
                  JunosApplication.JUNOS_SUN_RPC_MOUNTD_UDP);
          break;
        }

      case JUNOS_SUN_RPC_NFS:
        {
          applications =
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_NFS_TCP,
                  JunosApplication.JUNOS_SUN_RPC_NFS_UDP);
          break;
        }

      case JUNOS_SUN_RPC_NFS_ACCESS:
        {
          applications =
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_NFS_TCP,
                  JunosApplication.JUNOS_SUN_RPC_NFS_UDP,
                  JunosApplication.JUNOS_SUN_RPC_MOUNTD_TCP,
                  JunosApplication.JUNOS_SUN_RPC_MOUNTD_UDP);
          break;
        }

      case JUNOS_SUN_RPC_NLOCKMGR:
        {
          applications =
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_NLOCKMGR_TCP,
                  JunosApplication.JUNOS_SUN_RPC_NLOCKMGR_UDP);
          break;
        }

      case JUNOS_SUN_RPC_PORTMAP:
        {
          applications =
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP);
          break;
        }

      case JUNOS_SUN_RPC_RQUOTAD:
        {
          applications =
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_RQUOTAD_TCP,
                  JunosApplication.JUNOS_SUN_RPC_RQUOTAD_UDP);
          break;
        }

      case JUNOS_SUN_RPC_RUSERD:
        {
          applications =
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_RUSERD_TCP,
                  JunosApplication.JUNOS_SUN_RPC_RUSERD_UDP);
          break;
        }

      case JUNOS_SUN_RPC_SADMIND:
        {
          applications =
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_SADMIND_TCP,
                  JunosApplication.JUNOS_SUN_RPC_SADMIND_UDP);
          break;
        }

      case JUNOS_SUN_RPC_SPRAYD:
        {
          applications =
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_SPRAYD_TCP,
                  JunosApplication.JUNOS_SUN_RPC_SPRAYD_UDP);
          break;
        }

      case JUNOS_SUN_RPC_STATUS:
        {
          applications =
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_STATUS_TCP,
                  JunosApplication.JUNOS_SUN_RPC_STATUS_UDP);
          break;
        }

      case JUNOS_SUN_RPC_WALLD:
        {
          applications =
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_WALLD_TCP,
                  JunosApplication.JUNOS_SUN_RPC_WALLD_UDP);
          break;
        }

      case JUNOS_SUN_RPC_YPBIND:
        {
          applications =
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_YPBIND_TCP,
                  JunosApplication.JUNOS_SUN_RPC_YPBIND_UDP);
          break;
        }

      case JUNOS_SUN_RPC_YPSERV:
        {
          applications =
              Arrays.asList(
                  JunosApplication.JUNOS_SUN_RPC_TCP,
                  JunosApplication.JUNOS_SUN_RPC_UDP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP,
                  JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP,
                  JunosApplication.JUNOS_SUN_RPC_YPSERV_TCP,
                  JunosApplication.JUNOS_SUN_RPC_YPSERV_UDP);
          break;
        }

      default:
        return null;
    }

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
}
