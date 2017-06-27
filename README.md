# springmvc-clientgen-maven-plugin

## Build Status

| Master | Development |
|--------|-------------|
| [![Build Status](https://travis-ci.org/bluedevel/springmvc-clientgen.svg?branch=master)](https://travis-ci.org/bluedevel/springmvc-clientgen) | |

## Example
```xml
<build>
        <plugins>
            <plugin>
                <groupId>com.github.bluedevel</groupId>
                <artifactId>springmvc-clientgen-maven-plugin</artifactId>
                <version>1.0-SNAPSHOT</version>
                <executions>
                    <execution>
                        <id>first execution</id>
                        <phase>prepare-package</phase>
                        <goals>
                            <goal>generate-clients</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <controllers>
                        <controller>
                            <baseUrl>http://bla.de</baseUrl>
                            <implementation>springmvcclientgen.test.ExampleController</implementation>
                        </controller>
                        <controller>
                            <name>ExampleClient2</name>
                            <baseUrl>http://bla2.de</baseUrl>
                            <implementation>springmvcclientgen.test.ExampleController</implementation>
                            <generator>jquery</generator>
                        </controller>
                        <controller>
                            <baseUrl>http://nico.peter</baseUrl>
                            <implementation>springmvcclientgen.test.OtherExampleController</implementation>
                        </controller>
                    </controllers>
                    <!-- Default Generator -->
                    <generator>javascript</generator>
                </configuration>
            </plugin>
        </plugins>
</build>
```
