package de.ppi.deepsampler.persistence.bean.ext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class BeanConversion {

private final Object returnObject;
private final Type declaredReturnType;

    private BeanConversion(Object realReturnType, Type declaredReturnType) {
        this.returnObject = realReturnType;
        this.declaredReturnType = declaredReturnType;
    }

    public Class getReturnObjectType(){
        return returnObject.getClass();
    }

    public Object getReturnObject() {
        return returnObject;
    }

    public Type getDeclaredReturnType() {
        return declaredReturnType;
    }

    public ParameterizedType getGenericParameterReturnTypesOrNull() {
        return  declaredReturnType instanceof ParameterizedType ? (ParameterizedType) declaredReturnType : null;
    }

    public static class BeanConversionBuilder {
        private Object realReturnType;
        private Type declaredReturnType;

        public BeanConversionBuilder returnObject(Object realReturnType) {
            this.realReturnType = realReturnType;
            return this;
        }

        public BeanConversionBuilder declaredReturnType(Type declaredReturnType) {
            this.declaredReturnType = declaredReturnType;
            return this;
        }


        public BeanConversion build() {
            return new BeanConversion(realReturnType, declaredReturnType);
        }
    }
}
