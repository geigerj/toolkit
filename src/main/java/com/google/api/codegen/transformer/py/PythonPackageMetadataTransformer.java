package com.google.api.codegen.transformer.py;

import com.google.api.codegen.config.ApiConfig;
import com.google.api.codegen.config.PackageMetadataConfig;
import com.google.api.codegen.transformer.ModelToViewTransformer;
import com.google.api.codegen.viewmodel.PackageMetadataView;
import com.google.api.codegen.viewmodel.ViewModel;
import com.google.api.tools.framework.model.Model;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class PythonPackageMetadataTransformer implements ModelToViewTransformer {

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
        "py/setup.py.snip",
        "py/requirements.txt.snip",
        "py/README.rst.snip");
  }

  private ViewModel generateMetadataView(Model model, ApiConfig apiConfig, String template) {
    int extensionIndex = template.lastIndexOf(".");
    String outputPath = template.substring(0, extensionIndex);

    return PackageMetadataView.newBuilder()
        .templateFileName(template)
        .outputPath(outputPath)
        .identifier(apiConfig.getDomainLayerLocation())
        .packageVersion(packageConfig.packageVersion().get("python"))
        .protoPath(packageConfig.protoPath())
        .shortName(packageConfig.shortName())
        .gaxVersion(packageConfig.gaxVersion().get("python"))
        .protoVersion(packageConfig.protoVersion().get("python"))
        .commonProtosVersion(packageConfig.commonProtosVersion().get("python"))
        .packageName(packageConfig.packageName().get("python"))
        .majorVersion(packageConfig.apiVersion())
        .author(packageConfig.author())
        .email(packageConfig.email())
        .homepage(packageConfig.homepage())
        .license(packageConfig.license())
        .fullName(model.getServiceConfig().getTitle())
        .serviceName(apiConfig.getPackageName())
        .namespacePackages(
            computeNamespacePackages(apiConfig.getPackageName(), packageConfig.apiVersion()))
        .build();
  }

  private List<String> computeNamespacePackages(String packageName, String apiVersion) {
    ArrayList<String> packages = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean first = true;
    for (String pkg : Splitter.on(".").split(packageName)) {
      if (!first) {
        current.append("." + pkg);
      } else {
        current.append(pkg);
        first = false;
      }
      if (!pkg.equals(apiVersion)) {
        packages.add(current.toString());
      }
    }
    return packages;
  }
}
