package flowforge.core.context;

import flowforge.core.annotations.Inject;

import java.lang.reflect.Field;

public class Injector {

    public static void inject(Object obj) {

        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {

            if (field.isAnnotationPresent(Inject.class)) {

                Class<?> type = field.getType();

                try {

                    Object dependency;

                    if (Context.contains(type)) {
                        dependency = Context.getBean(type);
                    } else {
                        dependency = type.getDeclaredConstructor().newInstance();
                        Context.addBean(type, dependency);
                    }

                    field.setAccessible(true);
                    field.set(obj, dependency);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}