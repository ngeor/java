package com.github.ngeor;

import java.util.Optional;
import org.immutables.value.Value;

@Value.Builder
record AppOptions(
        String directory, Optional<String> developmentVersion, Optional<String> releaseVersion, Optional<String> tag) {}
