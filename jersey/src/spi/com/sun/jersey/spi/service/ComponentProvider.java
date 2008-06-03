/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.jersey.spi.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A provider for the instantiation and management of components.
 * <p>
 * The runtime will defer to a registered component provider (if present)
 * for every component (application-defined or infrastructure-defined) that
 * needs to be instantiated. If the component provider does
 * not support the requested component it should return a null value and the
 * runtime will attempt to directly instantiate and manage the component.
 * <p>
 * A {@link ComponentProvider} instance may be registed by passing the 
 * instance to the 
 * {@link com.sun.jersey.spi.container.WebApplication#initiate(com.sun.jersey.api.core.ResourceConfig, ComponentProvider)}
 * method.
 * <p>
 * Applications may extend the {@link com.sun.jersey.spi.container.servlet.ServletContainer}
 * and override the method {@link com.sun.jersey.spi.container.servlet.ServletContainer#initiate(ResourceConfig, WebApplication)}
 * to initiate the {@link com.sun.jersey.spi.container.WebApplication} with the {@link ComponentProvider} instance.
 * 
 * @see com.sun.jersey.spi.container.WebApplication
 * @see com.sun.jersey.spi.container.servlet.ServletContainer
 * @author Paul.Sandoz@Sun.Com
 */
public interface ComponentProvider {
    /**
     * The scope contract for the instantiation of a component.
     */
    public enum Scope {
        /**
         * Declares that only one instance of a component shall exist 
         * per-web application instance. The runtime will manage the component
         * in the scope of the web application.
         */
        Singleton,
        
        /**
         * Declares that the scope is application defined and instances will 
         * be managed by the runtime according to this scope. This requires 
         * that a new instance be created for each invocation of 
         * <code>getInstance</code>.
         */
        PerRequest,
        
        /**
         * The JAX-RS application (jersey) does not care what the scope is,
         * the component provider can decide which to choose - the component
         * provider is responsible for managing instances of a type.
         */
        Undefined    
    }
    
    /**
     * Get the instance of a class. Injection will be performed on the
     * instance.
     * 
     * @param scope the scope of the instance
     * @param c the class
     * @return the instance, or null if the component cannot be instantaited
     *         and managed.
     * 
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     */
    <T> T getInstance(Scope scope, Class<T> c) 
            throws InstantiationException, IllegalAccessException;
    
    /**
     * Get the instance of a class using a constructor and a corresponding
     * array of parameter values. Injection will be performed on the instance.
     * <p>
     * The array of parameter values must be the same length as that required
     * by the constructor. Some parameter values may be null, indicating that
     * the values are not set and must be set by the component provider before
     * construction occurs.
     * 
     * @param scope the scope of the instance
     * @param contructor the constructor to instantiate the class
     * @param parameters the array parameter values passed to the constructor
     * @return the instance, or null if the component cannot be instantaited
     *         and managed.
     * 
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalArgumentException 
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.reflect.InvocationTargetException
     */
    <T> T getInstance(Scope scope, Constructor<T> contructor, Object[] parameters) 
            throws InstantiationException, IllegalArgumentException, 
            IllegalAccessException, InvocationTargetException;

    /**
     * Get the injectable instance to inject JAX-RS and Jersey specific
     * instances on to fields.
     * <p>
     * If the injectable instance is the same as the instance that was passed in
     * then the provider MUST return that instance.
     * 
     * @param instance the instance returned by one of the getInstance methods.
     * @return the injectable instance.
     */
    <T> T getInjectableInstance(T instance);
    
    /**
     * Perform injection on an instance. This may be used when a
     * component is instantiated by means other than the component
     * provider.
     * 
     * @param instance the instance to perform injection on.
     */
    void inject(Object instance);
}