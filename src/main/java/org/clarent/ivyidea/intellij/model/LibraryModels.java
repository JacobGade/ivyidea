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

package org.clarent.ivyidea.intellij.model;

import com.intellij.openapi.graph.util.Tuple;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryUtil;
import com.intellij.openapi.roots.libraries.PersistentLibraryKind;
import com.intellij.openapi.roots.ui.configuration.libraries.LibraryEditingUtil;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.resolve.dependency.ExternalDependency;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.clarent.ivyidea.util.StringUtils.isBlank;

class LibraryModels implements Closeable {

    private static final Logger LOGGER = Logger.getLogger(LibraryModels.class.getName());    

    private final ConcurrentMap<String, Library.ModifiableModel> libraryModels = new ConcurrentHashMap<String, Library.ModifiableModel>();

    private ModifiableRootModel intellijModule;

    LibraryModels(ModifiableRootModel intellijModule) {
        this.intellijModule = intellijModule;
    }

    public List<Library.ModifiableModel> getForExternalDependency(final ExternalDependency externalDependency) {
        return externalDependency.getConfigurations()
                                 .stream()
                                 .map(c -> isBlank(c) ? "default" : c)
                                 .distinct()
                                 .map(c -> getForConfiguration(c, IvyIdeaConfigHelper.getCreatedLibraryName(intellijModule, c)))
                                 .collect(Collectors.toList());

    }

    private Library.ModifiableModel getForConfiguration(String configuration, String libraryName) {
        if (!libraryModels.containsKey(libraryName)) {
            final Library.ModifiableModel libraryModel = getIvyIdeaLibrary(intellijModule, configuration, libraryName).getModifiableModel();
            libraryModels.putIfAbsent(libraryName, libraryModel);

        }
        return libraryModels.get(libraryName);
    }

    private Library getIvyIdeaLibrary(ModifiableRootModel modifiableRootModel,
                                      String configuration,
                                      final String libraryName) {
        final LibraryTable libraryTable = modifiableRootModel.getModuleLibraryTable();
        Library library = libraryTable.getLibraryByName(libraryName);
        if (library == null) {
            LOGGER.info("Internal library not found for module " + modifiableRootModel.getModule().getModuleFilePath() + ", creating with name " + libraryName + "...");
            library = libraryTable.createLibrary(libraryName);
            if(IvyIdeaConfigHelper.isLibraryNameIncludesConfiguration(modifiableRootModel.getProject())){
                switch (configuration){
                    case "test":
                        modifiableRootModel.findLibraryOrderEntry(library).setScope(DependencyScope.TEST);
                        break;
                    case "runtime":
                        modifiableRootModel.findLibraryOrderEntry(library).setScope(DependencyScope.RUNTIME);
                        break;
                    case "provided":
                        modifiableRootModel.findLibraryOrderEntry(library).setScope(DependencyScope.PROVIDED);
                        break;
                }
            }
        }
        return library;
    }

    public void close() {
        for (Library.ModifiableModel libraryModel : libraryModels.values()) {
            if (libraryModel.isChanged()) {
                libraryModel.commit();
            } else {
                libraryModel.dispose();
            }
        }
    }
}
