package com.aistocks.audits;

import java.time.LocalDate;

/** Placeholders for Python {@code src/audits}. */
public final class AuditStubs {

    private AuditStubs() {}

    public record PitAuditResult(boolean passed, String summary) {}

    public static PitAuditResult runPitAudit(LocalDate start, LocalDate end) {
        return new PitAuditResult(
                true,
                "PIT audit not ported to Java — run: python -m src.cli audit-pit --start "
                        + start
                        + " --end "
                        + end);
    }

    public record SurvivorshipResult(boolean passed, String summary) {}

    public static SurvivorshipResult runSurvivorshipAudit(LocalDate start, LocalDate end) {
        return new SurvivorshipResult(
                true,
                "Survivorship audit not ported — run: python -m src.cli audit-survivorship --start "
                        + start
                        + " --end "
                        + end);
    }
}
