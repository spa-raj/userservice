package com.vibevault.userservice.services.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ClientInfo {
    public static String getClientIpAddress() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }
            // For multiple proxies, first IP is the real client
            if (ipAddress != null && ipAddress.contains(",")) {
                ipAddress = ipAddress.split(",")[0].trim();
            }
            return ipAddress != null ? ipAddress : "0.0.0.0";
        } catch (Exception e) {
            return "0.0.0.0"; // Default when not in request context
        }
    }
    public static String getUserAgent() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String userAgent = request.getHeader("User-Agent");
            return userAgent != null ? userAgent : "Unknown";
        } catch (Exception e) {
            return "Unknown"; // Default when not in request context
        }
    }
}
