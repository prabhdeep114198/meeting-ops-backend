package com.meetingops.application.util;

import com.meetingops.domain.enumeration.UserRole;

import java.util.UUID;

/**
 * Holder for managing ThreadLocal tenant context in Spring microservices.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public final class TenantContextHolder {

    private static final ThreadLocal<TenantContext> CONTEXT = new ThreadLocal<>();

    private TenantContextHolder() {
    }

    public static void setContext(TenantContext context) {
        CONTEXT.set(context);
    }

    public static TenantContext getContext() {
        return CONTEXT.get();
    }

    public static UUID getOrganizationId() {
        TenantContext ctx = CONTEXT.get();
        if (ctx != null && ctx.getOrganizationId() != null) {
            return ctx.getOrganizationId();
        }
        try {
            return SecurityContextHolder.getCurrentOrganizationId();
        } catch (Exception e) {
            return null;
        }
    }

    public static UUID getUserId() {
        TenantContext ctx = CONTEXT.get();
        if (ctx != null && ctx.getUserId() != null) {
            return ctx.getUserId();
        }
        try {
            return SecurityContextHolder.getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }

    public static UserRole getUserRole() {
        TenantContext ctx = CONTEXT.get();
        if (ctx != null && ctx.getUserRole() != null) {
            return ctx.getUserRole();
        }
        try {
            String roleStr = SecurityContextHolder.getCurrentUserRole();
            return roleStr != null ? UserRole.valueOf(roleStr.toUpperCase()) : UserRole.PARTICIPANT;
        } catch (Exception e) {
            return UserRole.PARTICIPANT;
        }
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
