/*
 * Copyright (c) 2016-2017 by OpenText Corporation. All Rights Reserved.
 */
package com.opentext.ia.sdk.server.configuration.yaml;

import java.util.Collection;
import java.util.Map;

import com.opentext.ia.sdk.support.yaml.Visit;


public abstract class PropertyVisitor extends PathVisitor {

  private final Map<String, Collection<String>> propertiesByPathRegex;

  public PropertyVisitor(Map<String, Collection<String>> propertiesByPathRegex) {
    super(propertiesByPathRegex.keySet());
    this.propertiesByPathRegex = propertiesByPathRegex;
  }

  @Override
  public void accept(Visit visit) {
    pathRegexesMatching(visit)
        .flatMap(regex -> propertiesByPathRegex.get(regex).stream())
        .forEach(property -> visitProperty(visit, property));
  }

  protected abstract void visitProperty(Visit visit, String property);

}
