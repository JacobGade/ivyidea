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

package org.clarent.ivyidea.config.model;

import com.intellij.util.xmlb.annotations.XCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Guy Mahieu
 */
public class PropertiesSettings {

    @XCollection(elementName = "fileName",propertyElementName = "propertiesFiles", valueAttributeName = "")
    private List<String> propertiesFiles = new ArrayList<>();

    public List<String> getPropertiesFiles() {
        return propertiesFiles;
    }

    public void setPropertiesFiles(List<String> propertiesFiles) {
        this.propertiesFiles = propertiesFiles;
    }

}
