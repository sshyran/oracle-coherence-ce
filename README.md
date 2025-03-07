<!--

  Copyright (c) 2000, 2020, Oracle and/or its affiliates.

  Licensed under the Universal Permissive License v 1.0 as shown at
  http://oss.oracle.com/licenses/upl.

-->

-----
<img src=https://oracle.github.io/coherence/assets/images/logo-red.png><img>

[![CI Build](https://github.com/oracle/coherence/workflows/CI%20Build/badge.svg?branch=master)](https://github.com/oracle/coherence/actions?query=workflow%3A%22CI+Build%22+branch%3Amaster)
[![Maven Central](https://img.shields.io/maven-central/v/com.oracle.coherence.ce/coherence.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.oracle.coherence.ce%22)
[![License](http://img.shields.io/badge/license-UPL%201.0-blue.svg)](https://oss.oracle.com/licenses/upl/)
[![Docker Pulls](https://img.shields.io/docker/pulls/oraclecoherence/coherence-ce)](https://hub.docker.com/r/oraclecoherence/coherence-ce)

# Oracle Coherence Community Edition

## Contents
1. [Introduction](#intro)
1. [How to Get Coherence Community Edition](#acquire)
1. [Coherence Overview](#overview)
1. [Hello Coherence](#get-started)
  1. [CohQL Console](#cohql)
  1. [Coherence Console](#coh-console)
  1. [Code Example](#hello-coh)
1. [Building](#build)
1. [Integrations](#integrations)
1. [Documentation](#documentation)
1. [Contributing](#contrib)

## <a name="intro"></a>Introduction

[Coherence](http://coherence.community/) is a scalable, fault-tolerant, cloud-ready,
distributed platform for building grid-based applications and reliably storing data.
The product is used at scale, for both compute and raw storage, in a vast array of
industries such as critical financial trading systems, high performance telecommunication
products and eCommerce applications.

Typically these deployments do not tolerate any downtime and Coherence is chosen due to its
novel features in death detection, application data evolvability, and the robust,
battle-hardened core of the product that enables it to be seamlessly deployed and
adapted within any ecosystem.

At a high level, Coherence provides an implementation of the familiar `Map<K,V>`
interface but rather than storing the associated data in the local process it is partitioned
(or sharded) across a number of designated remote nodes. This partitioning enables
applications to not only distribute (and therefore scale) their storage across multiple
processes, machines, racks, and data centers but also to perform grid-based processing
to truly harness the CPU resources of the machines.

The Coherence interface `NamedMap<K,V>` (an extension of `Map<K,V>`) provides methods
to query, aggregate (map/reduce style) and compute (send functions to storage nodes
for locally executed mutations) the data set. These capabilities, in addition to
numerous other features, enable Coherence to be used as a framework for writing robust,
distributed applications.

## <a name="acquire"></a>Downloading Coherence Community Edition

As Coherence is generally embedded into an application by using Coherence APIs,
the natural place to consume this dependency is from Maven:

```xml
<dependencies>
    <dependency>
        <groupId>com.oracle.coherence.ce</groupId>
        <artifactId>coherence</artifactId>
        <version>21.12</version>
    </dependency>
</dependencies>
```

You can also get Coherence from the official [Docker site](https://hub.docker.com/r/oraclecoherence/coherence-ce).
For other language clients, use [C++](https://github.com/oracle/coherence-cpp-extend-client) and
[.NET](https://github.com/oracle/coherence-dotnet-extend-client) and for the non-community
edition, see [Oracle Technology Network](https://www.oracle.com/middleware/technologies/coherence-downloads.html).

## <a name="overview"></a>Overview

First and foremost, Coherence provides a fundamental service that is responsible
for all facets of clustering and is a common denominator / building block for all
other Coherence services. This service, referred to as 'service 0' internally,
ensures that the mesh of members is maintained and responsive, taking action to collaboratively
evict, shun, or in some cases, voluntarily depart the cluster when deemed necessary.
As members join and leave the cluster, other Coherence services are notified,
thus enabling those services to react accordingly.

> Note: This part of the Coherence product has been in production for more that 10 years,
>       being the subject of some extensive and imaginative testing. While this feature has
>       been discussed here, it certainly is not something that customers, generally,
>       interact with directly, but is important to be aware of.

Coherence services build on top of the cluster service. The key implementations
to be aware of are PartitionedService, InvocationService, and ProxyService.

In the majority of cases, customers deal with maps. A map is represented
by an implementation of `NamedMap<K,V>`. A `NamedMap` is hosted by a service,
generally the PartitionedService, and is the entry point to store, retrieve,
aggregate, query, and stream data.

Coherence Maps provide a number of features:

* Fundamental **key-based access**: get/put getAll/putAll.
* Client-side and storage-side events:
  * **MapListeners** to asynchronously notify clients of changes to data.
  * **EventInterceptors** (either sync or async) to be notified storage level events, including
mutations, partition transfer, failover, and so on.
* **NearCaches** - Locally cached data based on previous requests with local content
invalidated upon changes in the storage tier.
* **ViewCaches** - Locally stored view of remote data that can be a subset based on a
predicate and is kept in sync, real time.
* **Queries** - Distributed, parallel query evaluation to return matching key, values,
or entries with potential to optimize performance with indices.
* **Aggregations** - A map/reduce style aggregation where data is aggregated in parallel
on all storage nodes, and results streamed back to the client for aggregation of
those results to produce a final result.
* **Data local processing** - Ability to send a function to the relevant storage node
to execute processing logic for the appropriate entries with exclusive access.
* **Partition local transactions** - Ability to perform scalable transactions by
associating data (thus being on the same partition) and manipulating other entries
on the same partition, potentially across different maps.
* **Non-blocking / async NamedMap API**
* **C++ and .NET clients** - Access the same NamedMap API from either C++ or .NET.
* **Portable Object Format** - Optimized serialization format, with the ability to
navigate the serialized form for optimized queries, aggregations, or data processing.
* **Integration with Databases** - Database and third party data integration with
CacheStores, including both synchronous or asynchronous writes.
* **CohQL** - Ansi-style query language with a console for adhoc queries.
* **Topics** - Distributed topics implementation that offers pub/sub messaging with
the storage capacity, the cluster, and parallelizable subscribers.

Coherence also provides a number of non-functional features:

* **Rock solid clustering** - Highly tuned and robust clustering stack that enables
Coherence to scale to thousands of members in a cluster with thousands of partitions
and terabytes of data being accessed, mutated, queried, and aggregated concurrently.
* **Safety first** - Resilient data management that ensures backup copies are
on distinct machines, racks, or sites, and the ability to maintain multiple backups.
* **24/7 Availability** - Zero downtime with rolling redeployment of cluster members
to upgrade application or product versions.
  * Backward and forward compatibility of product upgrades, including major versions.
* **Persistent Maps** - Ability to use local file system persistence (thus
avoid extra network hops) and leverage Coherence consensus protocols to perform
distributed disk recovery when appropriate.
* **Distributed State Snapshot** - Ability to perform distributed point-in-time
snapshot of cluster state, and recover snapshot in this or a different cluster
(leverages persistence feature).
* **Lossy redundancy** - Ability to reduce the redundancy guarantee by making backups
and/or persistence asynchronous from a client perspective.
* **Single Mangement View** - Provides insight into the cluster  with a single
JMX server that provides a view of all members of the cluster.
* **Management over REST** - All JMX data and operations can be performed over REST,
including cluster wide thread dumps and heapdumps.
* **Non-cluster Access** - Provides access to the cluster from the outside via proxies,
for distant (high latency) clients and for non-java languages such as C++ and .NET.
* **Kubernetes friendly** - Enables seamless and safe deployment of applications to k8s with
our own [operator](https://github.com/oracle/coherence-operator).

## <a name="get-started"></a>Hello Coherence

### Prerequisites

  1. Java - JDK 8 or higher
  2. Maven - 3.6.3 or higher

> **Note:** When _building_ Coherence, JDK 11 is required.

### CLI Hello Coherence

The following example illustrated starting a **storage enabled** Coherence Server,
followed by a **storage disabled** Coherence Console. Using the console, data is
inserted, retrieved, and then the console is terminated. The console is restarted
and data is once again retrieved to illustrate the permanence of the data.

> **Note:** This example uses the OOTB cache configuration and therefore, explicitly
>          specifying the console is storage disabled is unnecessary.

> **Note:** Coherence cluster members discover each other via one of two mechanisms;
>           multicast (default) or Well Known Addressing (deterministic broadcast).
>           If your system does not support mutlicast, enable WKA by specifying
>           `-Dcoherence.wka=localhost` for both processes started in the following
>           console examples.

#### <a name="cohql"></a>CohQL Console

```shell

$> mvn -DgroupId=com.oracle.coherence.ce -DartifactId=coherence -Dversion=21.12 dependency:get

$> export COH_JAR=~/.m2/repository/com/oracle/coherence/ce/coherence/21.12/coherence-21.12.jar

$> java -jar $COH_JAR &

$> java -cp $COH_JAR com.tangosol.coherence.dslquery.QueryPlus

CohQL> select * from welcomes

CohQL> insert into welcomes key 'english' value 'Hello'

CohQL> insert into welcomes key 'spanish' value 'Hola'

CohQL> insert into welcomes key 'french' value 'Bonjour'

CohQL> select key(), value() from welcomes
Results
["french", "Bonjour"]
["english", "Hello"]
["spanish", "Hola"]

CohQL> bye

$> java -cp $COH_JAR com.tangosol.coherence.dslquery.QueryPlus

CohQL> select key(), value() from welcomes
Results
["french", "Bonjour"]
["english", "Hello"]
["spanish", "Hola"]

CohQL> bye

$> kill %1
```
#### <a name="coh-console"></a>Coherence Console
```shell

$> mvn -DgroupId=com.oracle.coherence.ce -DartifactId=coherence -Dversion=21.12 dependency:get

$> export COH_JAR=~/.m2/repository/com/oracle/coherence/ce/coherence/21.12/coherence-21.12.jar

$> java -jar $COH_JAR &

$> java -cp $COH_JAR com.tangosol.net.CacheFactory

Map (?): cache welcomes

Map (welcomes): get english
null

Map (welcomes): put english Hello
null

Map (welcomes): put spanish Hola
null

Map (welcomes): put french Bonjour
null

Map (welcomes): get english
Hello

Map (welcomes): list
french = Bonjour
spanish = Hola
english = Hello

Map (welcomes): bye

$> java -cp $COH_JAR com.tangosol.net.CacheFactory

Map (?): cache welcomes

Map (welcomes): list
french = Bonjour
spanish = Hola
english = Hello

Map (welcomes): bye

$> kill %1
```

### <a name="hello-coh"></a>Programmatic Hello Coherence Example

The following example illustrates starting a **storage enabled** Coherence server,
followed by running the `HelloCoherence` application. The `HelloCoherence` application
inserts and retrieves data from the Coherence server.

#### Build `HelloCoherence`

1. Create a maven project either manually or by using an archetype such as maven-archetype-quickstart
2. Add a dependency to the pom file:
```xml
    <dependency>
        <groupId>com.oracle.coherence.ce</groupId>
        <artifactId>coherence</artifactId>
        <version>21.12</version>
    </dependency>
```
3. Copy and paste the following source to a file named src/main/java/HelloCoherence.java:
```java
    import com.tangosol.net.CacheFactory;
    import com.tangosol.net.NamedMap;

    public class HelloCoherence
        {
        // ----- static methods -------------------------------------------------

        public static void main(String[] asArgs)
            {
            NamedMap<String, String> map = CacheFactory.getCache("welcomes");

            System.out.printf("Accessing map \"%s\" containing %d entries\n",
                    map.getName(),
                    map.size());

            map.put("english", "Hello");
            map.put("spanish", "Hola");
            map.put("french" , "Bonjour");

            // list
            map.entrySet().forEach(System.out::println);
            }
        }
```
4. Compile the maven project:
```shell
    mvn package
```
5. Start a Storage Server
```shell
    mvn exec:java -Dexec.mainClass="com.tangosol.net.DefaultCacheServer" &
```
6. Run `HelloCoherence`
```shell
    mvn exec:java -Dexec.mainClass="HelloCoherence"
```
7. Confirm you see output including the following:
```shell
    Accessing map "welcomes" containing 3 entries
    ConverterEntry{Key="french", Value="Bonjour"}
    ConverterEntry{Key="spanish", Value="Hola"}
    ConverterEntry{Key="english", Value="Hello"}
```
8. Kill the storage server started previously:
```shell
    kill %1
```

## <a name="build"></a>Building

In order to **build** Coherence CE, please ensure that you have **JDK 11** installed.

> **Note:** When using Coherence merely as a dependency in a project, without intending
> to build the project from source code, then an installed JDK 8 version is sufficient.

```shell

$> git clone git@github.com:oracle/coherence.git
$> cd coherence/prj

# build all modules
$> mvn clean install

# build all modules skipping tests
$> mvn clean install -DskipTests

# build a specific module, including all dependent modules and run tests
$> mvn -am -pl test/functional/persistence clean verify

# only build coherence.jar without running tests
$> mvn -am -pl coherence clean install -DskipTests

# only build coherence.jar and skip compilation of CDBs and tests
$> mvn -am -pl coherence clean install -DskipTests -Dtde.compile.not.required

```

## <a name="integrations"></a>Integrations

## <a name="documentation"></a>Documentation

### Oracle Coherence Documentation

Oracle Coherence product documentation is available
[here](https://docs.oracle.com/en/middleware/standalone/coherence/14.1.1.0/index.html).

### Features Not Included in Coherence Community Edition

The following Oracle Coherence features are not included in Coherence Community Edition:

* Management of Coherence via the Oracle WebLogic Management Framework
* Deployment of Grid Archives (GARs)
* HTTP Session Management for Application Servers (Coherence*Web)
* GoldenGate HotCache
* TopLink-based CacheLoaders and CacheStores
* Elastic Data
* Federation and WAN (wide area network) Support
* Transaction Framework
* CommonJ Work Manager

Below is an overview of features supported in each Coherence edition for comparison purposes:

<img src=https://oracle.github.io/coherence/assets/images/coherence-edition-matrix.png><img>

Please refer to <a href="https://docs.oracle.com/en/middleware/fusion-middleware/fmwlc/application-server-products-new-structure.html#GUID-00982997-3110-4AC8-8729-80F1D904E62B">Oracle Fusion Middleware Licensing Documentation</a> for official documentation of Oracle Coherence commercial editions and licensing details.

## <a name="contrib"></a>Contribute

Interested in contributing?  See our contribution [guidelines](CONTRIBUTING.md) for details.
