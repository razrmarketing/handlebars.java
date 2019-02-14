package com.razr.handlebars.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Options;

import java.io.IOException;

public class IfEqualsHelpers  {

    /**
     * <p>
     * Usage:
     * </p>
     *
     * <pre>
     *    {{ifEquals val1 val2 [type]}}
     * </pre>
     *
     * type parameters is one of:
     * <ul>
     * <li>"long"</li>
     * <li>"int"</li>
     * <li>"boolean"</li>
     * <li>"string"</li>
     * </ul>
     *
     * <p>If no type is specified, it will match against each values native type.</p>
     * <p>Renders the content within if val1 and val2 are equal</p>
     */
    public CharSequence ifEquals(final Object val1, final Options options) throws IOException {

        Object param1;
        Object param2;
        String type = options.param(1, null);
        if (type != null) {
            param1 = convertToType(val1, type);
            param2 = convertToType(options.param(0), type);
        } else {
            param1 = val1;
            param2 = options.param(0);
        }


        if ((param1 == null && param2 == null) ||
                (param1 != null && param1.equals(param2))) {
            return options.fn(Context.newContext(options.context, param1));
        }
        return options.inverse(Context.newContext(options.context, param1));

    }

    private Object convertToType(Object param, String type) {

        if (type.equalsIgnoreCase("long") && !(param instanceof Long)) {
            try {
                return Long.valueOf(param.toString());
            } catch (Exception e) {
                //Ignore failure and return the input
            }
        } else if (type.equalsIgnoreCase("int") && !(param instanceof Integer)) {
            try {
                return Integer.valueOf(param.toString());
            } catch (Exception e) {
                //Ignore failure and return the input
            }
        } else if (type.equalsIgnoreCase("string") && !(param instanceof String)) {
            try {
                return param.toString();
            } catch (Exception e) {
                //Ignore failure and return the input
            }
        } else if (type.equalsIgnoreCase("boolean") && !(param instanceof Boolean)) {
            try {
                return Boolean.valueOf(param.toString());
            } catch (Exception e) {
                //Ignore failure and return the input
            }
        }
        return param;
    }

}
