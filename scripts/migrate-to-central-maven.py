#!/usr/bin/env python

import argparse
import os
import os.path
import shutil
import subprocess
from xml.etree import ElementTree as ET


# Align build.yaml and release.yaml from ngeor/yak4j-spring-test-utils

MAVEN_NS = "http://maven.apache.org/POM/4.0.0"
MAVEN_PREFIX = "{" + MAVEN_NS + "}"


def main():
    args = parse_args()
    ensure_maven_project(args.directory)
    ensure_maven_project(args.template)

    # namespace workaround to prevent writing namespace in the output xml
    ET.register_namespace("", MAVEN_NS)

    # Load pom.xml
    et = ET.parse(os.path.join(args.directory, "pom.xml"))

    # Get the <project> root element
    project = et.getroot()

    # Remove distributionManagement section from pom.xml
    remove_if_exists(project, f"{MAVEN_PREFIX}distributionManagement")

    # Update properties
    properties = project.find(f"{MAVEN_PREFIX}properties")
    remove_if_exists(properties, f"{MAVEN_PREFIX}nexus-staging-maven-plugin.version")
    remove_if_exists(
        properties, f"{MAVEN_PREFIX}central-publishing-maven-plugin.version"
    )

    # Plugins
    remove_old_or_new_sonatype_plugin(project)
    profiles = project.find(f"{MAVEN_PREFIX}profiles")
    for profile in profiles.findall(f"{MAVEN_PREFIX}profile"):
        remove_old_or_new_sonatype_plugin(profile)

        id = profile.find(f"{MAVEN_PREFIX}id")
        if id is not None and id.text == "gpg":
            add_new_sonatype_plugin(profile)

    # Save
    et.write(
        os.path.join(args.directory, "pom.xml"), encoding="UTF-8", xml_declaration=True
    )

    # Remove scripts files
    for file in ["keys.asc", "release.py"]:
        if os.path.isfile(os.path.join(os.path.join(args.directory, "scripts"), file)):
            os.remove(os.path.join(os.path.join(args.directory, "scripts"), file))

    # Copy workflows from template project
    source_workflows = os.path.join(os.path.join(args.template, ".github"), "workflows")
    target_workflows = os.path.join(
        os.path.join(args.directory, ".github"), "workflows"
    )
    for filename in os.listdir(source_workflows):
        shutil.copyfile(
            os.path.join(source_workflows, filename),
            os.path.join(target_workflows, filename),
        )

    # Run mvn compile in project
    subprocess.run(["mvn", "-q", "compile"], cwd=args.directory, encoding="utf8", check=True)


def remove_old_or_new_sonatype_plugin(parent):
    build = parent.find(f"{MAVEN_PREFIX}build")
    if build is None:
        return

    build_plugin_management = build.find(f"{MAVEN_PREFIX}pluginManagement")
    if build_plugin_management is not None:
        build_plugin_management_plugins = build_plugin_management.find(
            f"{MAVEN_PREFIX}plugins"
        )
        if build_plugin_management_plugins is not None:
            remove_all(
                build_plugin_management_plugins,
                f"{MAVEN_PREFIX}plugin",
                is_old_or_new_sonatype_plugin,
            )

    build_plugins = build.find(f"{MAVEN_PREFIX}plugins")
    if build_plugins is not None:
        remove_all(
            build_plugins, f"{MAVEN_PREFIX}plugin", is_old_or_new_sonatype_plugin
        )


def add_new_sonatype_plugin(parent):
    build = add_if_missing(parent, f"{MAVEN_PREFIX}build")
    plugins = add_if_missing(build, f"{MAVEN_PREFIX}plugins")
    plugin = maven_element_with_children(
        "plugin",
        maven_element_with_text("groupId", "org.sonatype.central"),
        maven_element_with_text("artifactId", "central-publishing-maven-plugin"),
        maven_element_with_text("version", "0.8.0"),
        maven_element_with_text("extensions", "true"),
        maven_element_with_children(
            "configuration",
            maven_element_with_text("autoPublish", "true"),
            maven_element_with_text("publishingServerId", "central"),
            maven_element_with_text("waitUntil", "published"),
        ),
    )

    plugins.append(plugin)


def maven_element_with_children(name, *children):
    element = ET.Element(f"{MAVEN_PREFIX}{name}")
    for child in children:
        element.append(child)
    return element


def maven_element_with_text(name, text):
    element = ET.Element(f"{MAVEN_PREFIX}{name}")
    element.text = text
    return element


def remove_if_exists(parent, element_name):
    child = parent.find(element_name)
    if child is not None:
        parent.remove(child)


def add_if_missing(parent, element_name):
    child = parent.find(element_name)
    if child is None:
        child = ET.Element(element_name)
        parent.append(child)
    return child


def remove_all(parent, element_name, predicate):
    to_delete = []
    for child in parent.findall(element_name):
        if predicate(child):
            to_delete.append(child)
    for child in to_delete:
        parent.remove(child)


def ensure_maven_project(dir):
    if not os.path.isdir(dir):
        raise ValueError(f"Not a directory: {dir}")
    if not os.path.isfile(os.path.join(dir, "pom.xml")):
        raise ValueError(f"No pom.xml found in {dir}")


def is_old_or_new_sonatype_plugin(plugin):
    group_id = plugin.find(f"{MAVEN_PREFIX}groupId")
    return group_id is not None and group_id.text.startswith("org.sonatype.")


def parse_args():
    parser = argparse.ArgumentParser(description="Migrate to Central Maven Publishing")
    parser.add_argument(
        "-d", "--directory", required=True, help="The repository to modify"
    )
    parser.add_argument(
        "-t", "--template", required=True, help="The repository to use as a template"
    )
    return parser.parse_args()


if __name__ == "__main__":
    main()
