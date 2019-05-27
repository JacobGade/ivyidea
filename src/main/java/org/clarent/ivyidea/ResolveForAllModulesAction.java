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

package org.clarent.ivyidea;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleId;
import org.clarent.ivyidea.exception.IvyFileReadException;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.intellij.task.IvyIdeaResolveBackgroundTask;
import org.clarent.ivyidea.ivy.IvyManager;
import org.clarent.ivyidea.resolve.DependencyResolver;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Action to resolve the dependencies for all modules that have an IvyIDEA facet configured.
 *
 * @author Guy Mahieu
 */
public class ResolveForAllModulesAction extends AbstractResolveAction {

    public void actionPerformed(AnActionEvent e) {
        FileDocumentManager.getInstance().saveAllDocuments();

        final Project project = DataKeys.PROJECT.getData(e.getDataContext());
        ProgressManager.getInstance().run(new IvyIdeaResolveBackgroundTask(project, e) {
            public void doResolve(final @NotNull ProgressIndicator indicator) throws IvySettingsNotFoundException, IvyFileReadException, IvySettingsFileReadException {
                ConsoleView consoleView = IntellijUtils.getConsoleView(project);
                consoleView.clear();
                consoleView.print("Initializing Ivy settings\n", ConsoleViewContentType.NORMAL_OUTPUT);
                indicator.setText("Initializing Ivy settings");

                final IvyManager ivyManager = new IvyManager();
                final DependencyResolver resolver = new DependencyResolver();
                final long startTime = System.nanoTime();
                long resolveTime = 0;
                long processingTime = 0;
                Module[] allLoadedModulesWithIvyIdeaFacet = IntellijUtils.getAllModulesWithIvyIdeaFacet(project);
                HashMap<ModuleId, Module> moduleMap = new HashMap<>();
                ArrayList<DependencyResolutionPackage> packages = new ArrayList<>();
                ArrayList<Module> brokenModules = new ArrayList<>();
                for (final Module module : allLoadedModulesWithIvyIdeaFacet){
                    ModuleDescriptor moduleDescriptor = ivyManager.getModuleDescriptor(module);
                    if(moduleDescriptor != null)
                        moduleMap.put(moduleDescriptor.getModuleRevisionId().getModuleId(), module);
                    else{
                        consoleView.print("Unable to find Ivy files at specified location for following modules. \n" +
                                          String.join("\n", brokenModules.stream().map(Module::getName).toArray(String[]::new)),
                                          ConsoleViewContentType.ERROR_OUTPUT);
                        brokenModules.add(module);
                    }
                }
                allLoadedModulesWithIvyIdeaFacet = Arrays.stream(allLoadedModulesWithIvyIdeaFacet)
                                                         .filter(m -> !brokenModules.contains(m))
                                                         .toArray(Module[]::new);

                String resolvingFormatString = "(%d/"+allLoadedModulesWithIvyIdeaFacet.length+") Resolving dependencies for %s\n";
                String resolvedFormatString = "(%d/"+allLoadedModulesWithIvyIdeaFacet.length+") Finished resolving dependencies for %s. Resolve time: %s, Processing time: %s\n";
                for (int i = 0; i < allLoadedModulesWithIvyIdeaFacet.length; i++) {
                    Module module = allLoadedModulesWithIvyIdeaFacet[i];
                    indicator.setFraction(((double)i) / allLoadedModulesWithIvyIdeaFacet.length);
                    indicator.setText("Resolve for all modules ("+(i+1)+"/"+allLoadedModulesWithIvyIdeaFacet.length+")");

                    Ivy ivy = ivyManager.getIvy(module);
                    getProgressMonitorThread().setIvy(ivy);
                    indicator.setText2("Resolving for module " + module.getName());
                    consoleView.print(String.format(resolvingFormatString, i+1, module.getName()),
                                      ConsoleViewContentType.NORMAL_OUTPUT);
                    DependencyResolutionPackage resolve = resolver.resolve(ivy, module, moduleMap);
                    packages.add(resolve);
                    consoleView.print(String.format(resolvedFormatString,
                                                    i + 1, module.getName(),
                                                    getDurationText(resolve.getResolveTime()),
                                                    getDurationText(resolve.getExtractDependenciesTime())),
                                      ConsoleViewContentType.NORMAL_OUTPUT);
                    resolveTime += resolve.getResolveTime();
                    processingTime += resolve.getExtractDependenciesTime();
                    if (indicator.isCanceled()) {
                        return;
                    }
                }
                consoleView.print("Finished resolving modules. \n" +
                                  "Total time spent resolving: " + getDurationText(resolveTime) + "\n" +
                                  "Total time spent processing: " + getDurationText(processingTime) + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
                for (DependencyResolutionPackage module : packages){
                    if(!module.getProblems().isEmpty())
                        reportProblems(module.getModule(), module.getProblems());
                }
                final long dependencyStartTime = System.nanoTime();
                consoleView.print("Updating IntelliJ modules with resolved dependencies", ConsoleViewContentType.NORMAL_OUTPUT);
                indicator.setText("Updating IntelliJ modules with resolved dependencies");
                indicator.setText2("");
                indicator.setIndeterminate(true);
                updateIntellijModel(project, packages.toArray(new DependencyResolutionPackage[0]));
                consoleView.print("Total time spent updating dependencies: " + getDurationText(dependencyStartTime, System.nanoTime()) + "\n" +
                                  "Total time: " + getDurationText(startTime, System.nanoTime()) + "\n",
                                  ConsoleViewContentType.NORMAL_OUTPUT);
            }
        });
    }
}
