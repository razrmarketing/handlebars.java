package com.razr.handlebars.helpers;

import com.github.jknack.handlebars.*;

import java.io.*;
import java.util.*;

import static org.apache.commons.lang3.Validate.*;

public enum ArrayHelpers implements Helper<Object>
{

    /**
     * <p>
     * Usage:
     * </p>
     *
     * <pre>
     *    {{getLength array}}
     * </pre>
     *
     * Returns the length of the passed in array
     *
     *     inputArray           || expectedResult
     *     [1, 2, 3, 4]         || "4"
     *     ["1", "a", "c", "d"] || "4"
     *     []                   || ""
     *     ""                   || ""
     *     [1]                  || "1"
     */
    getLength {

        @Override
        protected CharSequence safeApply(final Object value, final Options options) throws IllegalArgumentException {
            try
            {
                isTrue(value instanceof ArrayList, "found '%s', expected an ArrayList type", value);
            }
            catch (IllegalArgumentException e){
                throw new IllegalArgumentException(e);
            }
            ArrayList arrayToEvaluate = (ArrayList) value;

            int arrLength = arrayToEvaluate.size();

            StringBuilder stringLength = new StringBuilder();
            stringLength.append(arrLength);

            return stringLength;
        }

    };

    @Override
    public CharSequence apply(final Object context, final Options options) throws IOException {
        if (options.isFalsy(context)) {
            Object param = options.param(0, null);
            return param == null ? null : param.toString();
        }
        return safeApply(context, options);
    }

    /**
     * Apply the helper to the context.
     *
     * @param context The context object (param=0).
     * @param options The options object.
     * @return A string result.
     */
    protected abstract CharSequence safeApply(final Object context, final Options options);

    /**
     * Register the helper in a handlebars instance.
     *
     * @param handlebars A handlebars object. Required.
     */
    public void registerHelper(final Handlebars handlebars) {
        notNull(handlebars, "The handlebars is required.");
        handlebars.registerHelper(name(), this);
    }

    /**
     * Register all the helpers.
     *
     * @param handlebars The helper's owner. Required.
     */
    public static void register(final Handlebars handlebars) {
        notNull(handlebars, "A handlebars object is required.");
        ArrayHelpers[] helpers = values();
        for (ArrayHelpers helper : helpers) {
            helper.registerHelper(handlebars);
        }
    }

}
