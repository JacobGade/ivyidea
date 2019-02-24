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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.Library.ModifiableModel;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.vfs.VirtualFile;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.intellij.compatibility.IntellijCompatibilityService;
import org.clarent.ivyidea.ivy.IvyManager;
import org.clarent.ivyidea.ivy.IvyUtil;
import org.clarent.ivyidea.resolve.dependency.ExternalDependency;
import org.clarent.ivyidea.resolve.dependency.InternalDependency;
import org.clarent.ivyidea.resolve.dependency.ResolvedDependency;

import java.io.Closeable;
import java.util.*;

public class IntellijModuleWrapper implements Closeable {

    private final ModifiableRootModel intellijModule;
    private final LibraryModels libraryModels;

    public static IntellijModuleWrapper forModule(Module module) {
        ModifiableRootModel modifiableModel = null;
        try {
            modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
            return new IntellijModuleWrapper(modifiableModel);
        } catch (RuntimeException e) {
            if (modifiableModel != null) {
                modifiableModel.dispose();
            }
            throw e;
        }
    }

    private IntellijModuleWrapper(ModifiableRootModel intellijModule) {
        this.intellijModule = intellijModule;
        this.libraryModels = new LibraryModels(intellijModule);
    }

    public void updateDependencies(Collection<ResolvedDependency> resolvedDependencies) {
        List<OrderEntry> toRemove = new ArrayList<>();
        for (OrderEntry entry : intellijModule.getOrderEntries()) {
            if(entry instanceof ModuleOrderEntry){
                toRemove.add(entry);
            }
        }

        for (OrderEntry entry : toRemove) {
            intellijModule.removeOrderEntry(entry);
        }

        LibraryTable libraryTable = intellijModule.getModuleLibraryTable();
        Library[] libraries = libraryTable.getLibraries();
        for (Library library : libraries) {
            if (IvyIdeaConfigHelper.isCreatedLibraryName(library.getName())) {
                libraryTable.removeLibrary(library);
            }
        }

        for (ResolvedDependency resolvedDependency : resolvedDependencies) {
            resolvedDependency.addTo(this);
        }
    }

    public void close() {
        libraryModels.close();
        if (intellijModule.isChanged()) {
            intellijModule.commit();
        } else {
            intellijModule.dispose();
        }
    }

    public String getModuleName() {
        return intellijModule.getModule().getName();
    }

    public void addInternalDependency(InternalDependency internalDependency) {
        ModuleOrderEntry entry = intellijModule.addModuleOrderEntry(internalDependency.getModule());
        entry.setScope(internalDependency.getRelevantDependencyScope());
    }

    public void addExternalDependency(ExternalDependency externalDependency) {
        List<ModifiableModel> libraryModels = this.libraryModels.getForExternalDependency(externalDependency);
        for (ModifiableModel libraryModel : libraryModels) {
            libraryModel.addRoot(externalDependency.getUrlForLibraryRoot(), externalDependency.getType());
        }
    }

    public boolean alreadyHasDependencyOnModule(Module module) {
        final Module[] existingDependencies = intellijModule.getModuleDependencies();
        for (Module existingDependency : existingDependencies) {
            if (existingDependency.getName().equals(module.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean alreadyHasDependencyOnLibrary(ExternalDependency externalDependency) {
        List<ModifiableModel> libraryModels = this.libraryModels.getForExternalDependency(externalDependency);
        boolean allHasDependency = true;
        for (ModifiableModel libraryModel : libraryModels) {
            boolean hasDependency = false;
            for (String url : libraryModel.getUrls(externalDependency.getType())) {
                if (externalDependency.isSameDependency(url)) {
                    hasDependency = true;
                }
            }
            allHasDependency &= hasDependency;
        }

        return allHasDependency;
    }
}
