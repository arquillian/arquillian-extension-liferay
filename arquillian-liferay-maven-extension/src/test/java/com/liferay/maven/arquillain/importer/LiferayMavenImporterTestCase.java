
package com.liferay.maven.arquillain.importer;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter;

public class LiferayMavenImporterTestCase {

    @Before
    public void cleanTarget() throws IOException {
        new File("src/it/demo-hook/target").delete();

    }

    @Test
    public void importWar() {
        // When
        final WebArchive archive =
            doImport("src/it/demo-hook/pom.xml");

        archive.as(ZipExporter.class).exportTo(
            new File("target/liferay-demo-hook.war"), true);

    }

    private WebArchive doImport(String pomFile) {

        try {
            System.out.println(new File(pomFile).getAbsolutePath());

            // When
            WebArchive archive =
                ShrinkWrap.create(MavenImporter.class).loadPomFromFile(pomFile).importBuildOutput()
                    .as(WebArchive.class);

            return archive;
        }
        catch (Exception e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
        }
        return null;
    }
}
