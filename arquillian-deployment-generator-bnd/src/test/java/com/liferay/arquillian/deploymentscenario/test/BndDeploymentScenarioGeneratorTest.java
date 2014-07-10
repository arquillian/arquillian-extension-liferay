/**
 * Copyright (c) 2000-2014 Liferay, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liferay.arquillian.deploymentscenario.test;

import com.liferay.arquillian.deploymentscenario.BndDeploymentScenarioGenerator;
import com.liferay.arquillian.test.ATest;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentScenarioGenerator;
import org.jboss.arquillian.test.spi.TestClass;
import static org.junit.Assert.*;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author Carlos Sierra Andrés
 */
public class BndDeploymentScenarioGeneratorTest {

    @Test
    public void testBundleGeneration() throws IOException {
        BndDeploymentScenarioGenerator bndDeploymentScenarioGenerator = new BndDeploymentScenarioGenerator() {
            @Override
            protected DeploymentScenarioGenerator getDefaultDeploymentScenarioGenerator() {
                return null;
            }
        };

        bndDeploymentScenarioGenerator.setBndFile(new File("target/test-classes/test.bnd"));
        List<DeploymentDescription> deploymentDescriptions = bndDeploymentScenarioGenerator.generate(new TestClass(ATest.class));

        assertEquals(1, deploymentDescriptions.size());

        DeploymentDescription deploymentDescription = deploymentDescriptions.get(0);

        Archive<?> archive = deploymentDescription.getArchive();

        Node object = archive.get("META-INF/MANIFEST.MF");

        assertNotNull("We must have a MANIFEST.MF", object);

        Manifest manifest = new Manifest(object.getAsset().openStream());

        Attributes mainAttributes = manifest.getMainAttributes();

        String exportPackageValue = mainAttributes.getValue("Export-Package");

        assertTrue("Package form test class must be exported",
                        exportPackageValue.contains("com.liferay.arquillian.test"));

        String importPackageValue = mainAttributes.getValue("Import-Package");

        assertFalse("Package from the classes must not be imported",
                importPackageValue.contains("com.liferay.arquillian.test.extras.a"));
        assertFalse("Package from the classes must not be imported",
                importPackageValue.contains("com.liferay.arquillian.test.extras.b"));

        assertNotNull("Classes must be included",
                archive.get("com/liferay/arquillian/test/extras/a/A.class"));
        assertNotNull("Classes must be included",
                archive.get("com/liferay/arquillian/test/extras/b/B.class"));

    }
}
