#!/bin/sh
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

# Install the MQ Light API to the local Maven repository
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=mqlight-api-$version.jar -DpomFile=mqlight/pom.xml

# Install the MQ Light API samples to the local Maven repository
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=mqlight-api-samples-$version.jar -DpomFile=mqlight-samples/pom.xml

# Install the MQ Light project to the local Maven repository
# This is necessary in order for other Maven projects depending on MQ Light to be able to build in offline mode
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=pom.xml -DpomFile=pom.xml

# Install the required dependencies to the local Maven repository
mvn org.apache.maven.plugins:maven-dependency-plugin:2.10:get -Dartifact=com.ibm.mqlight:mqlight-api:$version -DrepoUrl=http://repo1.maven.org/maven2/