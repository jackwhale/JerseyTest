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
package com.sun.jersey.api.json;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * An immutable configuration of JSON notationand options. JSONConfiguration could be used
 * for configuring the JSON notation on {@link JSONJAXBContext}
 *
 * @author Jakub.Podlesak@Sun.COM
 */
public class JSONConfiguration {

    /**
     * Enumeration of supported JSON notations.
     */
    public enum Notation {

        /**
         * The mapped (default) JSON notation.
         * <p>Example JSON expression:<pre>
         * {"columns":[{"id":"userid","label":"UserID"},{"id":"name","label":"User Name"}],"rows":{"userid":"1621","name":"Grotefend"}}
         * </pre>
         */
        MAPPED,
        /**
         * The mapped Jettison JSON notation.
         * <p>Example JSON expression:<pre>
         * {"userTable":{"columns":[{"id":"userid","label":"UserID"},{"id":"name","label":"User Name"}],"rows":{"userid":1621,"name":"Grotefend"}}}
         * </pre>
         */
        MAPPED_JETTISON,
        /**
         * The mapped Badgerfish JSON notation.
         * <p>Example JSON expression:<pre>
         * {"userTable":{"columns":[{"id":{"$":"userid"},"label":{"$":"UserID"}},{"id":{"$":"name"},"label":{"$":"User Name"}}],"rows":{"userid":{"$":"1621"},"name":{"$":"Grotefend"}}}}
         * </pre>
         */
        BADGERFISH,
        /**
         * The natural JSON notation, leveraging closely-coupled JAXB RI integration.
         * <p>Example JSON expression:<pre>
         * {"columns":[{"id":"userid","label":"UserID"},{"id":"name","label":"User Name"}],"rows":[{"userid":1621,"name":"Grotefend"}]}
         * </pre>
         */
        NATURAL
    };
    
    private final Notation notation;
    private final Collection<String> arrays;
    private final Collection<String> attrsAsElems;
    private final Collection<String> nonStrings;
    private final boolean rootUnwrapping;
    private final Map<String, String> jsonXml2JsonNs;

    /**
     * Builder class for constructing {@link JSONConfiguration} options
     */
    public static class Builder {

        private final Notation notation;
        protected Collection<String> arrays = new HashSet<String>(0);
        protected Collection<String> attrsAsElems = new HashSet<String>(0);
        protected Collection<String> nonStrings = new HashSet<String>(0);
        protected boolean rootUnwrapping = true;
        protected Map<String, String> jsonXml2JsonNs = new HashMap<String, String>(0);

        private Builder(Notation notation) {
            this.notation = notation;
        }
        
        /**
         * Constructs a new immutable {@link JSONConfiguration} object based on options set on this Builder
         *
         * @return a non-null {@link JSONConfiguration} instance
         */
        public JSONConfiguration build() {
            return new JSONConfiguration(this);
        }
    }

    /**
     * Builder class for constructing {@link JSONConfiguration} options
     * for the {@link MAPPED} convention.
     */
    public static class MappedBuilder extends Builder {

        private MappedBuilder(Notation notation) {
            super(notation);
        }

        /**
         * Adds name(s) to JSON arrays configuration property.
         * This property is valid for the {@link JSONConfiguration.Notation#MAPPED} notation only.
         * <p>
         * The property value is a collection of strings representing JSON object names.
         * Those objects will be declared as arrays in  JSON document.
         * <p>
         * For example, consider that the the property value is not set and the
         * JSON document is <code>{ ..., "arr1":"single element", ... }</code>.
         * If the property value is set to contain <code>"arr1"</code> then
         * the JSON document would become <code>{ ..., "arr1":["single element"], ... }</code>.
         * <p>
         * The default value is an empty collection.
         * @param arrays an array of strings representing JSON object names.
         * @return the mapped builder.
         */
        public MappedBuilder arrays(String... arrays) {
            this.arrays.addAll(Arrays.asList(arrays));
            return this;
        }

        /**
         * Adds name(s) toJSON attributes as elements property.
         * This property is valid for the {@link JSONConfiguration.Notation#MAPPED} notation only.
         * <p>
         * The value is a collection of string values that are
         * object names that correspond to XML attribute information items.
         * The value of an object name in the JSON document that exists in the collection
         * of object names will be declared as an element as not as an attribute if
         * the object corresponds to an XML attribute information item.
         * <p>
         * For example, consider that the property value is not set and the
         * JSON document is <code>{ ..., "@number":"12", ... }</code>.
         * If the property value is set to contain <code>"number"</code>
         * then the JSON document would be <code>{ ..., "number":"12", ... }</code>.
         * <p>
         * The default value is an empty collection.
         * @param attributeAsElements an array of string values that are
         *        object names that correspond to XML attribute information items.
         * @return the mapped builder.
         */
        public MappedBuilder attributeAsElement(String... attributeAsElements) {
            this.attrsAsElems.addAll(Arrays.asList(attributeAsElements));
            return this;
        }

        /**
         * Setter for XML to JSON namespace mapping.
         * This property is valid for the {@link JSONConfiguration.Notation#MAPPED_JETTISON} notation only.
         * <p>
         * The value is a map with zero or more
         * key/value pairs, where the key is an XML namespace and the value
         * is the prefix to use as the replacement for the XML namespace.
         * <p>
         * The default value is a map with zero key/value pairs.
         * @param jsonXml2JsonNs a map with zero or more key/value pairs,
         *        where the key is an XML namespace and the value
         *        is the prefix to use as the replacement for the XML namespace.
         * @return the mapped builder.
         */
        public MappedBuilder xml2JsonNs(Map<String, String> jsonXml2JsonNs) {
            this.jsonXml2JsonNs = jsonXml2JsonNs;
            return this;
        }

        /**
         * Adds name(s) JSON non-string values property.
         * This property is valid for the {@link JSONConfiguration.Notation#MAPPED} notation only.
         * <p>
         * The value is collection of string values that are
         * object names.
         * The value of an object name in the JSON document that exists in the collection
         * of object names will be declared as non-string value, which is not surrounded
         * by double quotes.
         * <p>
         * For example, consider that the the property value is not set and the
         * JSON document is <code>{ ..., "anumber":"12", ... }</code>.
         * If the property value is set to contain <code>"anumber"</code>
         * then the JSON document would be <code>{ ..., "anumber":12, ... }</code>.
         * <p>
         * The default value is an empty collection.
         * @param nonStrings an array of string values that are
         * object names
         * @return the mapped builder.
         */
        public MappedBuilder nonStrings(String... nonStrings) {
            this.nonStrings.addAll(Arrays.asList(nonStrings));
            return this;
        }

        /**
         * Setter for XML root element unwrapping.
         * This property is valid for the {@link JSONConfiguration.Notation#MAPPED} notation only.
         * <p>
         * If set to true, JSON code corresponding to the XML root element will be stripped out
         * <p>
         * The default value is false.
         * @param rootUnwrapping if set to true, JSON code corresponding to the
         *        XML root element will be stripped out.
         * @return the mapped builder.
         */
        public MappedBuilder rootUnwrapping(boolean rootUnwrapping) {
            this.rootUnwrapping = rootUnwrapping;
            return this;
        }
    }

    private JSONConfiguration(Builder b) {
        notation = b.notation;
        arrays = b.arrays;
        attrsAsElems = b.attrsAsElems;
        nonStrings = b.nonStrings;
        rootUnwrapping = b.rootUnwrapping;
        jsonXml2JsonNs = b.jsonXml2JsonNs;
    }
    /**
     * The default JSONConfiguration uses {@link JSONConfiguration.Notation#MAPPED} notation with root unwrapping option set to true.
     */
    public static JSONConfiguration DEFAULT = mapped().rootUnwrapping(true).build();

    /**
     * A static method for obtaining a builder of {@link JSONConfiguration} instance, which will use {@link Notation#NATURAL} JSON notation.
     * After getting the builder, you can set configuration options on it, and finally get an immutable JSONConfiguration
     * instance using the {@link Builder#build() } method.
     *
     * @return a builder for JSONConfiguration instance
     */
    public static Builder natural() {
        return new Builder(Notation.NATURAL);
    }

    /**
     * A static method for obtaining a builder of {@link JSONConfiguration} instance, which will use {@link Notation#MAPPED} JSON notation.
     * After getting the builder, you can set configuration options on it and finally get an immutable  JSONConfiguration
     * instance the using {@link Builder#build() } method.
     *
     * @return a builder for JSONConfiguration instance
     */
    public static MappedBuilder mapped() {
        return new MappedBuilder(Notation.MAPPED);
    }

    /**
     * A static method for obtaining a builder of {@link JSONConfiguration} instance, which will use {@link Notation#MAPPED_JETTISON} JSON notation.
     * After getting the builder, you can set configuration options on it and finally get an immutable  JSONConfiguration
     * instance using the {@link Builder#build() } method.
     *
     * @return a builder for JSONConfiguration instance
     */
    public static Builder mappedJettison() {
        return new MappedBuilder(Notation.MAPPED_JETTISON);
    }

    /**
     * A static method for obtaining a builder of {@link JSONConfiguration} instance, which will use {@link Notation#BADGERFISH} JSON notation.
     * After getting the builder, you can set configuration options on it and finally get an immutable  JSONConfiguration
     * instance using the {@link Builder#build() } method.
     *
     * @return a builder for JSONConfiguration instance
     */
    public static Builder badgerFish() {
        return new Builder(Notation.BADGERFISH);
    }

    
    /**
     * Returns JSON notation selected for this configuration
     * @return JSON notation
     */
    public Notation getNotation() {
        return notation;
    }

    /**
     * Returns JSON array names property
     * This property is valid for the {@link JSONConfiguration.Notation#MAPPED} notation only.
     * @return collection of array names
     * @see Builder#arrays(java.lang.String[]) 
     */
    public Collection<String> getArrays() {
        return (arrays != null) ? Collections.unmodifiableCollection(arrays) : null;
    }

    /**
     * Returns names of attributes, which will be handled as elements
     * This property is valid for the {@link JSONConfiguration.Notation#MAPPED} notation only.
     * @return attribute as element names collection
     * @see Builder#attributeAsElement(java.lang.String[])
     */
    public Collection<String> getAttributeAsElements() {
        return (attrsAsElems != null) ? Collections.unmodifiableCollection(attrsAsElems) : null;
    }

    /**
     * Returns a map for XML to JSON namespace mapping
     * This property is valid for the {@link JSONConfiguration.Notation#MAPPED} notation only.
     * @return a map for XML to JSON namespace mapping
     * @see Builder#xml2JsonNs(java.util.Map)
     */
    public Map<String, String> getXml2JsonNs() {
        return (jsonXml2JsonNs != null) ? Collections.unmodifiableMap(jsonXml2JsonNs) : null;
    }

    /**
     * Returns names of JSON objects, which will be serialized out as non-strings, i.e. without delimiting their values with double quotes
     * This property is valid for the {@link JSONConfiguration.Notation#MAPPED} notation only.
     * @return name of non-string JSON objects
     * @see Builder#nonStrings(java.lang.String[])
     */
    public Collection<String> getNonStrings() {
        return (nonStrings != null) ? Collections.unmodifiableCollection(nonStrings) : null;
    }

    /**
     * Says if the root element will be stripped off
     * This property is valid for the {@link JSONConfiguration.Notation#MAPPED} notation only.
     * @return true, if root element has to be stripped off
     * @see Builder#rootUnwrapping(boolean) 
     */
    public boolean isRootUnwrapping() {
        return rootUnwrapping;
    }
}
