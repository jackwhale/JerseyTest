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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.impl.wadl;

import com.sun.jersey.api.MediaTypes;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.WebResource;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author mh124079
 */
public class WadlResourceTest extends AbstractResourceTester {
    
    public WadlResourceTest(String testName) {
        super(testName);
    }
    
    @Path("foo")
    public static class ExtraResource {
        @GET
        @Produces("application/xml")
        public String getRep() {
            return null;
        }
    }

    @Path("widgets")
    public static class WidgetsResource {

        @GET
        @Produces({"application/xml", "application/json"})
        public String getWidgets() {
            return null;
        }

        @POST
        @Consumes({"application/xml"})
        @Produces({"application/xml", "application/json"})
        public String createWidget(String bar) {
            return bar;
        }

        @PUT
        @Path("{id}")
        @Consumes("application/xml")
        public void updateWidget(String bar, @PathParam("id")int id) {
        }

        @GET
        @Path("{id}")
        @Produces({"application/xml", "application/json"})
        public String getWidget(@PathParam("id")int id) {
            return null;
        }

        @DELETE
        @Path("{id}")
        public void deleteWidget(@PathParam("id")int id) {
        }

        @Path("{id}/verbose")
        public Object getVerbose(@PathParam("id")int id) {
            return new ExtraResource();
        }
    }
    
    /**
     * Test WADL generation
     */
    public void testGetWadl() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        initiateWebApplication(WidgetsResource.class, ExtraResource.class);
        WebResource r = resource("/application.wadl");
        
        File tmpFile = r.get(File.class);
        DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
        bf.setNamespaceAware(true);
        bf.setValidating(false);
        bf.setXIncludeAware(false);
        DocumentBuilder b = bf.newDocumentBuilder();
        Document d = b.parse(tmpFile);
        printSource(new DOMSource(d));
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new NSResolver("wadl", "http://research.sun.com/wadl/2006/10"));
        // check base URI
        String val = (String)xp.evaluate("/wadl:application/wadl:resources/@base", d, XPathConstants.STRING);
        assertEquals(val,BASE_URI.toString());
        // check total number of resources is 4
        val = (String)xp.evaluate("count(//wadl:resource)", d, XPathConstants.STRING);
        assertEquals(val,"4");
        // check only once resource with for {id}
        val = (String)xp.evaluate("count(//wadl:resource[@path='{id}'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check only once resource with for {id}/verbose
        val = (String)xp.evaluate("count(//wadl:resource[@path='{id}/verbose'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check only once resource with for widgets
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check 3 methods for {id}
        val = (String)xp.evaluate("count(//wadl:resource[@path='{id}']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"3");
        // check 2 methods for widgets
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"2");
        // check type of {id} is int
        val = (String)xp.evaluate("//wadl:resource[@path='{id}']/wadl:param[@name='id']/@type", d, XPathConstants.STRING);
        assertEquals(val,"xs:int");
        // check number of output representations is two
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets']/wadl:method[@name='GET']/wadl:response/wadl:representation)", d, XPathConstants.STRING);
        assertEquals(val,"2");
        // check number of output representations is one
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets']/wadl:method[@name='POST']/wadl:request/wadl:representation)", d, XPathConstants.STRING);
        assertEquals(val,"1");
    }
    
    public void testOptionsResourceWadl() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        initiateWebApplication(WidgetsResource.class, ExtraResource.class);
        WebResource r = resource("/widgets");
        
        // test WidgetsResource
        File tmpFile = r.options(File.class);
        DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
        bf.setNamespaceAware(true);
        bf.setValidating(false);
        bf.setXIncludeAware(false);
        DocumentBuilder b = bf.newDocumentBuilder();
        Document d = b.parse(tmpFile);
        printSource(new DOMSource(d));
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new NSResolver("wadl", "http://research.sun.com/wadl/2006/10"));

        // check base URI
        String val = (String)xp.evaluate("/wadl:application/wadl:resources/@base", d, XPathConstants.STRING);
        assertEquals(val,BASE_URI.toString());
        // check total number of resources is 3 (no ExtraResource details included)
        val = (String)xp.evaluate("count(//wadl:resource)", d, XPathConstants.STRING);
        assertEquals(val,"3");
        // check only once resource with for {id}
        val = (String)xp.evaluate("count(//wadl:resource[@path='{id}'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check only once resource with for {id}/verbose
        val = (String)xp.evaluate("count(//wadl:resource[@path='{id}/verbose'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check only once resource with for widgets
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check 3 methods for {id}
        val = (String)xp.evaluate("count(//wadl:resource[@path='{id}']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"3");
        // check 2 methods for widgets
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"2");
        // check type of {id} is int
        val = (String)xp.evaluate("//wadl:resource[@path='{id}']/wadl:param[@name='id']/@type", d, XPathConstants.STRING);
        assertEquals(val,"xs:int");
        // check number of output representations is two
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets']/wadl:method[@name='GET']/wadl:response/wadl:representation)", d, XPathConstants.STRING);
        assertEquals(val,"2");
        // check number of output representations is one
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets']/wadl:method[@name='POST']/wadl:request/wadl:representation)", d, XPathConstants.STRING);
        assertEquals(val,"1");

        // test ExtraResource
        r = resource("/foo");
        
        tmpFile = r.options(File.class);
        b = bf.newDocumentBuilder();
        d = b.parse(tmpFile);
        printSource(new DOMSource(d));
        // check base URI
        val = (String)xp.evaluate("/wadl:application/wadl:resources/@base", d, XPathConstants.STRING);
        assertEquals(val,BASE_URI.toString());
        // check total number of resources is 1 (no ExtraResource details included)
        val = (String)xp.evaluate("count(//wadl:resource)", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check only once resource with path foo
        val = (String)xp.evaluate("count(//wadl:resource[@path='foo'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check 1 methods for foo
        val = (String)xp.evaluate("count(//wadl:resource[@path='foo']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"1");
    }
    
    public void testOptionsLocatorWadl() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        initiateWebApplication(WidgetsResource.class, ExtraResource.class);
        WebResource r = resource("/widgets/3/verbose");
        
        // test WidgetsResource
        File tmpFile = r.accept(MediaTypes.WADL).options(File.class);
        DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
        bf.setNamespaceAware(true);
        bf.setValidating(false);
        bf.setXIncludeAware(false);
        DocumentBuilder b = bf.newDocumentBuilder();
        Document d = b.parse(tmpFile);
        printSource(new DOMSource(d));
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new NSResolver("wadl", "http://research.sun.com/wadl/2006/10"));
        // check base URI
        String val = (String)xp.evaluate("/wadl:application/wadl:resources/@base", d, XPathConstants.STRING);
        assertEquals(val,BASE_URI.toString());
        // check total number of resources is 1 (no ExtraResource details included)
        val = (String)xp.evaluate("count(//wadl:resource)", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check only once resource with path 
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets/3/verbose'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check 1 methods
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets/3/verbose']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"1");
    }
    
    public void testOptionsSubResourceWadl() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        initiateWebApplication(WidgetsResource.class, ExtraResource.class);
        WebResource r = resource("/widgets/3");
        
        // test WidgetsResource
        File tmpFile = r.accept(MediaTypes.WADL).options(File.class);
        DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
        bf.setNamespaceAware(true);
        bf.setValidating(false);
        bf.setXIncludeAware(false);
        DocumentBuilder b = bf.newDocumentBuilder();
        Document d = b.parse(tmpFile);
        printSource(new DOMSource(d));
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new NSResolver("wadl", "http://research.sun.com/wadl/2006/10"));
        String val = (String)xp.evaluate("/wadl:application/wadl:resources/@base", d, XPathConstants.STRING);
        assertEquals(val,BASE_URI.toString());
        // check total number of resources is 1
        val = (String)xp.evaluate("count(//wadl:resource)", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check only one resource with for {id}
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets/3'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check 3 methods
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets/3']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"3");
    }
    
    @Path("root")
    public static class RootResource {
        @Path("loc")
        public Object getSub() {
            return new SubResource();
        }
    }
    
    public static class SubResource {
        @Path("loc")
        public Object getSub() {
            return new SubResource();
        }
        
        @GET
        @Produces("text/plain")
        public String hello() {
            return "Hello World !";
        }
        
        @GET
        @Path("sub")
        @Produces("text/plain")
        public String helloSub() {
            return "Hello World !";
        }
    }
    
    public void testRecursive() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        initiateWebApplication(RootResource.class);
        WebResource r = resource("/root/loc");
        
        // test WidgetsResource
        File tmpFile = r.accept(MediaTypes.WADL).options(File.class);
        DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
        bf.setNamespaceAware(true);
        bf.setValidating(false);
        bf.setXIncludeAware(false);
        DocumentBuilder b = bf.newDocumentBuilder();
        Document d = b.parse(tmpFile);
        printSource(new DOMSource(d));
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new NSResolver("wadl", "http://research.sun.com/wadl/2006/10"));
        String val = (String)xp.evaluate("/wadl:application/wadl:resources/@base", d, XPathConstants.STRING);
        assertEquals(val,BASE_URI.toString());
        // check only one resource with for 'root/loc'
        val = (String)xp.evaluate("count(//wadl:resource[@path='root/loc'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        
        r = resource("/root/loc/loc");
        
        // test WidgetsResource
        tmpFile = r.accept(MediaTypes.WADL).options(File.class);
        bf = DocumentBuilderFactory.newInstance();
        bf.setNamespaceAware(true);
        bf.setValidating(false);
        bf.setXIncludeAware(false);
        b = bf.newDocumentBuilder();
        d = b.parse(tmpFile);
        printSource(new DOMSource(d));
        xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new NSResolver("wadl", "http://research.sun.com/wadl/2006/10"));
        val = (String)xp.evaluate("/wadl:application/wadl:resources/@base", d, XPathConstants.STRING);
        assertEquals(val,BASE_URI.toString());
        // check only one resource with for 'root/loc'
        val = (String)xp.evaluate("count(//wadl:resource[@path='root/loc/loc'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        
    }
    
    private static class NSResolver implements NamespaceContext {
        private String prefix;
        private String nsURI;
        
        public NSResolver(String prefix, String nsURI) {
            this.prefix = prefix;
            this.nsURI = nsURI;
        }
        
        public String getNamespaceURI(String prefix) {
             if (prefix.equals(this.prefix))
                 return this.nsURI;
             else
                 return XMLConstants.NULL_NS_URI;
        }

        public String getPrefix(String namespaceURI) {
            if (namespaceURI.equals(this.nsURI))
                return this.prefix;
            else
                return null;
        }

        public Iterator getPrefixes(String namespaceURI) {
            return null;
        }
    }

    
    private static void printSource(Source source) {
        try {
            System.out.println("---------------------");
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            Properties oprops = new Properties();
            oprops.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
            oprops.put(OutputKeys.INDENT, "yes");
            oprops.put(OutputKeys.METHOD, "xml");
            trans.setOutputProperties(oprops);
            trans.transform(source, new StreamResult(System.out));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}