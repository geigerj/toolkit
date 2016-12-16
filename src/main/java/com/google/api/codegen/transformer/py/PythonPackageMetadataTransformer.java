package com.google.api.codegen.transformer.py;

import java.util.List;

import javax.annotation.Nullable;

import com.google.api.codegen.PackageMetadataConfig;
import com.google.api.codegen.config.ApiConfig;
import com.google.api.codegen.transformer.ModelToViewTransformer;
import com.google.api.codegen.viewmodel.PackageMetadataView;
import com.google.api.codegen.viewmodel.ViewModel;
import com.google.api.tools.framework.model.Model;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class PythonPackageMetadataTransformer implements ModelToViewTransformer {

  // TODO: Retrieve the following values from static file
  // Github issue: https://github.com/googleapis/toolkit/issues/848
  private static final String PACKAGE_VERSION = "0.7.1";
  private static final String GAX_VERSION = "0.15.0";
  private static final String PROTO_VERSION = "1.0.2";
  private static final String PACKAGE_URL = "https://github.com/googleapis/googleapis";
  PackageMetadataConfig packageConfig;

  public PythonPackageMetadataTransformer(PackageMetadataConfig packageConfig) {
    this.packageConfig = packageConfig;
  }

  @Override
  public List<ViewModel> transform(Model model, ApiConfig apiConfig) {
    return Lists.transform(
        getTemplateFileNames(),
        new Function<String, ViewModel>() {
          @Override
          @Nullable
          public ViewModel apply(@Nullable String templateFileName) {
            return generateMetadataView(model, apiConfig, templateFileName);
          }
        });
  }

  @Override
  public List<String> getTemplateFileNames() {
    return Lists.newArrayList(
        "LICENSE.snip",
        "py/MANIFEST.in.snip",
        "py/PUBLISHING.rst.snip",
        // "py/setup.py.snip",
        // "py/requirements.txt.snip",
        "py/README.rst.snip");
  }

  private ViewModel generateMetadataView(Model model, ApiConfig apiConfig, String template) {
    int extensionIndex = template.lastIndexOf(".");
    String outputPath = template.substring(0, extensionIndex);

    return PackageMetadataView.newBuilder()
        .templateFileName(template)
        .outputPath(outputPath)
        .identifier(apiConfig.getDomainLayerLocation())
        .version(packageConfig.majorVersion())
        .gaxVersion(GAX_VERSION)
        .protoVersion(PROTO_VERSION)
        .protoPath(PACKAGE_URL)
        .shortName("foo")
        .packageName("bar")
        .majorVersion("baz")
        .fullName(model.getServiceConfig().getTitle())
        .serviceName(apiConfig.getPackageName())
        .build();
  }
}
