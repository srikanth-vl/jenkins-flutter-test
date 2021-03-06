package io.flutter.plugins;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.plugins.shim.ShimPluginRegistry;

/**
 * Generated file. Do not edit.
 * This file is generated by the Flutter tool based on the
 * plugins that support the Android platform.
 */
@Keep
public final class GeneratedPluginRegistrant {
  public static void registerWith(@NonNull FlutterEngine flutterEngine) {
    ShimPluginRegistry shimPluginRegistry = new ShimPluginRegistry(flutterEngine);
    flutterEngine.getPlugins().add(new io.flutter.plugins.camera.CameraPlugin());
    flutterEngine.getPlugins().add(new io.flutter.plugins.connectivity.ConnectivityPlugin());
    flutterEngine.getPlugins().add(new dev.flutter.plugins.e2e.E2EPlugin());
      de.esys.esysfluttershare.EsysFlutterSharePlugin.registerWith(shimPluginRegistry.registrarFor("de.esys.esysfluttershare.EsysFlutterSharePlugin"));
      com.hemanthraj.fluttercompass.FlutterCompassPlugin.registerWith(shimPluginRegistry.registrarFor("com.hemanthraj.fluttercompass.FlutterCompassPlugin"));
      com.baseflow.geolocator.GeolocatorPlugin.registerWith(shimPluginRegistry.registrarFor("com.baseflow.geolocator.GeolocatorPlugin"));
      com.baseflow.googleapiavailability.GoogleApiAvailabilityPlugin.registerWith(shimPluginRegistry.registrarFor("com.baseflow.googleapiavailability.GoogleApiAvailabilityPlugin"));
      com.lyokone.location.LocationPlugin.registerWith(shimPluginRegistry.registrarFor("com.lyokone.location.LocationPlugin"));
      com.baseflow.location_permissions.LocationPermissionsPlugin.registerWith(shimPluginRegistry.registrarFor("com.baseflow.location_permissions.LocationPermissionsPlugin"));
      io.flutter.plugins.pathprovider.PathProviderPlugin.registerWith(shimPluginRegistry.registrarFor("io.flutter.plugins.pathprovider.PathProviderPlugin"));
      com.baseflow.permissionhandler.PermissionHandlerPlugin.registerWith(shimPluginRegistry.registrarFor("com.baseflow.permissionhandler.PermissionHandlerPlugin"));
      flutter.plugins.screen.screen.ScreenPlugin.registerWith(shimPluginRegistry.registrarFor("flutter.plugins.screen.screen.ScreenPlugin"));
    flutterEngine.getPlugins().add(new io.flutter.plugins.sharedpreferences.SharedPreferencesPlugin());
      com.tekartik.sqflite.SqflitePlugin.registerWith(shimPluginRegistry.registrarFor("com.tekartik.sqflite.SqflitePlugin"));
    flutterEngine.getPlugins().add(new io.flutter.plugins.urllauncher.UrlLauncherPlugin());
    flutterEngine.getPlugins().add(new io.flutter.plugins.videoplayer.VideoPlayerPlugin());
  }
}
