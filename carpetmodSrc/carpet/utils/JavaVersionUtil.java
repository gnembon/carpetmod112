package carpet.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class JavaVersionUtil {
    public static final int JAVA_VERSION = getJavaVersion();

    private JavaVersionUtil() {}

    private static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            // old format (Java 8 and below)
            return version.charAt(2) - '0';
        } else {
            // new format (Java 9 and above)
            int dotIndex = version.indexOf('.');
            if (dotIndex == -1) {
                return Integer.parseInt(version);
            } else {
                return Integer.parseInt(version.substring(0, dotIndex));
            }
        }
    }

    public static <T> FieldAccessor<T> objectFieldAccessor(Class<?> ownerClass, String name, Class<T> fieldType) {
        Field field;
        try {
            field = ownerClass.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Could not find field", e);
        }
        if (field.getType() != fieldType) {
            throw new RuntimeException("Field has wrong type, expected \"" + fieldType.getName() + "\", got \"" + field.getType().getName() + "\"");
        }
        if (fieldType.isPrimitive()) {
            throw new RuntimeException("objectFieldAccessor does not work for primitive field types");
        }

        try {
            field.setAccessible(true);
        } catch (RuntimeException e) { // InaccessibleObjectException
            if (JAVA_VERSION <= 8) {
                throw e;
            }
            long fieldOffset = UnsafeFieldAccessor.unsafe.objectFieldOffset(field);
            return new UnsafeFieldAccessor<>(ownerClass, fieldOffset);
        }

        try {
            return new MethodHandleFieldAccessor<>(MethodHandles.lookup().unreflectGetter(field));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public interface FieldAccessor<T> {
        T get(Object instance);
    }

    private static class MethodHandleFieldAccessor<T> implements FieldAccessor<T> {
        private final MethodHandle getter;

        private MethodHandleFieldAccessor(MethodHandle getter) {
            this.getter = getter;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T get(Object instance) {
            try {
                return (T) getter.invoke(instance);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class UnsafeFieldAccessor<T> implements FieldAccessor<T> {
        private static final sun.misc.Unsafe unsafe = getUnsafe();

        private final Class<?> ownerClass;
        private final long fieldOffset;

        private UnsafeFieldAccessor(Class<?> ownerClass, long fieldOffset) {
            this.ownerClass = ownerClass;
            this.fieldOffset = fieldOffset;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T get(Object instance) {
            return (T) unsafe.getObject(ownerClass.cast(instance), fieldOffset);
        }

        private static sun.misc.Unsafe getUnsafe() {
            try {
                Field field = null;
                for (Field f : sun.misc.Unsafe.class.getDeclaredFields()) {
                    if (Modifier.isStatic(f.getModifiers()) && f.getType() == sun.misc.Unsafe.class) {
                        field = f;
                        break;
                    }
                }
                if (field == null) {
                    throw new RuntimeException("Unable to get Unsafe instance");
                }
                field.setAccessible(true);
                return (sun.misc.Unsafe) field.get(null);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Unable to get Unsafe instance", e);
            }
        }
    }
}
