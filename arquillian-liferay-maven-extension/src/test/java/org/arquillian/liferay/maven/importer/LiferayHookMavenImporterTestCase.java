
package org.arquillian.liferay.maven.importer;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter;

public class LiferayHookMavenImporterTestCase extends LiferayPluginTestCase {

    @BeforeClass
    public static void setupPortal() throws IOException {
        if (!setup) {
            setupPortalMinimal();
        }
    }

    @Before
    public void cleanTarget() {
        new File("src/it/demo-hook/target").delete();
    }

    @Test
    public void importWar() {
        // When
        final WebArchive archive =
            doImport("src/it/demo-hook/pom.xml");

        // Then
        assertNotNull(archive.get(ArchivePaths.create(
            "/WEB-INF/lib", "util-java.jar")));
        assertNotNull(archive.get(ArchivePaths.create(
            "/WEB-INF/lib", "commons-logging.jar")));
        assertNotNull(archive.get(ArchivePaths.create(
            "/WEB-INF/lib", "log4j-extras.jar")));
        assertNotNull(archive.get(ArchivePaths.create(
            "/WEB-INF", "liferay-hook.xml")));
        assertNotNull(archive.get(ArchivePaths.create(
            "/WEB-INF/classes", "log4j.properties")));

    }

    private WebArchive doImport(String pomFile) {

        try {

            // When
            WebArchive archive =
                ShrinkWrap.create(MavenImporter.class).loadPomFromFile(pomFile).importBuildOutput()
                    .as(WebArchive.class);

            return archive;
        }
        catch (Exception e) {
           throw new RuntimeException(e);
        }
    }
}
