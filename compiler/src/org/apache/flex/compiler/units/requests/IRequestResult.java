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

package org.apache.flex.compiler.units.requests;

import org.apache.flex.compiler.problems.ICompilerProblem;

/**
 * Base interface for the result of a ICompilationUnit request result.
 * @see org.apache.flex.compiler.units.ICompilationUnit
 */
public interface IRequestResult
{
    /**
     * @return ICompilerProblems found while computing the result of the request.
     */
    ICompilerProblem[] getProblems();
}
