package com.zenith.client.module;

public enum Category {
    INFO("HUD Info"), PVP("PvP"), VISUAL("Visual"), UTILITY("Utility"), SETTINGS("Settings");
    public final String label;
    Category(String l) { label = l; }
}
