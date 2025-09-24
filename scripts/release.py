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
    parser.add_argument("--gpg-key-file", help="Location of the GPG key", required=True)
    parser.add_argument("--gpg-key-name", help="GPG key name", required=True)
    parser.add_argument("--gpg-passphrase", help="GPG passphrase", required=True)
    parser.add_argument(
        "--maven-username",
        help="Maven Central repository username",
        required=True,
    )
    parser.add_argument(
        "--maven-password",
        help="Maven Central repository password",
        required=True,
    )
    parser.add_argument("-f", "--file", help="Location of pom.xml file", required=False)

    return parser.parse_args()


def release(args):
    try:
        gpg_list_keys()
        gpg_import_key(args.gpg_passphrase, args.gpg_key_file)
        perform_release(args)
    finally:
        clean_gpg(args.gpg_key_name)


def clean_gpg(gpg_key_name):
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
    subprocess.run(
        ["gpg", "--batch", "--yes", "--delete-key", gpg_key_name], check=True
    )


def perform_release(args):
    with tempfile.TemporaryDirectory() as tmp_dir:
        settings_xml_file = os.path.join(tmp_dir, "settings.xml")
        with open(settings_xml_file, "w") as f:
            f.write(
                f"""
<settings>
    <servers>
        <server>
            <id>central</id>
            <username>{args.maven_username}</username>
            <password>{args.maven_password}</password>
        </server>
    </servers>
</settings>
            """
            )
        # create a new env dictionary to hold the gpg passphrase
        # as per https://maven.apache.org/plugins/maven-gpg-plugin/sign-mojo.html
        # that is the best practice and the gpg.passphrase property is deprecated
        new_env = os.environ.copy()
        new_env["MAVEN_GPG_PASSPHRASE"] = args.gpg_passphrase
        maven_args = [
            "mvn",
            "-ntp",
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
            f"-Dgpg.keyname={args.gpg_key_name}",
        ]
        if args.file:
            maven_args += ["-f", args.file]
        maven_args += ["deploy"]
        subprocess.run(
            maven_args,
            check=True,
            env=new_env,
        )


def gpg_list_keys():
    """
    Lists the GPG keys.
    This is mainly used as a workaround to prime the gpg folders before importing the keys.
    """
    subprocess.run(["gpg", "--list-keys"], check=True)


def gpg_import_key(gpg_passphrase, gpg_key_file):
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
            gpg_key_file,
        ],
        stdout=subprocess.PIPE,
    )
    p2 = subprocess.Popen(
        ["gpg", "--batch", "--import", "--quiet"],
        stdin=p1.stdout,
        stdout=subprocess.PIPE,
    )
    p1.stdout.close()  # Allow p1 to receive a SIGPIPE if p2 exits.
    p2.communicate()
    if p2.returncode != 0:
        raise ValueError("Could not import GPG key")


if __name__ == "__main__":
    main()
