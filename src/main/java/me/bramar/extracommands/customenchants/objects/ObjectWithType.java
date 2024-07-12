package me.bramar.extracommands.customenchants.objects;

import java.util.Objects;

public class ObjectWithType<T> {
    public final String type;
    public final T obj;
    public ObjectWithType(String type, T obj) {
        this.type = type;
        this.obj = obj;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectWithType<?> that = (ObjectWithType<?>) o;
        return Objects.equals(type, that.type) && Objects.equals(obj, that.obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, obj);
    }
}
