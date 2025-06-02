package com.rasit.brokage.utility;

import com.rasit.brokage.rest.exception.CustomException;
import com.rasit.brokage.rest.security.UserDetailsImpl;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.time.format.DateTimeFormatter;

@Slf4j
@NoArgsConstructor
public class BrokageUtil {
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    /**
     * Checks if the given UserDetails object has an "ADMIN" role.
     * This method looks for "ADMIN" authority.
     *
     * @param userDetails The authenticated user's details.
     * @return true if the user has an ADMIN role, false otherwise.
     */
    public static boolean isAdmin(UserDetailsImpl userDetails) {
        return userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
    }


    /**
     * Resolves the target customer ID based on the authenticated user's roles
     * and the provided X-Customer-ID header.
     *
     * @param userDetails      The authenticated user's details.
     * @param customerIdHeader The value of the X-Customer-ID header, if present.
     * @return The determined customer ID for the operation.
     * @throws CustomException If an admin user does not provide the X-Customer-ID header.
     */
    public static String resolveCustomerId(UserDetailsImpl userDetails, String customerIdHeader) throws CustomException {
        boolean isAdminUser = isAdmin(userDetails);

        if (isAdminUser) {
            if (customerIdHeader == null || customerIdHeader.isBlank()) {
                throw new CustomException(ErrorMessageType.X_CUSTOMER_HEADER_NOT_FOUND, new String[]{}, HttpStatus.BAD_REQUEST);
            }
            return customerIdHeader;
        } else {
            if (customerIdHeader != null && !customerIdHeader.isBlank() && !customerIdHeader.equals(userDetails.getId().toString())) {
                log.warn("Non-admin user (ID: {}) attempted to set X-Customer-ID header to '{}'. This header is ignored for non-admin roles; own ID will be used.", userDetails.getId(), customerIdHeader);
            }
            return userDetails.getId().toString();
        }
    }
}
