#!/usr/bin/env python

"""
Create a properties files like this (case sensitive):

[Main]
Token=YOUR GITHUB TOKEN

[Variables]
VAR1=value
VAR2=value

Invoke with ./batch-secret-update.py repo-name file.properties
"""

import base64
import configparser
import nacl.encoding
import nacl.public
import requests
import sys


def main():
    repo = sys.argv[1]
    properties_file = sys.argv[2]

    config = configparser.ConfigParser()
    # this prevents configparser from storing everything in lowercase
    config.optionxform = str
    config.read(properties_file)
    token = config["Main"]["Token"]
    owner = config["Main"]["Owner"]

    # https://docs.github.com/en/rest/actions/secrets?apiVersion=2022-11-28#get-a-repository-public-key
    response = requests.get(
        f"https://api.github.com/repos/{owner}/{repo}/actions/secrets/public-key",
        headers={
            "Accept": "application/vnd.github+json",
            "Authorization": f"Bearer {token}",
            "X-GitHub-Api-Version": "2022-11-28",
        },
    )
    response.raise_for_status()
    json = response.json()
    key_id = json["key_id"]
    public_key_base64_encoded = json["key"]
    public_key = nacl.public.PublicKey(
        public_key_base64_encoded.encode("utf-8"), nacl.encoding.Base64Encoder()
    )
    sealed_box = nacl.public.SealedBox(public_key)

    for key, value in config["Variables"].items():
        encrypted = sealed_box.encrypt(value.encode("utf-8"))
        encrypted_base64 = base64.b64encode(encrypted).decode("utf-8")

        # https://docs.github.com/en/rest/actions/secrets?apiVersion=2022-11-28#create-or-update-a-repository-secret
        response = requests.put(
            f"https://api.github.com/repos/{owner}/{repo}/actions/secrets/{key}",
            json={"encrypted_value": encrypted_base64, "key_id": key_id},
            headers={
                "Accept": "application/vnd.github+json",
                "Authorization": f"Bearer {token}",
                "X-GitHub-Api-Version": "2022-11-28",
            },
        )
        response.raise_for_status()


if __name__ == "__main__":
    main()
