package com.github.ngeor.yak4jcli;

import com.github.ngeor.yak4jdom.ElementWrapper;

/**
 * A wrapper around the {@code parent} element of a pom file.
 */
public class PomParentElement implements HasCoordinates {
    private final ElementWrapper element;

    public PomParentElement(ElementWrapper element) {
        this.element = element;
    }

    @Override
    public String getGroupId() {
        return element.firstElementText("groupId");
    }

    @Override
    public String getArtifactId() {
        return element.firstElementText("artifactId");
    }

    public String getVersion() {
        return element.firstElementText("version");
    }

    /**
     * Sets the version of the parent pom.
     */
    public void setVersion(String version) {
        ElementWrapper versionElement = element.firstElement("version").orElseThrow(
            () -> new IllegalArgumentException("Could not find version element")
        );
        versionElement.setTextContent(version);
    }
}
