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

package org.clarent.ivyidea.intellij.ui;

import com.intellij.openapi.components.ComponentManager;
import com.intellij.openapi.ui.TextComponentAccessor;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CollapsingTextComponentAccessor implements TextComponentAccessor<JTextField> {
    private ComponentManager componentManager;

    public CollapsingTextComponentAccessor(ComponentManager componentManager) {
        this.componentManager = componentManager;
    }

    @Override
    public String getText(JTextField jTextField) {
        return jTextField.getText();
    }

    @Override
    public void setText(JTextField jTextField, @NotNull String s) {
        if(componentManager != null && s != null)
            s = IntellijUtils.getRelativePathIfInProjectFolder(componentManager, s);
        jTextField.setText(s);
    }
}
