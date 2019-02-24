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
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.intellij.facet.config.IvyIdeaFacetConfiguration;
import org.clarent.ivyidea.intellij.model.IntellijModuleWrapper;
import org.clarent.ivyidea.resolve.problem.ResolveProblem;

import java.util.List;
import java.util.Set;

/**
 * @author Guy Mahieu
 */
public abstract class AbstractResolveAction extends AnAction {

    protected void updateIntellijModel(final DependencyResolutionPackage... packages) {
        for (DependencyResolutionPackage drp : packages) {
            ApplicationManager.getApplication().invokeAndWait(() -> ApplicationManager.getApplication()
                                                                                      .runWriteAction(() -> {
                                                                                          try (IntellijModuleWrapper moduleWrapper = IntellijModuleWrapper
                                                                                                  .forModule(drp.getModule())) {
                                                                                              moduleWrapper.updateDependencies(drp.getDependencies());
                                                                                          }
                                                                                      }));
        }
    }

    protected void reportProblems(final Module module, final List<ResolveProblem> problems) {
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
                consoleView.print("Problems for module '" + module.getName() + " " + configsForModule + "':" + '\n', ConsoleViewContentType.NORMAL_OUTPUT);
                for (ResolveProblem resolveProblem : problems) {
                    consoleView.print("\t" + resolveProblem.toString() + '\n', ConsoleViewContentType.ERROR_OUTPUT);
                }
                // Make sure the toolwindow becomes visible if there were problems
                IntellijUtils.getToolWindow(module.getProject()).show(null);
            }
        });
    }

    protected String getDurationText(long startNanos, long stopNanos){
        return getDurationText(stopNanos - startNanos );
    }

    protected String getDurationText(long durationNanos){
        long tempMsSec = durationNanos/(1000*1000);
        long ms = tempMsSec % 1000;
        long sec = tempMsSec % 60*1000;
        long min = (tempMsSec /60*1000) % 60;
        long hour = (tempMsSec /(60*60*1000)) % 24;
        long day = (tempMsSec / (24*60*60*1000)) % 24;
        return String.format("%d:%d:%d.%d", hour,min,sec, ms);
    }
}

