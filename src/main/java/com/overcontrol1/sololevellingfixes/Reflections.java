package com.overcontrol1.sololevellingfixes;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class Reflections {
    private static final Unsafe UNSAFE;

    public static final MethodHandles.Lookup HANDLE;

    static {
        try {
            final Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe)theUnsafe.get(null);

            HANDLE = staticField(MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T staticField(Field field) {
        return (T) UNSAFE.getObject(UNSAFE.staticFieldBase(field), UNSAFE.staticFieldOffset(field));
    }
}
