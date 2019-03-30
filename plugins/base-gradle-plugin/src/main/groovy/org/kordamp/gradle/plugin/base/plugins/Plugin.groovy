/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugin.devel.PluginDeclaration
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.16.0
 */
@CompileStatic
@Canonical
class Plugin extends AbstractFeature {
    private static final String PLUGIN_ID = 'org.kordamp.gradle.plugin'

    String id
    String implementationClass
    List<String> tags = []

    private final String pluginName

    Plugin(Project project) {
        super(project)
        doSetEnabled(project.plugins.findPlugin(PLUGIN_ID) != null)

        pluginName = project.name - '-gradle' - 'gradle-' - '-plugin' + 'Plugin'
    }

    String getPluginName() {
        pluginName
    }

    void setId(String id) {
        this.id = id

        GradlePluginDevelopmentExtension gpde = project.extensions.findByType(GradlePluginDevelopmentExtension)
        if (gpde) {
            PluginDeclaration pd = gpde.plugins.maybeCreate(pluginName)
            pd.id = id
        }
    }

    void setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass

        GradlePluginDevelopmentExtension gpde = project.extensions.findByType(GradlePluginDevelopmentExtension)
        if (gpde) {
            PluginDeclaration pd = gpde.plugins.maybeCreate(pluginName)
            pd.implementationClass = implementationClass
        }
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        Map map = [enabled: enabled]

        if (enabled) {
            map.pluginName = pluginName
            map.id = id
            map.implementationClass = implementationClass
            map.tags = tags
        }

        ['plugin': map]
    }

    void copyInto(Plugin copy) {
        super.copyInto(copy)
        copy.@id = id
        copy.@implementationClass = implementationClass
        copy.@tags.addAll(tags)
    }

    static void merge(Plugin o1, Plugin o2) {
        AbstractFeature.merge(o1, o2)
        o1.id = o1.id ?: o2?.id
        o1.implementationClass = o1.implementationClass ?: o2?.implementationClass
        List<String> ts = new ArrayList<>(o1.tags)
        o1.tags.clear()
        o1.tags.addAll((ts + o2?.tags).unique())
    }

    void normalize() {
        if (!enabledSet) {
            setEnabled(project.plugins.findPlugin(PLUGIN_ID) != null)
        }
    }

    List<String> validate(ProjectConfigurationExtension extension) {
        List<String> errors = []

        if (!enabled) return errors

        if (isBlank(id)) {
            errors << "[${project.name}] Plugin id is blank".toString()
        }
        if (isBlank(implementationClass)) {
            errors << "[${project.name}] Plugin implementationClass is blank".toString()
        }
        if (!tags && !extension.info.tags) {
            errors << "[${project.name}] Plugin has no tags defined".toString()
        }

        errors
    }

    List<String> resolveTags(ProjectConfigurationExtension extension) {
        tags ?: extension.info.tags
    }
}
