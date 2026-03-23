package com.github.ngeor;

public record SemVer(int major, int minor, int patch, String suffix) {
    public SemVer {
        if (major < 0) {
            throw new IllegalArgumentException("Major version cannot be negative");
        }
        if (minor < 0) {
            throw new IllegalArgumentException("Minor version cannot be negative");
        }
        if (patch < 0) {
            throw new IllegalArgumentException("Patch version cannot be negative");
        }
        if (suffix != null && suffix.isBlank()) {
            // suffix can be either null or non-blank, it cannot be blank
            throw new IllegalArgumentException("Version suffix cannot be blank");
        }
    }

    @Override
    public String toString() {
        if (suffix != null) {
            return major + "." + minor + "." + patch + "-" + suffix;
        } else {
            return major + "." + minor + "." + patch;
        }
    }

    public static SemVer parse(String version) {
        Validate.requireNotBlank(version, "version cannot be empty");
        String[] parts = version.split("\\.", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid version: " + version);
        }

        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        String[] finalParts = parts[2].split("-", 2);
        int patch = Integer.parseInt(finalParts[0]);
        if (finalParts.length == 1) {
            return new SemVer(major, minor, patch, null);
        } else {
            String suffix = finalParts[1];
            Validate.requireNotBlank(suffix, "version suffix cannot be blank");
            return new SemVer(major, minor, patch, suffix);
        }
    }
}
