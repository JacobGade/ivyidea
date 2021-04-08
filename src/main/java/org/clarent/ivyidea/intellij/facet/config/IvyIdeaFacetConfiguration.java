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

package org.clarent.ivyidea.intellij.facet.config;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.module.Module;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.XCollection;
import org.clarent.ivyidea.intellij.facet.IvyIdeaFacet;
import org.clarent.ivyidea.intellij.facet.ui.BasicSettingsTab;
import org.clarent.ivyidea.intellij.facet.ui.PropertiesSettingsTab;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Guy Mahieu
 */
@State(name= "configuration")
public class IvyIdeaFacetConfiguration implements FacetConfiguration, PersistentStateComponent<IvyIdeaFacetConfiguration> {

    private static final Logger LOGGER = Logger.getLogger(IvyIdeaFacetConfiguration.class.getName());

    /*
        Al the fields are initialized with a default value to avoid errors when adding a new IvyIDEA facet to an
        existing module.
    */
    @Attribute
    private String ivyFile = "";
    @Attribute
    private boolean useProjectSettings = true;
    @Attribute
    private boolean useCustomIvySettings = true;
    @Attribute
    private String ivySettingsFile = "";
    @Attribute
    private boolean onlyResolveSelectedConfigs = false;

    @XCollection(elementName = "configsToResolve", propertyElementName = "config", valueAttributeName = "")
    private Set<String> configsToResolve = Collections.emptySet();

    @Property(surroundWithTag = false)
    private org.clarent.ivyidea.intellij.facet.config.propertiesSettings propertiesSettings = new propertiesSettings();

    @Nullable
    public static IvyIdeaFacetConfiguration getInstance(Module module) {
        final IvyIdeaFacet ivyIdeaFacet = IvyIdeaFacet.getInstance(module);
        if (ivyIdeaFacet != null) {
            return ivyIdeaFacet.getConfiguration();
        } else {
            LOGGER.info("Module " + module.getName() + " does not have the IvyIDEA facet configured; ignoring.");
            return null;
        }
    }

    @NotNull
    public String getIvyFile() {
        return ivyFile;
    }

    public void setIvyFile(@NotNull String ivyFile) {
        this.ivyFile = ivyFile;
    }

    public boolean isUseProjectSettings() {
        return useProjectSettings;
    }

    public void setUseProjectSettings(boolean useProjectSettings) {
        this.useProjectSettings = useProjectSettings;
    }

    public boolean isUseCustomIvySettings() {
        return useCustomIvySettings;
    }

    public void setUseCustomIvySettings(boolean useCustomIvySettings) {
        this.useCustomIvySettings = useCustomIvySettings;
    }

    @NotNull
    public String getIvySettingsFile() {
        return ivySettingsFile;
    }

    public void setIvySettingsFile(@NotNull String ivySettingsFile) {
        this.ivySettingsFile = ivySettingsFile;
    }

    public boolean isOnlyResolveSelectedConfigs() {
        return onlyResolveSelectedConfigs;
    }

    public void setOnlyResolveSelectedConfigs(boolean onlyResolveSelectedConfigs) {
        this.onlyResolveSelectedConfigs = onlyResolveSelectedConfigs;
    }

    public Set<String> getConfigsToResolve() {
        return configsToResolve;
    }

    public void setConfigsToResolve(Set<String> configsToResolve) {
        this.configsToResolve = configsToResolve;
    }

    public org.clarent.ivyidea.intellij.facet.config.propertiesSettings getPropertiesSettings() {
        return propertiesSettings;
    }

    public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        final PropertiesSettingsTab propertiesSettingsTab = new PropertiesSettingsTab(editorContext);
        final BasicSettingsTab basicSettingsTab = new BasicSettingsTab(editorContext, propertiesSettingsTab);
        return new FacetEditorTab[]{basicSettingsTab, propertiesSettingsTab};
    }

    @NotNull
    public IvyIdeaFacetConfiguration getState() {
        return this;
    }

    public void loadState(@NotNull IvyIdeaFacetConfiguration state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
