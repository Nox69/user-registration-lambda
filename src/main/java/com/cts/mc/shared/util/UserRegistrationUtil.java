package com.cts.mc.shared.util;

import java.util.UUID;

/**
 * @author bharatkumar
 *
 */
public class UserRegistrationUtil {

    private UserRegistrationUtil() {
        // Utility classes should not have public constructors (squid:S1118)
    }

    public static String generatePermamentAccessCode() {
        return UUID.randomUUID().toString();
    }

}
