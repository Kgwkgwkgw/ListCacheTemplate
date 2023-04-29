package org.tommy.core.key;

import java.util.Objects;

public class SingleKey implements Key {
    private String key;

    public SingleKey(String key) {
        Objects.requireNonNull(key);
        this.key = key;
    }

    @Override
    public String getKeyValue() {
        return this.key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SingleKey singleKey = (SingleKey) o;
        return Objects.equals(key, singleKey.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
