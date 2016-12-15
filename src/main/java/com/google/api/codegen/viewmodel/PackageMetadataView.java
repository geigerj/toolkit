/* Copyright 2016 Google Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.api.codegen.viewmodel;

import com.google.api.codegen.SnippetSetRunner;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PackageMetadataView implements ViewModel {

  @Override
  public String resourceRoot() {
    return SnippetSetRunner.SNIPPET_RESOURCE_ROOT;
  }

  @Override
  public abstract String templateFileName();

  @Override
  public abstract String outputPath();

  public abstract String identifier();

  public abstract String version();

  public abstract String gaxVersion();

  public abstract String protoVersion();

  public abstract String serviceName();

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

  public static Builder newBuilder() {
    return new AutoValue_PackageMetadataView.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder outputPath(String val);

    public abstract Builder templateFileName(String val);

    public abstract Builder identifier(String val);

    public abstract Builder version(String val);

    public abstract Builder gaxVersion(String val);

    public abstract Builder protoVersion(String val);

    public abstract Builder serviceName(String val);

    /** The full name of the API, including branding. E.g., "Stackdriver Logging". */
    public abstract Builder fullName(String val);

    /** A single-word short name of the API. E.g., "logging". */
    public abstract Builder shortName(String val);

    /** The base name of the client library package. E.g., "google-cloud-logging-v1". */
    public abstract Builder packageName(String val);

    /** The major version of the API, as used in the package name. E.g., "v1". */
    public abstract Builder majorVersion(String val);

    /** The path to the API protos in the googleapis repo. */
    public abstract Builder protoPath(String val);

    public abstract PackageMetadataView build();
  }
}
