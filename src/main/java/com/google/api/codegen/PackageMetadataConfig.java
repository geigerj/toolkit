package com.google.api.codegen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;

@AutoValue
public abstract class PackageMetadataConfig {

  /** The version of GAX that this package depends on */
  public abstract String gaxVersion();

  /** The version of the protocol buffer package that this package depends on */
  public abstract String protoVersion();

  /** The full name of the API, including branding. E.g., "Stackdriver Logging". */
  public abstract String fullName();

  /** A single-word short name of the API. E.g., "logging". */
  public abstract String shortName();

  /** The base name of the client library package. E.g., "google-cloud-logging-v1". */
  public abstract String packageName();

  /** The major version of the API, as used in the package name. E.g., "v1". */
  public abstract String majorVersion();

  /** The path to the API protos in the googleapis repo. */
  public abstract String protoPath();

  private static Builder newBuilder() {
    return new AutoValue_PackageMetadataConfig.Builder();
  }

  @AutoValue.Builder
  protected abstract static class Builder {
    abstract Builder gaxVersion(String val);

    abstract Builder protoVersion(String val);

    abstract Builder fullName(String val);

    abstract Builder shortName(String val);

    abstract Builder packageName(String val);

    abstract Builder majorVersion(String val);

    abstract Builder protoPath(String val);

    abstract PackageMetadataConfig build();
  }

  /** Creates an PackageMetadataConfig with no content. Exposed for testing. */
  @VisibleForTesting
  public static PackageMetadataConfig createDummyPackageMetadataConfig() {
    return newBuilder().gaxVersion("").protoVersion("").fullName("").shortName("").packageName("")
        .majorVersion("").protoPath("").build();
  }

  @SuppressWarnings("unchecked")
  public static PackageMetadataConfig createFromFile(Path packageConfigPath) throws IOException {
    Yaml yaml = new Yaml();
    String contents = new String(Files.readAllBytes(packageConfigPath), StandardCharsets.UTF_8);
    System.out.println("Creating from: " + contents);
    Map<String, String> configMap = (Map<String, String>) yaml.load(contents);
    return newBuilder()
        .gaxVersion(configMap.get("gax_version"))
        .protoVersion(configMap.get("proto_version"))
        .fullName(configMap.get("full_name"))
        .shortName(configMap.get("short_name"))
        .packageName(configMap.get("package_name"))
        .majorVersion(configMap.get("major_version"))
        .protoPath(configMap.get("proto_path"))
        .build();
  }
}
