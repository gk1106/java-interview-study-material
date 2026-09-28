package com.gk.study.streams.model;

/**
 * A bank customer.
 *
 * @param id      unique customer id, e.g. "C1"
 * @param name    display name
 * @param city    home branch city
 * @param segment RETAIL, PREMIUM or CORPORATE
 */
public record Customer(String id, String name, String city, CustomerSegment segment) {
}
