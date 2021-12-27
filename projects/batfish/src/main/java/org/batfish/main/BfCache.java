package org.batfish.main;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import org.apache.commons.collections4.map.LRUMap;
import org.batfish.common.NetworkSnapshot;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;
import org.batfish.vendor.VendorConfiguration;

/** Internal caches. */
public final class BfCache {
  public static final Cache<NetworkSnapshot, DataPlane> CACHED_DATA_PLANES = buildDataPlaneCache();
  public static final Map<NetworkSnapshot, SortedMap<String, BgpAdvertisementsByVrf>>
      CACHED_ENVIRONMENT_BGP_TABLES = buildEnvironmentBgpTablesCache();
  public static final Cache<NetworkSnapshot, SortedMap<String, Configuration>> CACHED_TESTRIGS =
      buildTestrigCache();
  public static final Cache<NetworkSnapshot, Map<String, VendorConfiguration>>
      CACHED_VENDOR_CONFIGURATIONS = buildVendorConfigurationCache();

  private static final int MAX_CACHED_DATA_PLANES = 2;

  private static final int MAX_CACHED_ENVIRONMENT_BGP_TABLES = 4;

  private static final int MAX_CACHED_TESTRIGS = 5;

  private static final int MAX_CACHED_VENDOR_CONFIGURATIONS = 2;

  private BfCache() {}

  static Cache<NetworkSnapshot, DataPlane> buildDataPlaneCache() {
    return CacheBuilder.newBuilder().softValues().maximumSize(MAX_CACHED_DATA_PLANES).build();
  }

  static Map<NetworkSnapshot, SortedMap<String, BgpAdvertisementsByVrf>>
      buildEnvironmentBgpTablesCache() {
    return Collections.synchronizedMap(new LRUMap<>(MAX_CACHED_ENVIRONMENT_BGP_TABLES));
  }

  static Cache<NetworkSnapshot, SortedMap<String, Configuration>> buildTestrigCache() {
    return CacheBuilder.newBuilder().softValues().maximumSize(MAX_CACHED_TESTRIGS).build();
  }

  static Cache<NetworkSnapshot, Map<String, VendorConfiguration>> buildVendorConfigurationCache() {
    return CacheBuilder.newBuilder()
        .softValues()
        .maximumSize(MAX_CACHED_VENDOR_CONFIGURATIONS)
        .build();
  }
}
