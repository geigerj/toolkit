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
package com.google.api.codegen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.api.codegen.config.ApiConfig;
import com.google.api.codegen.gapic.GapicGeneratorConfig;
import com.google.api.codegen.gapic.GapicProvider;
import com.google.api.codegen.gapic.GapicProviderFactory;
import com.google.api.codegen.util.ClassInstantiator;
import com.google.api.tools.framework.model.Diag;
import com.google.api.tools.framework.model.Model;
import com.google.api.tools.framework.model.SimpleLocation;
import com.google.api.tools.framework.model.stages.Merged;
import com.google.api.tools.framework.snippet.Doc;
import com.google.api.tools.framework.tools.ToolDriverBase;
import com.google.api.tools.framework.tools.ToolOptions;
import com.google.api.tools.framework.tools.ToolOptions.Option;
import com.google.api.tools.framework.tools.ToolUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.TypeLiteral;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;

/** Main class for the code generator. */
public class CodeGeneratorApi extends ToolDriverBase {
  public static final Option<String> OUTPUT_FILE =
      ToolOptions.createOption(
          String.class,
          "output_file",
          "The name of the output file or folder to put generated code.",
          "");

  public static final Option<List<String>> GENERATOR_CONFIG_FILES =
      ToolOptions.createOption(
          new TypeLiteral<List<String>>() {},
          "config_files",
          "The list of YAML configuration files for the code generator.",
          ImmutableList.<String>of());

  public static final Option<String> PACKAGE_CONFIG_FILE =
      ToolOptions.createOption(
          String.class,
          "package_config",
          "The package metadata configuration.",
          "");
  
  public static final Option<List<String>> ENABLED_ARTIFACTS =
      ToolOptions.createOption(
          new TypeLiteral<List<String>>() {},
          "enabled_artifacts",
          "The artifacts to be generated by the code generator.",
          ImmutableList.<String>of());

  /** Constructs a code generator api based on given options. */
  public CodeGeneratorApi(ToolOptions options) {
    super(options);
  }

  @Override
  public ExtensionRegistry getPlatformExtensions() {
    ExtensionRegistry extensionRegistry = super.getPlatformExtensions();
    return extensionRegistry;
  }

  @Override
  protected void process() throws Exception {

    // Read the YAML config and convert it to proto.
    List<String> configFileNames = options.get(GENERATOR_CONFIG_FILES);
    if (configFileNames.size() == 0) {
      error(String.format("--%s must be provided", GENERATOR_CONFIG_FILES.name()));
      return;
    }

    ConfigProto configProto = loadConfigFromFiles(configFileNames);
    if (configProto == null) {
      return;
    }

    model.establishStage(Merged.KEY);
    if (model.getDiagCollector().getErrorCount() > 0) {
      for (Diag diag : model.getDiagCollector().getDiags()) {
        System.err.println(diag.toString());
      }
      return;
    }

    PackageMetadataConfig packageConfig = null;
    if (!Strings.isNullOrEmpty(options.get(PACKAGE_CONFIG_FILE))) {
      packageConfig = PackageMetadataConfig.createFromFile(Paths.get(options.get(PACKAGE_CONFIG_FILE)));
    }
    GeneratorProto generator = configProto.getGenerator();
    ApiConfig apiConfig = ApiConfig.createApiConfig(model, configProto);
    
    if (apiConfig == null) {
      return;
    }
    if (generator != null) {
      String factory = generator.getFactory();
      String id = generator.getId();

      GapicProviderFactory<GapicProvider<? extends Object>> providerFactory =
          createProviderFactory(model, factory);
      GapicGeneratorConfig generatorConfig =
          GapicGeneratorConfig.newBuilder()
              .id(id)
              .enabledArtifacts(options.get(ENABLED_ARTIFACTS))
              .build();

      List<GapicProvider<? extends Object>> providers =
          providerFactory.create(model, apiConfig, generatorConfig, packageConfig);
      String outputFile = options.get(OUTPUT_FILE);
      Map<String, Doc> outputFiles = Maps.newHashMap();
      for (GapicProvider<? extends Object> provider : providers) {
        outputFiles.putAll(provider.generate());
      }
      writeCodeGenOutput(outputFiles, outputFile);
    }
  }

  @VisibleForTesting
  public static void writeCodeGenOutput(Map<String, Doc> outputFiles, String outputFile)
      throws IOException {
    // TODO: Support zip output.
    if (outputFile.endsWith(".jar")) {
      ToolUtil.writeJar(outputFiles, outputFile);
    } else {
      ToolUtil.writeFiles(outputFiles, outputFile);
    }
  }

  private static GapicProviderFactory<GapicProvider<? extends Object>> createProviderFactory(
      final Model model, String factory) {
    @SuppressWarnings("unchecked")
    GapicProviderFactory<GapicProvider<? extends Object>> provider =
        ClassInstantiator.createClass(
            factory,
            GapicProviderFactory.class,
            new Class<?>[] {},
            new Object[] {},
            "generator",
            new ClassInstantiator.ErrorReporter() {
              @Override
              public void error(String message, Object... args) {
                model
                    .getDiagCollector()
                    .addDiag(Diag.error(SimpleLocation.TOPLEVEL, message, args));
              }
            });
    return provider;
  }

  private ConfigProto loadConfigFromFiles(List<String> configFileNames) {
    List<File> configFiles = pathsToFiles(configFileNames);
    if (model.getDiagCollector().getErrorCount() > 0) {
      return null;
    }
    ImmutableMap<String, Message> supportedConfigTypes =
        ImmutableMap.<String, Message>of(
            ConfigProto.getDescriptor().getFullName(), ConfigProto.getDefaultInstance());
    ConfigProto configProto =
        (ConfigProto)
            MultiYamlReader.read(model.getDiagCollector(), configFiles, supportedConfigTypes);
    return configProto;
  }

  private List<File> pathsToFiles(List<String> configFileNames) {
    List<File> files = new ArrayList<>();

    for (String configFileName : configFileNames) {
      File file = model.findDataFile(configFileName);
      if (file == null) {
        error("Cannot find configuration file '%s'.", configFileName);
        continue;
      }
      files.add(file);
    }

    return files;
  }

  private void error(String message, Object... args) {
    model.getDiagCollector().addDiag(Diag.error(SimpleLocation.TOPLEVEL, message, args));
  }
}
