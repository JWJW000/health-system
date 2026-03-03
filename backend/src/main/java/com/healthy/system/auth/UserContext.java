package com.healthy.system.auth;

public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        return user != null ? user.getId() : null;
    }

    public static String getRole() {
        LoginUser user = HOLDER.get();
        return user != null ? user.getRole() : null;
    }

    public static void clear() {
        HOLDER.remove();
    }
}

