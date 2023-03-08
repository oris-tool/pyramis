package it.unifi.hierarchical.model;

/**
 * Represents the type of region between ENDING (does not contain any cycle and has a final location) and NEVERENDING
 * (contains a cycle and has no final location)
 */
public enum RegionType {
    ENDING,
    NEVERENDING
}
