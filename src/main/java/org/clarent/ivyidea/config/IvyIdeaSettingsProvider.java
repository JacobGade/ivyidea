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

package org.clarent.ivyidea.config;

import com.intellij.openapi.module.Module;
import com.intellij.util.net.HttpConfigurable;
import org.apache.ivy.core.settings.IvySettings;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class IvyIdeaSettingsProvider {
    private static final HashMap<String,IvySettings> settingsMap = new HashMap<>();

    @NotNull
    public IvySettings createConfiguredIvySettings(Module module) throws IvySettingsNotFoundException, IvySettingsFileReadException {
        return createConfiguredIvySettings(module, IvyIdeaConfigHelper.getIvySettingsFile(module), IvyIdeaConfigHelper.getIvyProperties(module));
    }

    @NotNull
    public IvySettings createConfiguredIvySettings(Module module, @Nullable String settingsFile, Properties properties) throws IvySettingsFileReadException {
        String settingsKey = settingsFile + properties.hashCode();
        if(settingsMap.containsKey(settingsKey))
            return settingsMap.get(settingsKey);
        IvySettings s = new IvySettings();
        injectProperties(s, module, properties); // inject our properties; they may be needed to parse the settings file

        try {
            if (!StringUtils.isBlank(settingsFile)) {
                if (settingsFile.startsWith("http://") || settingsFile.startsWith("https://")) {
                    HttpConfigurable.getInstance().prepareURL(settingsFile);
                    s.load(new URL(settingsFile));
                } else if (settingsFile.startsWith("file://")) {
                    s.load(new URL(settingsFile));
                } else {
                    s.load(new File(settingsFile));
                }
            } else {
                s.loadDefault();
            }
        } catch (ParseException e) {
            throw new IvySettingsFileReadException(settingsFile, module.getName(), e);
        } catch (IOException e) {
            throw new IvySettingsFileReadException(settingsFile, module.getName(), e);
        }

        // re-inject our properties; they may overwrite some properties loaded by the settings file
        for (Map.Entry<Object,Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            // we first clear the property to avoid possible cyclic-variable errors (cfr issue 95)
            s.setVariable(key, null);
            s.setVariable(key, value);
        }
        settingsMap.put(settingsKey, s);
        return s;
    }

    private static void injectProperties(IvySettings ivySettings, Module module, Properties properties) {
        // By default, we use the module root as basedir (can be overridden by properties injected below)
        fillDefaultBaseDir(ivySettings, module);
        fillSettingsVariablesWithProperties(ivySettings, properties);
    }

    private static void fillSettingsVariablesWithProperties(IvySettings ivySettings, Properties properties) {
        @SuppressWarnings("unchecked")
        final Enumeration<String> propertyNames = (Enumeration<String>) properties.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = propertyNames.nextElement();
            ivySettings.setVariable(propertyName, properties.getProperty(propertyName));
        }
    }

    private static void fillDefaultBaseDir(IvySettings ivySettings, Module module) {
        final File moduleFileFolder = new File(module.getModuleFilePath()).getParentFile();
        if (moduleFileFolder != null) {
            ivySettings.setBaseDir(moduleFileFolder.getAbsoluteFile());
        }
    }
}
