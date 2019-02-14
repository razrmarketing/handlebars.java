package com.razr.handlebars.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IfInListHelpers {

    /**
     * <p>
     * Usage:
     * </p>
     *
     * <pre>
     *    {{ifInList val1 "a,b,c" [delimiter]}}
     * </pre>
     *
     * <pre>
     *    {{ifInList val1 nativeList}}
     * </pre>
     *
     * <p>If the first argument matches any of the items in the second argument it will evaluate to true.</p>
     * <p>The second argument supports either a delimited list or a native list, which is useful when referencing
     * another data element.</p>
     * <p>A third argument is optional, which allows you to specify a custom delimiter. A comma is the default.</p>
     */
    public CharSequence ifInList(final Object val1, final Options options) throws IOException {

        Boolean matches = false;
        String subject = ((String) val1).trim();

        List<String> allowedValues = parseAllowedValues(options);

        for (String allowedValue : allowedValues) {
            if (subject.equals(allowedValue.trim())) {
                matches = true;
                break;
            }
        }

        if (matches) {
            return options.fn(Context.newContext(options.context, val1));
        }
        return options.inverse(Context.newContext(options.context, val1));

    }

    private List<String> parseAllowedValues(Options options) {

        if (options.params.length == 0 || options.param(0) == null) {
            return new ArrayList<>();
        }

        //Already a native List, so use as-is
        if (options.param(0) instanceof List) {
            return options.param(0);
        }

        //Parse the string to determine the list.
        String delimiter = (options.params.length > 1) ? (String) options.param(1) : ",";
        String matchListString = options.param(0).toString().trim();
        if (matchListString.startsWith("[") && matchListString.endsWith("]")) {
            matchListString = matchListString.substring(1, matchListString.length()-1).trim();
        }

        //Parse the string to get elements of the list of possible matches
        String[] tokens = matchListString.split(delimiter, -1);
        return Arrays.asList(tokens);

    }

}
