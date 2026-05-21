package com.project.cmb.constant;

/**
 * Centralised order status strings — use these instead of magic literals.
 * Values must match the database column exactly.
 */
public final class OrderStatus {

    public static final String IN_PROCESS = "In Process";
    public static final String SHIPPED    = "Shipped";
    public static final String CANCELLED  = "Cancelled";
    public static final String ON_HOLD    = "On Hold";
    public static final String DISPUTED   = "Disputed";
    public static final String RESOLVED   = "Resolved";

    private OrderStatus() {}
}
