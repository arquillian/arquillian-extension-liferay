# Arquillian Liferay Extension Example

##What is this?

This is an example of the use of the Arquillian Liferay Extension.

This example will be executed in the next environment:

* Tomcat Server 7.0.62
  * JMX enabled and configured.
  * Tomcat Manager installed and configured.
* Liferay 7.0.0
* JUnit 4.12

##Configuration Steps

### Configure Liferay Tomcat Server

#### Enable and Configure JMX in tomcat

You can follow this [guide](https://tomcat.apache.org/tomcat-7.0-doc/monitoring.html#Enabling_JMX_Remote) to enable your JMX congifuration in tomcat 

In the next example you can see a example of a **setenv** file that enable JMX in a Tomcat in the port 8099 whithout authentication:

```sh
CATALINA_OPTS="$CATALINA_OPTS -Dfile.encoding=UTF8 -Djava.net.preferIPv4Stack=true -Dorg.apache.catalina.loader.WebappClassLoader.ENABLE_CLEAR_REFERENCES=false -Duser.timezone=GMT -Xmx1024m -XX:MaxPermSize=256m"

JMX_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=8099 -Dcom.sun.management.jmxremote.ssl=false"

CATALINA_OPTS="${CATALINA_OPTS} ${JMX_OPTS}"
```

#### Install and configure Tomcat Manager

You can follow this [guide] (https://tomcat.apache.org/tomcat-7.0-doc/manager-howto.html#Introduction) to configure the Tomcat Manager.

The Tomcat Manager is installed by default on context path /manager.

```xml
<?xml version="1.0" encoding="utf-8" standalone="no"?>
<tomcat-users>
  <role rolename="tomcat"/>
  <role rolename="manager-gui"/>
  <role rolename="manager-script"/>
  <role rolename="manager-jmx"/>
  <role rolename="manager-status"/>
  <user password="tomcat" roles="tomcat,manager-gui,manager-script,manager-jmx,manager-status" username="tomcat"/>
</tomcat-users>
```

In the above example, you can see how we have created a user with the username **tomcat** and the password **tomcat** that has the roles **tomcat,manager-gui,manager-script,manager-jmx,manager-status**, these roles are mandatory to execute Arquillian test using the Arquillian Liferay Extension.

By default this extension needs that the user and password are both **tomcat**. This behaviour can be configured in a custom arquilliam.xml file. We will see how to configure the custom extension properties in other chapter.

## Create a test in Liferay with the Arquillian Liferay Extension

#### Add dependencies to pom.xml

```xml
...
	<dependencies>
	....
		<dependency>
			<groupId>org.arquillian.liferay</groupId>
			<artifactId>arquillian-container-liferay</artifactId>
			<version>1.0.0.Final-SNAPSHOT</version>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.jboss.arquillian.junit</groupId>
			<artifactId>arquillian-junit-container</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>junit</groupId>
			<artifactId>junit</artifactId>
			<version>4.12</version>
			<scope>test</scope>
		</dependency>
		....
	</dependencies>
...	
```
#### Create simple tests using the Arquillian Extension

```java
package org.arquillian.liferay.test;

import java.io.InputStream;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;

import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SimpleTest {

	@Deployment
	public static JavaArchive create() {
		final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "bundle.jar");

		archive.setManifest(new Asset() {
			@Override
			public InputStream openStream() {
				OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
				builder.addBundleSymbolicName(archive.getName());
				builder.addBundleManifestVersion(2);
				return builder.openStream();
			}
		});
		return archive;
	}

	@Test
	public void test() {
		System.out.println("Example of test executed in Liferay");
	}

}
```
