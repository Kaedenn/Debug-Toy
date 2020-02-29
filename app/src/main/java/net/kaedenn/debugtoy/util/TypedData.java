package net.kaedenn.debugtoy.util;

import android.util.TypedValue;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class TypedData {

    private static TypedValue make(int type, Integer intData) {
        return TypedData.make(type, intData, null);
    }

    private static TypedValue make(int type, String strData) {
        return TypedData.make(type, null, strData);
    }

    private static TypedValue make(int type, Integer intData, String strData) {
        TypedValue tv = new TypedValue();
        tv.type = type;
        if (intData != null && strData != null) {
            tv.data = intData;
            tv.string = strData;
        } else if (intData != null) {
            tv.data = intData;
            tv.string = TypedValue.coerceToString(type, intData);
        } else if (strData != null) {
            tv.string = strData;
        }
        return tv;
    }

    public static TypedValue of(int data) {
        return ofDec(data);
    }

    public static TypedValue ofDec(int data) {
        final int type = TypedValue.TYPE_INT_DEC;
        return TypedData.make(type, data);
    }

    public static TypedValue ofHex(int data) {
        final int type = TypedValue.TYPE_INT_HEX;
        return TypedData.make(type, data);
    }

    public static TypedValue of(float data) {
        final int type = TypedValue.TYPE_FLOAT;
        final int iData = Float.floatToIntBits(data);
        return TypedData.make(type, iData);
    }

    public static TypedValue of(String data) {
        return TypedData.make(TypedValue.TYPE_STRING, data);
    }

}
