package com.github.ngeor;

public record SemVer(int major, int minor, int patch, String suffix) implements Comparable<SemVer> {
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

    @Override
    public int compareTo(SemVer other) {
        if (major != other.major) {
            return Integer.compare(major, other.major);
        }
        if (minor != other.minor) {
            return Integer.compare(minor, other.minor);
        }
        if (patch != other.patch) {
            return Integer.compare(patch, other.patch);
        }
        if (suffix == null && other.suffix == null) {
            return 0;
        }
        if (suffix == null) {
            return 1;
        }
        if (other.suffix == null) {
            return -1;
        }
        return suffix.compareTo(other.suffix);
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

    public SemVer bump(SemVerBump semVerBump) {
        return switch (semVerBump) {
            case MAJOR -> new SemVer(major + 1, 0, 0, suffix);
            case MINOR -> new SemVer(major, minor + 1, 0, suffix);
            case PATCH -> new SemVer(major, minor, patch + 1, suffix);
        };
    }

    public SemVer withSuffix(String suffix) {
        return new SemVer(major, minor, patch, suffix);
    }
}
