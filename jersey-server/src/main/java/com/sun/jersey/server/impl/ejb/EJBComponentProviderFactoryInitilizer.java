/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.jersey.server.impl.ejb;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.server.impl.InitialContextHelper;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class EJBComponentProviderFactoryInitilizer {
    private static final Logger LOGGER = Logger.getLogger(
            EJBComponentProviderFactoryInitilizer.class.getName());

    public static void initialize(ResourceConfig rc) {
        try {
            InitialContext ic = InitialContextHelper.getInitialContext();
            if (ic == null) {
                return;
            }
            Object interceptorBinder = ic.
                    lookup("java:org.glassfish.ejb.container.interceptor_binding_spi");
            // Some implementations of InitialContext return null instead of
            // throwing NamingException if there is no Object associated with
            // the name
            if (interceptorBinder == null) {
                LOGGER.config("The EJB interceptor binding API is not available. JAX-RS EJB support is disabled.");
                return;
            }
            
            Method interceptorBinderMethod = interceptorBinder.getClass().
                    getMethod("registerInterceptor", java.lang.Object.class);

            EJBInjectionInterceptor interceptor = new EJBInjectionInterceptor();

            try {
                interceptorBinderMethod.invoke(interceptorBinder, interceptor);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error when configuring to use the EJB interceptor binding API. JAX-RS EJB support is disabled.", ex);
                return;
            }

            rc.getSingletons().add(new EJBComponentProviderFactory(interceptor));
            rc.getClasses().add(EJBExceptionMapper.class);
        } catch (NamingException ex) {
            LOGGER.log(Level.CONFIG, "The EJB interceptor binding API is not available. JAX-RS EJB support is disabled.", ex);
        } catch (NoSuchMethodException ex) {
            LOGGER.log(Level.SEVERE, "The EJB interceptor binding API does not conform to what is expected. JAX-RS EJB support is disabled.", ex);
        } catch (SecurityException ex) {
            LOGGER.log(Level.SEVERE, "Security issue when configuring to use the EJB interceptor binding API. JAX-RS EJB support is disabled.", ex);
        } catch (LinkageError ex) {
            LOGGER.log(Level.SEVERE, "Linkage error when configuring to use the EJB interceptor binding API. JAX-RS EJB support is disabled.", ex);
        }
    }
}