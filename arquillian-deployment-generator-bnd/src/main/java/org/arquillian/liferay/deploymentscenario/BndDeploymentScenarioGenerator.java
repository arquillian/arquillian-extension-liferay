/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
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

package org.arquillian.liferay.deploymentscenario;

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Jar;
import org.arquillian.liferay.deploymentscenario.annotations.BndFile;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.impl.client.deployment.AnnotationDeploymentScenarioGenerator;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentScenarioGenerator;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.osgi.api.BndProjectBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author Carlos Sierra Andrés
 */
public class BndDeploymentScenarioGenerator implements DeploymentScenarioGenerator {

    @Inject
    Instance<Injector> injector;

    public static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";

    private File bndFile = new File("bnd.bnd");

    private File commonBndFile;

    public BndDeploymentScenarioGenerator() {
        String sdkDir = System.getProperty("sdk.dir");

        if (sdkDir != null && !sdkDir.isEmpty()) {
            commonBndFile = new File(sdkDir, "common.bnd");
        }
    }

    public File getBndFile(TestClass testClass) {
        if (testClass.isAnnotationPresent(BndFile.class)) {
            BndFile annotation = testClass.getAnnotation(BndFile.class);

            return new File(annotation.value());
        }

        return bndFile;
    }

    public void setBndFile(File bndFile) {
        this.bndFile = bndFile;
    }

    @Override
	public List<DeploymentDescription> generate(TestClass testClass) {
		ArrayList<DeploymentDescription> deployments = new ArrayList<>();

        DeploymentScenarioGenerator defaultDeploymentScenarioGenerator = getDefaultDeploymentScenarioGenerator();

        if (defaultDeploymentScenarioGenerator != null) {
            List<DeploymentDescription> annotationDeployments = defaultDeploymentScenarioGenerator.generate(testClass);

            if (annotationDeployments != null && !annotationDeployments.isEmpty()) {
                return annotationDeployments;
            }
        }

        try {
            bndFile = getBndFile(testClass);

            BndProjectBuilder bndProjectBuilder = ShrinkWrap.create(BndProjectBuilder.class);

            bndProjectBuilder.setBndFile(bndFile);

            bndProjectBuilder.generateManifest(true);

            File commonBndFile = getCommonBndFile();

            if (commonBndFile != null) {

                bndProjectBuilder.addProjectPropertiesFile(commonBndFile);
            }

            JavaArchive javaArchive = bndProjectBuilder.as(JavaArchive.class);

            javaArchive.addClass(BndFile.class);

            Analyzer analyzer = new Analyzer();

            Properties analyzerProperties = new Properties();

            if (commonBndFile != null) {
                analyzerProperties.putAll(analyzer.loadProperties(commonBndFile));
            }

            analyzerProperties.putAll(analyzer.loadProperties(bndFile));

            analyzer.setProperties(analyzerProperties);

            boolean testable = isTestable(testClass);

            if (testable) {
                addTestClass(testClass, javaArchive);

                //FIXME: Is this still needed with latest version of arquillian-osgi-bundle?
                fixExportPackage(testClass, analyzer);
            }

            ZipExporter zipExporter = javaArchive.as(ZipExporter.class);

            Jar jar = new Jar(javaArchive.getName(), zipExporter.exportAsInputStream());

            analyzer.setJar(jar);

            DeploymentDescription deploymentDescription = new DeploymentDescription(javaArchive.getName(), javaArchive);

			deploymentDescription.
                    shouldBeTestable(testable).
                    shouldBeManaged(true);

			deployments.add(deploymentDescription);

            Manifest firstPassManifest = new Manifest(javaArchive.get(MANIFEST_PATH).getAsset().openStream());

            firstPassManifest.getMainAttributes().remove(new Attributes.Name("Import-Package"));

            analyzer.mergeManifest(firstPassManifest);

            Manifest manifest = analyzer.calcManifest();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            manifest.write(baos);

            ByteArrayAsset byteArrayAsset = new ByteArrayAsset(
                    baos.toByteArray());

            replaceManifest(javaArchive, byteArrayAsset);

			return deployments;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

    private boolean isTestable(TestClass testClass) {
        return !testClass.isAnnotationPresent(RunAsClient.class);
    }

    public File getCommonBndFile() {
        return commonBndFile;
    }

    public void setCommonBndFile(File commonBndFile) {
        this.commonBndFile = commonBndFile;
    }

    protected DeploymentScenarioGenerator getDefaultDeploymentScenarioGenerator() {
        //FIXME: is there a way to request a specific service, not an interface?
        AnnotationDeploymentScenarioGenerator annotationDeploymentScenarioGenerator = new AnnotationDeploymentScenarioGenerator();
        annotationDeploymentScenarioGenerator = injector.get().inject(annotationDeploymentScenarioGenerator);
        return annotationDeploymentScenarioGenerator;
    }

    private void fixExportPackage(TestClass testClass, Analyzer analyzer) {
        String exportPackage = analyzer.getProperty("Export-Package");

        if ((exportPackage == null) || exportPackage.isEmpty()) {
            exportPackage = testClass.getJavaClass().getPackage().getName();
        }
        else {
            exportPackage += "," + testClass.getJavaClass().getPackage().getName();
        }

        analyzer.setProperty("Export-Package", exportPackage);
    }

    private void addTestClass(TestClass testClass, JavaArchive javaArchive) {
        Class<?> javaClass = testClass.getJavaClass();

        Class superClass = javaClass;

        //FIXME: This can give us trouble if a test superclass is being/needs to be imported from another bundle?
        while(superClass != Object.class) {
            javaArchive.addClass(superClass);
            superClass = superClass.getSuperclass();
        }
    }

    private void replaceManifest(
            Archive<?> archive, ByteArrayAsset byteArrayAsset) {

        archive.delete(MANIFEST_PATH);

        archive.add(byteArrayAsset, MANIFEST_PATH);
    }

}
