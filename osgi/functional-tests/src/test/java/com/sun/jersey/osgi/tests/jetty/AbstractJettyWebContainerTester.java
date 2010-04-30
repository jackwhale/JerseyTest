/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.osgi.tests.jetty;



import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.osgi.tests.util.Helper;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Servlet;
import javax.ws.rs.core.UriBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.MavenConfiguredJUnit4TestRunner;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 *
 * @author japod
 */
@RunWith(MavenConfiguredJUnit4TestRunner.class)
public abstract class AbstractJettyWebContainerTester {
    public static final String CONTEXT = "";

    private Server server;

    private int port = Helper.getEnvVariable("JERSEY_HTTP_PORT", 9997);
    
    private String contextPath;

    private Class<? extends Servlet> sc;

    public AbstractJettyWebContainerTester() {
        this(CONTEXT);
    }

    protected AbstractJettyWebContainerTester(String contextPath) {
        this.contextPath = contextPath;
    }

    public UriBuilder getUri() {
        return UriBuilder.fromUri("http://localhost").port(port).path(contextPath);
    }
    
    public void setServletClass(Class<? extends Servlet> sc) {
        this.sc = sc;
    }
    
    public void startServer(Class... resources) {
        Map<String, String> initParams = getInitParams(resources);
        start(initParams);
    }
    
    public void startServer(Map<String, String> initParams) {
        start(initParams);
    }
    
    public void startServer(Map<String, String> params, Class... resources) {
        Map<String, String> initParams = getInitParams(resources);
        initParams.putAll(params);
        start(initParams);
    }
    
    private Map<String, String> getInitParams(Class... resources) {
        Map<String, String> initParams = new HashMap<String, String>();
        
        StringBuilder sb = new StringBuilder();
        for (Class r : resources) {            
            if (sb.length() > 0)
                sb.append(';');
            sb.append(r.getName());
        }
        
        initParams.put(ServletContainer.RESOURCE_CONFIG_CLASS, 
                ClassNamesResourceConfig.class.getName());
                        initParams.put(ClassNamesResourceConfig.PROPERTY_CLASSNAMES, sb.toString());
        return initParams;
    }
    
    private void start(Map<String, String> initParams) {
        if (server != null && server.isRunning()){
            stopServer();
        }

        System.out.println("Starting Jetty, port number = " + port);
        
        URI u = getUri().path("/").build();
        server = new Server(port);
        Context root = new Context(server, contextPath);
        ServletHolder sh;
        if (sc == null) {
            sh = new ServletHolder(ServletContainer.class);
        } else {
            sh = new ServletHolder(sc);
        }
        sh.setInitParameters(initParams);
        root.addServlet(sh, "/*");


        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("Started Jetty");

        int timeToSleep = Helper.getEnvVariable("JERSEY_HTTP_SLEEP", 0);
        if (timeToSleep > 0) {
            System.out.println("Sleeping for " + timeToSleep + " ms");
            try {
                // Wait for the server to start
                Thread.sleep(timeToSleep);
            } catch (InterruptedException ex) {
                System.out.println("Sleeping interrupted: " + ex.getLocalizedMessage());
            }
        }
    }
    
    public void stopServer() {
        if (server.isRunning()) {
            try {
                server.stop();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        stopServer();
    }
}