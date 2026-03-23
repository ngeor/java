package com.github.ngeor;

import java.util.Optional;
import org.immutables.value.Value;

@Value.Builder
public record MavenPrepareOptions(
        String developmentVersion,
        String releaseVersion,
        String tag,
        @Value.Default.Boolean(false) boolean pushChanges,
        @Value.Default.String("validate") String completionGoals,
        Optional<String> checkModificationExcludeList) {}
