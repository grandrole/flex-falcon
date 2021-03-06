/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.flex.compiler.tree.mxml;

import org.apache.flex.compiler.definitions.IClassDefinition;

/**
 * This AST node represents an MXML <code>&lt;Definition&gt;</code> tag in an
 * AST.
 * <p>
 * An {@link IMXMLDefinitionNode} is a {@link IMXMLClassDefinitionNode} and thus
 * has all the same children as an {@link IMXMLClassDefinitionNode}.
 * <p>
 * The {@link IMXMLDefinitionNode} have a required name attribute that specifies
 * the name of the definition.
 */
public interface IMXMLDefinitionNode extends IMXMLNode
{
    /**
     * Gets the tag name of the definition as specified by the <code>name</code>
     * attribute on the <code>&lt;Definition&gt;</code> tag.
     * 
     * @return The name of the definition, if one was specified, and
     * <code>null</code> otherwise.
     */
    String getDefinitionName();

    /**
     * Gets the class-defining node which is the sole child of this node.
     * 
     * @return An {@link IMXMLClassDefinitionNode}.
     */
    IMXMLClassDefinitionNode getContainedClassDefinitionNode();

    /**
     * Gets the class definition of the defined class.
     * 
     * @return An {@link IClassDefinition} object representing the class defined
     * within this <code>&lt;Definition&gt;</code> tag.
     */
    IClassDefinition getContainedClassDefinition();
}
