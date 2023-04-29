package org.tommy.core.key;

import java.util.Objects;
import java.util.StringJoiner;
public class CompositeKey implements Key {

    private String key;

    @Override
    public String toString() {
        return "CompositeKey{" +
                "key='" + key + '\'' +
                '}';
    }

    public  CompositeKey(Object... keyList) {
        StringJoiner stringJoiner = new StringJoiner(",");
        for (Object k : keyList) {
            Objects.requireNonNull(k);
            String string = k.toString();
            stringJoiner.add(string);
        }
        this.key = stringJoiner.toString();
    }

    @Override
    public String getKeyValue() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompositeKey that = (CompositeKey) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
