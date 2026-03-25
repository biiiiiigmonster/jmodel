package io.github.biiiiiigmonster.config;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import io.github.biiiiiigmonster.Model;

import java.util.List;

/**
 * Jackson Module that automatically excludes all properties declared in {@link Model}
 * from JSON serialization. Subclass business properties are not affected.
 */
public class JmodelJacksonModule extends SimpleModule {

    public JmodelJacksonModule() {
        super("jmodel");
        setSerializerModifier(new ModelBeanSerializerModifier());
    }

    static class ModelBeanSerializerModifier extends BeanSerializerModifier {
        @Override
        public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
            if (Model.class.isAssignableFrom(beanDesc.getBeanClass())) {
                beanProperties.removeIf(prop ->
                        prop.getMember().getDeclaringClass() == Model.class
                );
            }
            return beanProperties;
        }
    }
}
