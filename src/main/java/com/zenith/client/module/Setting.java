package com.zenith.client.module;

public abstract class Setting {
    public final String name;
    protected Setting(String name) { this.name = name; }

    public static class Bool extends Setting {
        public boolean value;
        public float anim;
        public Bool(String name, boolean v) { super(name); value = v; anim = v ? 1f : 0f; }
    }

    public static class Num extends Setting {
        public double value, min, max, step;
        public Num(String name, double v, double min, double max, double step) {
            super(name); this.min = min; this.max = max; this.step = step; this.value = v;
        }
        public void set(double v) {
            v = Math.max(min, Math.min(max, v));
            if (step > 0) v = Math.round((v - min) / step) * step + min;
            value = Math.max(min, Math.min(max, Math.round(v * 1000.0) / 1000.0));
        }
    }
}
