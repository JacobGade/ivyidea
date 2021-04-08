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

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.intellij.facet.config.IvyIdeaFacetConfiguration;
import org.clarent.ivyidea.intellij.model.IntellijModuleWrapper;
import org.clarent.ivyidea.resolve.problem.ResolveProblem;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

/**
 * @author Guy Mahieu
 */
abstract class AbstractResolveAction extends AnAction {
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    void updateIntellijModel(final Project project,
                             final ProgressIndicator progressIndicator,
                             final DependencyResolutionPackage... packages) {
        progressIndicator.setText("Updating IntelliJ modules with resolved dependencies");


        DumbService.getInstance(project)
                   .suspendIndexingAndRun("Updating IntelliJ module dependencies",
                                          () -> {
                                              for (int i = 0, packagesLength = packages.length; i < packagesLength; i++) {
                                                  DependencyResolutionPackage drp = packages[i];
                                                  progressIndicator.setText2("(" + i + "/" + packagesLength + ")");
                                                  progressIndicator.setFraction(((double) i) / packagesLength);
                                                  final Runnable updateDependencies = () -> {
                                                      try (IntellijModuleWrapper moduleWrapper =
                                                                   IntellijModuleWrapper.forModule(drp.getModule())) {
                                                          moduleWrapper.updateDependencies(drp.getDependencies());
                                                      }
                                                  };
                                                  runWriteAction(updateDependencies);
                                              }
                                          });
    }

    private void runWriteAction(Runnable runnable) {
        ApplicationManager.getApplication()
                          .invokeAndWait(() -> ApplicationManager.getApplication().runWriteAction(runnable));
    }


    void reportProblems(final Module module, final List<ResolveProblem> problems) {
        ApplicationManager.getApplication().invokeLater(() -> {
            final IvyIdeaFacetConfiguration ivyIdeaFacetConfiguration = IvyIdeaFacetConfiguration.getInstance(module);
            if (ivyIdeaFacetConfiguration == null) {
                throw new RuntimeException("Internal error: module " + module.getName() + " does not seem to be have an IvyIDEA facet, but was included in the resolve process anyway.");
            }
            final ConsoleView consoleView = IntellijUtils.getConsoleView(module.getProject());
            String configsForModule;
            if (ivyIdeaFacetConfiguration.isOnlyResolveSelectedConfigs()) {
                final Set<String> configs = ivyIdeaFacetConfiguration.getConfigsToResolve();
                if (configs == null || configs.size() == 0) {
                    configsForModule = "[No configurations selected!]";
                } else {
                    configsForModule = configs.toString();
                }
            } else {
                configsForModule = "[All configurations]";
            }
            if (!problems.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("Problems for module '%s %s':\n", module.getName(), configsForModule));
                for (ResolveProblem resolveProblem : problems) {
                    sb.append(String.format("\t%s\n",resolveProblem.toString()));
                }
                consoleView.print(sb.toString(), ConsoleViewContentType.ERROR_OUTPUT);
                if(consoleView instanceof ConsoleViewImpl)
                    ((ConsoleViewImpl)consoleView).flushDeferredText();
            }
        });
    }

    String getDurationText(long startNanos, long stopNanos){
        return getDurationText(stopNanos - startNanos );
    }

    String getDurationText(long durationNanos){

        LocalDateTime time = LocalDate.now().atTime(LocalTime.ofNanoOfDay(durationNanos));
        return time.format(dateTimeFormatter);
    }
}

