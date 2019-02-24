/*
 * Copyright 2010 Guy Mahieu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.clarent.ivyidea.resolve.dependency;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.DependencyScope;
import org.clarent.ivyidea.intellij.model.IntellijModuleWrapper;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Guy Mahieu
 */

public class InternalDependency extends ResolvedDependencyBase {

    private static final Logger LOGGER = Logger.getLogger(InternalDependency.class.getName());

    private Module module;

    public InternalDependency(Module module) {
        this.module = module;
    }

    @Override
    public void addTo(IntellijModuleWrapper intellijModuleWrapper) {
        if (!intellijModuleWrapper.alreadyHasDependencyOnModule(module)) {
            LOGGER.info("Registering module dependency from " + intellijModuleWrapper.getModuleName() + " on module " + module.getName());
            intellijModuleWrapper.addInternalDependency(this);
        } else {
            LOGGER.info("Dependency from " + intellijModuleWrapper.getModuleName() + " on module " + module.getName() + " was already present; not reregistring");
        }
    }

    public Module getModule() {
        return module;
    }

    /*
    The following table summarizes the classpath information for the possible dependency scopes.

    Scope	 | Sources, when compiled | Sources, when run	| Tests, when compiled | Tests, when run
    Compile	 |         +              |          +          |           +          |        +
    Test     |         -              |          -          |           +          |        +
    Runtime	 |         -              |          +          |           -          |        +
    Provided |         +              |          -          |           +          |        +

     */
    public DependencyScope getRelevantDependencyScope(){
        Set<String> configurations = super.getConfigurations();
        if(configurations.size() == 1)
            return getDependencyScope(configurations.iterator().next());
        else if(configurations.size() == 2 && configurations.contains("test") && configurations.contains("provided")){
            return DependencyScope.PROVIDED;
        }
        return DependencyScope.COMPILE;
    }

    private DependencyScope getDependencyScope(String configuration) {
        switch (configuration) {
            case "test":
                return DependencyScope.TEST;
            case "runtime":
                return DependencyScope.RUNTIME;
            case "provided":
                return DependencyScope.PROVIDED;
            default:
                return DependencyScope.COMPILE;
        }
    }
}
