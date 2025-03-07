/*
 * Copyright (c) 2000, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package management;

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.options.Timeout;
import com.oracle.bedrock.runtime.coherence.CoherenceCluster;
import com.oracle.bedrock.runtime.coherence.CoherenceClusterMember;
import com.oracle.bedrock.runtime.coherence.ServiceStatus;
import com.oracle.bedrock.runtime.coherence.callables.GetServiceStatus;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.java.features.JmxFeature;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.runtime.java.profiles.JmxProfile;
import com.oracle.bedrock.runtime.options.StabilityPredicate;
import com.oracle.bedrock.testsupport.deferred.Eventually;
import com.tangosol.internal.net.management.HttpHelper;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.oracle.bedrock.deferred.DeferredHelper.within;
import static org.hamcrest.CoreMatchers.is;

/**
 * Test Management over REST when multiple Coherence clusters run in the same JVM.
 * <p>
 * This is similar to environments like Web Servers or WLS where a single JVM may
 * contain more than one Coherence cluster, isolated by class loader.
 */
//@Ignore
public class MultiClusterInSingleProcessTests
        extends BaseManagementInfoResourceTests
    {
    public MultiClusterInSingleProcessTests()
        {
        super(CLUSTER_NAME, MultiClusterInSingleProcessTests::invokeInCluster);
        }

    @BeforeClass
    public static void _startup()
        {
        String sClusterNames = CLUSTER_NAME + ",Foo";
        startTestCluster(MultiCluster.class,
                         CLUSTER_NAME,
                         MultiClusterInSingleProcessTests::assertMultiClusterReady,
                         MultiClusterInSingleProcessTests::invokeInCluster,
                         MultiClusterInSingleProcessTests::configureOptions,
                         SystemProperty.of(MultiCluster.PROP_CLUSTER_NAMES, sClusterNames),
                         StabilityPredicate.of(assembly -> true));
        }

    // ----- Overridden tests -----------------------------------------------

    @Test
    public void testJmxJfr()
        {
        // skipped
        }

    // ----- helper methods -------------------------------------------------

    private static OptionsByType configureOptions(OptionsByType options)
        {
        options.remove(JmxProfile.class);
        options.add(SystemProperty.of(JmxFeature.SUN_MANAGEMENT_JMXREMOTE));
        options.add(SystemProperty.of(JmxFeature.SUN_MANAGEMENT_JMXREMOTE_PORT, 0));
        options.add(SystemProperty.of(JmxFeature.SUN_MANAGEMENT_JMXREMOTE_AUTHENTICATE, false));
        options.add(SystemProperty.of(JmxFeature.SUN_MANAGEMENT_JMXREMOTE_SSL, false));
        return options;
        }

    /**
     * There are multiple clusters running in the remote process, so this
     * method wil invoke the remote runnable in the correct cluster ClassLoader
     * in the remote process.
     *
     * @param sCluster  the name of the cluster in the remote process
     * @param callable  the {@link RemoteCallable} to execute
     */
    protected static void invokeInCluster(String sCluster, RemoteCallable<Void> callable)
        {
        CoherenceClusterMember member = s_cluster.getAny();
        MultiCluster.invokeInCluster(member, sCluster, callable);
        }

    /**
     * Assert that the remote {@link CoherenceCluster} is ready.
     *
     * @param cluster  the cluster to check
     */
    private static void assertMultiClusterReady(CoherenceCluster cluster)
        {
        MultiCluster.assertClusterStarted(cluster, CLUSTER_NAME);
        MultiCluster.assertClusterStarted(cluster, "Foo");

        CoherenceClusterMember member = cluster.getAny();
        Eventually.assertDeferred(() -> MultiCluster.invokeInCluster(member, CLUSTER_NAME, new GetServiceStatus(HttpHelper.getServiceName())), is(ServiceStatus.RUNNING), Timeout.of(5, TimeUnit.MINUTES));
        Eventually.assertDeferred(() -> MultiCluster.invokeInCluster(member, CLUSTER_NAME, new GetServiceStatus(SERVICE_NAME)), is(ServiceStatus.NODE_SAFE), Timeout.of(5, TimeUnit.MINUTES));
        Eventually.assertDeferred(() -> MultiCluster.invokeInCluster(member, CLUSTER_NAME, new GetServiceStatus(ACTIVE_SERVICE)), is(ServiceStatus.NODE_SAFE), Timeout.of(5, TimeUnit.MINUTES));
        Eventually.assertDeferred(() -> MultiCluster.invokeInCluster(member, CLUSTER_NAME, new CalculateUnbalanced("dist-persistence-test")), is(0), within(5, TimeUnit.MINUTES));
        }
    }
