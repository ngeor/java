#!/usr/bin/env -S python -u

import argparse
import os
import os.path
import subprocess
import tempfile


def main():
    args = parse_args()
    release(args)


def parse_args():
    parser = argparse.ArgumentParser(description="Creates a release of a Maven library")
    parser.add_argument("--gpg-key", help="GPG key", required=True)
    parser.add_argument("--gpg-passphrase", help="GPG passphrase", required=True)
    parser.add_argument(
        "--maven-username",
        help="The username to use for publishing the release to Maven",
        required=True,
    )
    parser.add_argument(
        "--maven-password",
        help="The password to use for publishing the release to Maven",
        required=True,
    )
    return parser.parse_args()


def release(args):
    try:
        gpg_list_keys()
        gpg_import_key(args.gpg_passphrase, "scripts/keys.asc")
        perform_release(
            args.gpg_key, args.gpg_passphrase, args.maven_username, args.maven_password
        )
    finally:
        clean_gpg(args.gpg_key)


def clean_gpg(gpg_key):
    # gpg -K | grep "^  " | tr -d " " | xargs gpg --batch --yes --delete-secret-keys
    # gpg --batch --yes --delete-key ${GPG_KEY}
    p1 = subprocess.run(
        ["gpg", "-K"], check=True, encoding="utf8", stdout=subprocess.PIPE
    )
    keys = p1.stdout.splitlines()
    for key in keys:
        if key.startswith("  "):
            subprocess.run(
                ["gpg", "--batch", "--yes", "--delete-secret-keys", key.strip()],
                check=True,
            )
    subprocess.run(["gpg", "--batch", "--yes", "--delete-key", gpg_key], check=True)


def perform_release(gpg_key, gpg_passphrase, maven_username, maven_password):
    with tempfile.TemporaryDirectory() as tmp_dir:
        settings_xml_file = os.path.join(tmp_dir, "settings.xml")
        with open(settings_xml_file, "w") as f:
            f.write(
                f"""
<settings>
    <servers>
        <server>
            <id>central</id>
            <username>{maven_username}</username>
            <password>{maven_password}</password>
        </server>
    </servers>
    <profiles>
        <profile>
            <id>gpg</id>
            <properties>
                <gpg.keyname>{gpg_key}</gpg.keyname>
                <gpg.passphrase>{gpg_passphrase}</gpg.passphrase>
            </properties>
        </profile>
    </profiles>
</settings>
            """
            )
        subprocess.run(
            [
                "mvn",
                "-B",
                "-s",
                settings_xml_file,
                # skip surefire tests
                "-DskipTests=true",
                # skip failsafe tests
                "-DskipITs=true",
                # skip checkstyle
                "-Dcheckstyle.skip=true",
                # skip jacoco
                "-Djacoco.skip=true",
                # skip invoker
                "-Dinvoker.skip=true",
                # skip spotless
                "-Dspotless.check.skip=true",
                # skip sortpom
                "-Dsort.skip=true",
                "-Pgpg",
                "deploy",
            ],
            check=True,
        )


def gpg_list_keys():
    """
    Lists the GPG keys.
    This is mainly used as a workaround to prime the gpg folders before importing the keys.
    """
    subprocess.run(["gpg", "--list-keys"], check=True)


def gpg_import_key(gpg_passphrase, key_file):
    """
    Imports the GPG key.
    Equivalent shell:
    `gpg --batch --yes --passphrase=${{ secrets.GPG_PASSPHRASE }} --output - scripts/keys.asc | gpg --batch --import`
    """
    p1 = subprocess.Popen(
        [
            "gpg",
            "--batch",
            "--yes",
            f"--passphrase={gpg_passphrase}",
            "--output",
            "-",
            key_file,
        ],
        stdout=subprocess.PIPE,
    )
    p2 = subprocess.Popen(
        ["gpg", "--batch", "--import"], stdin=p1.stdout, stdout=subprocess.STDOUT
    )
    p1.stdout.close()  # Allow p1 to receive a SIGPIPE if p2 exits.
    p2.communicate()
    if p2.returncode != 0:
        raise ValueError("Could not import GPG key")


if __name__ == "__main__":
    main()
