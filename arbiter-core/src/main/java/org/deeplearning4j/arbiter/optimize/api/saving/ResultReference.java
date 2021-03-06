/*-
 *
 *  * Copyright 2016 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */
package org.deeplearning4j.arbiter.optimize.api.saving;

import org.deeplearning4j.arbiter.optimize.api.OptimizationResult;
import org.nd4j.shade.jackson.annotation.JsonTypeInfo;

import java.io.IOException;

/**
 * Idea: We can't store all results in memory in general (might have thousands of candidates with millions of
 * parameters each)
 * So instead: return a reference to the saved result. Idea is that the result may be saved to disk or a database,
 * and we can easily load it back into memory (if/when required) using the getResult() method
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface ResultReference {

    OptimizationResult getResult() throws IOException;

}
