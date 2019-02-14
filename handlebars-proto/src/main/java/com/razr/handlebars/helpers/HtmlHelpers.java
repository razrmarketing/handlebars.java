package com.razr.handlebars.helpers;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

public enum HtmlHelpers implements Helper<Object> {

    /**
     * <p>
     * Usage:
     * </p>
     *
     * <pre>
     *    {{htmlLineBreaks plainText}}
     * </pre>
     *
     * <p>Converts newline characters (\\n) in json to a &lt;br/&gt; tag.</p>
     */
    htmlLineBreaks {

        @Override
        protected CharSequence safeApply(final Object value, final Options options) {
            isTrue(value instanceof String, "found '%s', expected a String type", value);
            String inputText = (String) value;
            if (StringUtils.isEmpty(inputText)) {
                return inputText;
            }

            String resultText = inputText.replaceAll("(\\\\r\\\\n|\\\\n)", "<br/>");
            return new Handlebars.SafeString(resultText);
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
        HtmlHelpers[] helpers = values();
        for (HtmlHelpers helper : helpers) {
            helper.registerHelper(handlebars);
        }
    }

}
