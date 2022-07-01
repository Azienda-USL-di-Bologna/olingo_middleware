package it.nextsw.olingo.edmextension.util;

import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JavaOlingoTypeConverter {

    protected static Map<Class<?>,EdmSimpleTypeKind> convertMap;

    static {
        convertMap=new HashMap<>(20);
        convertMap.put(Boolean.class,EdmSimpleTypeKind.Boolean);
        convertMap.put(boolean.class,EdmSimpleTypeKind.Boolean);
        convertMap.put(Byte.class,EdmSimpleTypeKind.Byte);
        convertMap.put(byte.class,EdmSimpleTypeKind.Byte);
        convertMap.put(Date.class,EdmSimpleTypeKind.DateTime);
        convertMap.put(Float.class,EdmSimpleTypeKind.Decimal);
        convertMap.put(float.class,EdmSimpleTypeKind.Decimal);
        convertMap.put(Double.class,EdmSimpleTypeKind.Double);
        convertMap.put(double.class,EdmSimpleTypeKind.Double);
        convertMap.put(Short.class,EdmSimpleTypeKind.Int16);
        convertMap.put(short.class,EdmSimpleTypeKind.Int16);
        convertMap.put(Integer.class,EdmSimpleTypeKind.Int32);
        convertMap.put(int.class,EdmSimpleTypeKind.Int32);
        convertMap.put(Long.class,EdmSimpleTypeKind.Int64);
        convertMap.put(long.class,EdmSimpleTypeKind.Int64);
        convertMap.put(Short.class,EdmSimpleTypeKind.Int16);
        convertMap.put(short.class,EdmSimpleTypeKind.Int16);
        convertMap.put(UUID.class,EdmSimpleTypeKind.Guid);
        convertMap.put(Object.class,EdmSimpleTypeKind.Binary);
        convertMap.put(String.class,EdmSimpleTypeKind.String);
    }


    public static EdmSimpleTypeKind convert(Object object){
        EdmSimpleTypeKind edmSimpleTypeKind=convertMap.get(object);
        if (edmSimpleTypeKind==null)
            throw new IllegalStateException("No converter register for object class "+object.getClass());
        return edmSimpleTypeKind;
    }

}
