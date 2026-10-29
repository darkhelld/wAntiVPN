package com.wiss.wiss.wantivpn;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class CheckResult {
    private final boolean available;
    private final boolean blocked;
    private final String network;

    public static CheckResult unavailable() {
        return new CheckResult(false, false, "Не определена");
    }
}