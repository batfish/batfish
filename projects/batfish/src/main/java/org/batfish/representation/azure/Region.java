package org.batfish.representation.azure;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.common.Warning;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;

public class Region implements Serializable {

  private final @Nonnull Map<String, NetworkInterface> _interfaces = new HashMap<>();
  private final @Nonnull Map<String, IPConfiguration> _ipConfigurations = new HashMap<>();
  private final @Nonnull Map<String, VNet> _vnets = new HashMap<>();
  private final @Nonnull Map<String, Subnet> _subnets = new HashMap<>();
  private final @Nonnull Map<String, Instance> _instances = new HashMap<>();
  private final @Nonnull Map<String, NetworkSecurityGroup> _networkSecurityGroups = new HashMap<>();
  private final @Nonnull Map<String, NatGateway> _natGateways = new HashMap<>();
  private final @Nonnull Map<String, PublicIpAddress> _publicIpAddresses = new HashMap<>();
  private final @Nonnull Map<Class<? extends Resource>, Map<String, ? extends Resource>> _resourcesMap = new HashMap();
  private final @Nonnull String _regionName;

  public Region(String regionName) {
    _regionName = regionName;
    resourceFinderInitializer();
  }

  private void resourceFinderInitializer(){
      _resourcesMap.put(NetworkInterface.class, _interfaces);
      _resourcesMap.put(IPConfiguration.class, _ipConfigurations);
      _resourcesMap.put(VNet.class, _vnets);
      _resourcesMap.put(Subnet.class, _subnets);
      _resourcesMap.put(Instance.class, _instances);
      _resourcesMap.put(NetworkSecurityGroup.class, _networkSecurityGroups);
      _resourcesMap.put(NatGateway.class, _natGateways);
      _resourcesMap.put(PublicIpAddress.class, _publicIpAddresses);
  }

  public <T extends Resource> T findResource(IdReference idReference, Class<T> resourceClass){
      return findResource(idReference.getId(), resourceClass);
  }

  public @Nonnull <T extends Resource> T findResource(String id, Class<T> resourceClass){
      Map<String, ? extends Resource> wantedMap = _resourcesMap.get(resourceClass);
      if(wantedMap == null) {
          throw new IllegalArgumentException("Resource class not supported: " + resourceClass.getName());
      }

      Resource resource = wantedMap.get(id);
      if(resource == null){
          throw new BatfishException(
                  "Unable to find \""
                          + id
                          + "\" resource ("
                          + resourceClass.getName()
                          +"), did you include it ?"
          );
      }

      return resourceClass.cast(resource);
  }

  public @Nonnull Map<String, NetworkInterface> getInterfaces() {
    return _interfaces;
  }

  public @Nonnull Map<String, IPConfiguration> getIpConfigurations() {
    return _ipConfigurations;
  }

  public @Nonnull Map<String, VNet> getVnets() {
    return _vnets;
  }

  public @Nonnull Map<String, Subnet> getSubnets() {
    return _subnets;
  }

  public @Nonnull Map<String, Instance> getInstances() {
    return _instances;
  }

  public @Nonnull Map<String, NetworkSecurityGroup> getNetworkSecurityGroups() {
    return _networkSecurityGroups;
  }

  public @Nonnull Map<String, NatGateway> getNatGateways() {
    return _natGateways;
  }

  public @Nonnull Map<String, PublicIpAddress> getPublicIpAddresses() {
    return _publicIpAddresses;
  }

  public @Nullable String getName(){
      return _regionName;
  }

  public void addConfigElement(
      JsonNode node, String filename, ParseVendorConfigurationAnswerElement pvcae) {
    if (!node.has(AzureEntities.JSON_KEY_TYPE)) {
      pvcae.addRedFlagWarning(
          BfConsts.RELPATH_AZURE_CONFIGS_DIR,
          new Warning(
              String.format(
                  "Missing required key \"%s\" in file \"%s\"",
                  AzureEntities.JSON_KEY_TYPE, filename),
              "Azure"));
      return; // do not parse because it's unlikely an azure resource file
    }

    String type = node.get(AzureEntities.JSON_KEY_TYPE).textValue();

    switch (type) {
      case AzureEntities.JSON_TYPE_VM:
        Instance vm = BatfishObjectMapper.mapper().convertValue(node, VirtualMachine.class);
        _instances.put(vm.getId(), vm);
        break;
      case AzureEntities.JSON_TYPE_VNET:
        VNet network = BatfishObjectMapper.mapper().convertValue(node, VNet.class);
        _vnets.put(network.getId(), network);
        for (Subnet subnet : network.getProperties().getSubnets()) {
          _subnets.put(subnet.getId(), subnet);
        }
        break;
      case AzureEntities.JSON_TYPE_INTERFACE:
        NetworkInterface networkInterface =
            BatfishObjectMapper.mapper().convertValue(node, NetworkInterface.class);
        _interfaces.put(networkInterface.getId(), networkInterface);
        for (IPConfiguration ipConfiguration :
            networkInterface.getProperties().getIPConfigurations()) {
          // broken azure features which stores ipConfigurationName uppercased in id
          _ipConfigurations.put(ipConfiguration.getId().toLowerCase(), ipConfiguration);
        }
        break;
      case AzureEntities.JSON_TYPE_NETWORK_SECURITY_GROUP:
        NetworkSecurityGroup nsg =
            BatfishObjectMapper.mapper().convertValue(node, NetworkSecurityGroup.class);
        _networkSecurityGroups.put(nsg.getId(), nsg);
        break;
      case AzureEntities.JSON_TYPE_NAT_GATEWAY:
        NatGateway natGateway = BatfishObjectMapper.mapper().convertValue(node, NatGateway.class);
        _natGateways.put(natGateway.getId(), natGateway);
        break;
      case AzureEntities.JSON_TYPE_PUBLIC_IP:
        PublicIpAddress publicIp =
            BatfishObjectMapper.mapper().convertValue(node, PublicIpAddress.class);
        _publicIpAddresses.put(publicIp.getId(), publicIp);
        break;
      case AzureEntities.JSON_TYPE_POSTGRES:
        Instance db = BatfishObjectMapper.mapper().convertValue(node, Postgres.class);
        _instances.put(db.getId(), db);
        break;
      case AzureEntities.JSON_TYPE_CONTAINER_GROUP:
        Instance containerGroup =
            BatfishObjectMapper.mapper().convertValue(node, ContainerGroup.class);
        _instances.put(containerGroup.getId(), containerGroup);
        break;
      default:
        pvcae.addUnimplementedWarning(
            BfConsts.RELPATH_AZURE_CONFIGS_DIR,
            new Warning(
                String.format("Unknown type \"%s\" in file \"%s\"", type, filename), "AZURE"));
        return;
    }
  }

  public void toConfigurationNode(ConvertedConfiguration convertedConfiguration) {

    for (NatGateway natGateway : _natGateways.values()) {
      Configuration cfgNode = natGateway.toConfigurationNode(this, convertedConfiguration);
      convertedConfiguration.addNode(cfgNode);
    }

    for (Subnet subnet : _subnets.values()) {
      Configuration cfgNode = subnet.toConfigurationNode(this, convertedConfiguration);
      convertedConfiguration.addNode(cfgNode);
    }

    for (Instance instance : _instances.values()) {
      Configuration cfgNode = instance.toConfigurationNode(this, convertedConfiguration);
      convertedConfiguration.addNode(cfgNode);
    }

    // VNet interacts with subnet nodes so subnets must be generated before
    for (VNet vnet : _vnets.values()) {
      Configuration cfgNode = vnet.toConfigurationNode(this, convertedConfiguration);
      convertedConfiguration.addNode(cfgNode);
    }
  }
}
