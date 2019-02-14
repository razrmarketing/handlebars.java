package com.razr.handlebars.helpers;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

public enum PhoneHelpers implements Helper<Object> {

    /**
     * <p>
     * Usage:
     * </p>
     *
     * <pre>
     *    {{formatPhone phone}}
     * </pre>
     *
     * Formats a phone number into # (###) ###-#### format
     */
    formatPhone {

        @Override
        protected CharSequence safeApply(final Object value, final Options options) {
            isTrue(value instanceof String, "found '%s', expected a String type", value);

            String unformattedPhone = (String) value;
            String phone = unformattedPhone.toLowerCase();
            int extensionIndex = phone.indexOf("x");
            String extension = "";
            if (extensionIndex > -1) {
                //Everything after the x
                extension = unformattedPhone.substring(extensionIndex + 1);
                //Everything before the x
                phone = unformattedPhone.substring(0, extensionIndex);
            }
            String cleanExtension = extension.replaceAll("[^0-9]", "");
            String cleanPhone = phone.replaceAll("[^0-9]", "");

            StringBuilder formattedPhone = new StringBuilder();

            switch (cleanPhone.length()) {
                case 7:
                    formattedPhone.append(cleanPhone.substring(0, 3)).append("-").append(cleanPhone.substring(3, 7));
                    break;
                case 10:
                    formattedPhone.append("(").append(cleanPhone.substring(0, 3)).append(") ")
                            .append(cleanPhone.substring(3, 6))
                            .append("-")
                            .append(cleanPhone.substring(6, 10));
                    break;
                case 11:
                    formattedPhone.append("+").append(cleanPhone.substring(0, 1))
                            .append(" (").append(cleanPhone.substring(1, 4)).append(") ")
                            .append(cleanPhone.substring(4, 7))
                            .append("-")
                            .append(cleanPhone.substring(7, 11));
                    break;
                default: //Unrecognized number style, so don't apply formatting
                    return unformattedPhone;
            }

            if (StringUtils.isNotEmpty(cleanExtension)) {
                formattedPhone.append(" x").append(cleanExtension);
            }
            return formattedPhone.toString();

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
        PhoneHelpers[] helpers = values();
        for (PhoneHelpers helper : helpers) {
            helper.registerHelper(handlebars);
        }
    }

}
