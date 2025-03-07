/*
 * Copyright (c) 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */

package override;

import com.oracle.coherence.common.base.Logger;

import com.tangosol.net.events.EventInterceptor;
import com.tangosol.net.events.application.LifecycleEvent;

/**
 * A test class that implements {@link EventInterceptor}.
 */
public class BaseInterceptor
        implements EventInterceptor<LifecycleEvent>
    {
    // ----- EventInterceptor interface -------------------------------------

    @Override
    public void onEvent(LifecycleEvent event)
        {
        Logger.info(String.format("BaseInterceptor received event with type: %s", event.getType()));
        }
    }
