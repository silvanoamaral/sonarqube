/*
 * SonarQube
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.application.command;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class EsJvmOptions extends JvmOptions<EsJvmOptions> {
  private static final String ELASTICSEARCH_JVM_OPTIONS_HEADER = "# This file has been automatically generated by SonarQube during startup.\n" +
    "# Please use sonar.search.javaOpts and/or sonar.search.javaAdditionalOpts in sonar.properties to specify jvm options for Elasticsearch\n" +
    "\n" +
    "# DO NOT EDIT THIS FILE\n" +
    "\n";

  public EsJvmOptions() {
    super(mandatoryOptions());
  }

  private static Map<String, String> mandatoryOptions() {
    Map<String, String> res = new LinkedHashMap<>(16);
    res.put("-XX:+UseConcMarkSweepGC", "");
    res.put("-XX:CMSInitiatingOccupancyFraction=", "75");
    res.put("-XX:+UseCMSInitiatingOccupancyOnly", "");
    res.put("-XX:+AlwaysPreTouch", "");
    res.put("-server", "");
    res.put("-Xss", "1m");
    res.put("-Djava.awt.headless=", "true");
    res.put("-Dfile.encoding=", "UTF-8");
    res.put("-Djna.nosys=", "true");
    res.put("-Djdk.io.permissionsUseCanonicalPath=", "true");
    res.put("-Dio.netty.noUnsafe=", "true");
    res.put("-Dio.netty.noKeySetOptimization=", "true");
    res.put("-Dio.netty.recycler.maxCapacityPerThread=", "0");
    res.put("-Dlog4j.shutdownHookEnabled=", "false");
    res.put("-Dlog4j2.disable.jmx=", "true");
    res.put("-Dlog4j.skipJansi=", "true");
    return res;
  }

  public void writeToJvmOptionFile(File file) {
    String jvmOptions = getAll().stream().collect(Collectors.joining("\n"));
    String jvmOptionsContent = ELASTICSEARCH_JVM_OPTIONS_HEADER + jvmOptions;
    try {
      Files.write(file.toPath(), jvmOptionsContent.getBytes(Charset.forName("UTF-8")));
    } catch (IOException e) {
      throw new IllegalStateException("Cannot write Elasticsearch jvm options file", e);
    }
  }
}