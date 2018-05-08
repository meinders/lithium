/*
 * Copyright 2013 Gerrit Meinders
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lithium.config;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;

import lithium.io.Parameter;

public class BeanConfigurationSupport implements Configurable {
    /** The supported parameters. */
    private Set<BeanParameter> parameters;

    /** The bean storing parameter values. */
    private Object bean;

    /**
     * Constructs a new configuration support for the given JavaBean.
     *
     * @param bean the JavaBean
     * @throws IntrospectionException
     */
    public BeanConfigurationSupport(DisplayNameProvider nameProvider,
            Object bean) throws IntrospectionException {
        super();
        this.bean = bean;

        parameters = new LinkedHashSet<BeanParameter>();
        BeanInfo beanInfo;
        beanInfo = Introspector.getBeanInfo(bean.getClass(), Object.class);
        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
            try {
                descriptor.setDisplayName(nameProvider
                        .getDisplayName(descriptor.getName()));
                parameters.add(new BeanParameter(descriptor));
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public Set<? extends Parameter> getParameters() {
        return Collections.unmodifiableSet(parameters);
    }

    public void setParameterValue(Parameter parameter, Object value) {
        BeanParameter beanParameter = (BeanParameter) parameter;
        beanParameter.setValue(value);
    }

    public Object getParameterValue(Parameter parameter) {
        BeanParameter beanParameter = (BeanParameter) parameter;
        return beanParameter.getValue();
    }

    private class BeanParameter extends Parameter {
        private Method readMethod;

        private Method writeMethod;

        @SuppressWarnings("unchecked")
        public BeanParameter(PropertyDescriptor descriptor)
                throws IllegalArgumentException, IllegalAccessException,
                InvocationTargetException {
            super(descriptor.getName(), descriptor.getDisplayName(),
                    (Class) descriptor.getPropertyType(), descriptor
                            .getReadMethod().invoke(bean));
            this.readMethod = descriptor.getReadMethod();
            this.writeMethod = descriptor.getWriteMethod();
        }

        public Object getValue() {
            try {
                return readMethod.invoke(bean);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public void setValue(Object value) {
            try {
                writeMethod.invoke(bean, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
