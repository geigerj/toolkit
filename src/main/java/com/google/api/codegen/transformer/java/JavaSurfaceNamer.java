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
package com.google.api.codegen.transformer.java;

import com.google.api.codegen.MethodConfig;
import com.google.api.codegen.ServiceMessages;
import com.google.api.codegen.transformer.ModelTypeFormatterImpl;
import com.google.api.codegen.transformer.ModelTypeTable;
import com.google.api.codegen.transformer.SurfaceNamer;
import com.google.api.codegen.util.java.JavaNameFormatter;
import com.google.api.codegen.util.java.JavaRenderingUtil;
import com.google.api.codegen.util.java.JavaTypeTable;
import com.google.api.tools.framework.model.Method;
import com.google.api.tools.framework.model.TypeRef;

import java.util.Arrays;
import java.util.List;

/**
 * The SurfaceNamer for Java.
 */
public class JavaSurfaceNamer extends SurfaceNamer {

  public JavaSurfaceNamer() {
    super(
        new JavaNameFormatter(),
        new ModelTypeFormatterImpl(new JavaModelTypeNameConverter()),
        new JavaTypeTable());
  }

  @Override
  public List<String> getDocLines(String text) {
    return JavaRenderingUtil.getDocLines(text);
  }

  @Override
  public List<String> getThrowsDocLines() {
    return Arrays.asList("@throws com.google.api.gax.grpc.ApiException if the remote call fails");
  }

  @Override
  public void addPageStreamingDescriptorImports(ModelTypeTable typeTable) {
    typeTable.saveNicknameFor("com.google.api.gax.grpc.PageStreamingDescriptor");
  }

  @Override
  public void addBundlingDescriptorImports(ModelTypeTable typeTable) {
    typeTable.saveNicknameFor("com.google.api.gax.grpc.BundlingDescriptor");
    typeTable.saveNicknameFor("com.google.api.gax.grpc.RequestIssuer");
    typeTable.saveNicknameFor("java.util.ArrayList");
    typeTable.saveNicknameFor("java.util.Collection");
  }

  @Override
  public void addPageStreamingCallSettingsImports(ModelTypeTable typeTable) {
    typeTable.saveNicknameFor("com.google.api.gax.grpc.PageStreamingCallSettings");
  }

  @Override
  public void addBundlingCallSettingsImports(ModelTypeTable typeTable) {
    typeTable.saveNicknameFor("com.google.api.gax.grpc.BundlingCallSettings");
    typeTable.saveNicknameFor("com.google.api.gax.grpc.BundlingSettings");
  }

  @Override
  public String getStaticLangReturnTypeName(Method method, MethodConfig methodConfig) {
    if (ServiceMessages.s_isEmptyType(method.getOutputType())) {
      return "void";
    }
    return getModelTypeFormatter().getFullNameFor(method.getOutputType());
  }

  @Override
  public String getGenericAwareResponseTypeName(TypeRef outputType) {
    if (ServiceMessages.s_isEmptyType(outputType)) {
      return "Void";
    } else {
      return getModelTypeFormatter().getFullNameFor(outputType);
    }
  }

  @Override
  public String getAndSavePagedResponseTypeName(ModelTypeTable typeTable, TypeRef resourceType) {
    String resourceTypeName = typeTable.getFullNameForElementType(resourceType);
    return typeTable.getAndSaveNicknameForContainer(
        "com.google.api.gax.core.PageAccessor", resourceTypeName);
  }
}
