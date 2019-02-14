package com.razr.handlebars.helpers;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.apache.commons.lang3.LocaleUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

public enum DateHelpers implements Helper<Object> {

    /**
     * <p>
     * Usage:
     * </p>
     *
     * <pre>
     *    {{dateFormat date ["outputFormat"] [inputFormat="inputFormat"][outputFormat="format"][tz=timeZone|timeZoneId]}}
     * </pre>
     *
     * Output Format parameters is one of:
     * <ul>
     * <li>"full": full date format. For example: Tuesday, June 19, 2012</li>
     * <li>"long": long date format. For example: June 19, 2012</li>
     * <li>"medium": medium date format. For example: Jun 19, 2012</li>
     * <li>"short": short date format. For example: 6/19/12</li>
     * <li>"pattern": a date pattern.</li>
     * </ul>
     * Otherwise, the default formatter will be used.
     *
     * Input Format parameters is SimpleDateFormat pattern to be used for parsing the date string.
     * It is optional, and by default will handle parsing ISO 8601 dates.
     *
     * The format option can be specified as a parameter or hash (a.k.a named parameter).
     */
    stringDateFormat {
        /**
         * The default date styles.
         */
        @SuppressWarnings("serial")
        private Map<String, Integer> styles = new HashMap<String, Integer>()
        {
            {
                put("full", DateFormat.FULL);
                put("long", DateFormat.LONG);
                put("medium", DateFormat.MEDIUM);
                put("short", DateFormat.SHORT);
            }
        };

        @Override
        protected CharSequence safeApply(final Object value, final Options options) {
            isTrue(value instanceof String, "found '%s', expected a String type", value);

            //Convert date string to a Date object
            String inputPattern = (String) options.param(1, options.hash("inputFormat", null));
            DateTimeFormatter inputFormatter;
            if (inputPattern != null) {
                inputFormatter = DateTimeFormat.forPattern(inputPattern);
            } else {
                inputFormatter = ISODateTimeFormat.dateTimeParser();
            }
            String dateTimeString = (String) value;
            DateTime dateTime = inputFormatter.parseDateTime(dateTimeString);
            Date date = dateTime.toDate();

            //Generate the output
            final DateFormat dateFormat;
            Object outputPattern = options.param(0, options.hash("format", "medium"));
            String localeStr = options.param(2, Locale.getDefault().toString());
            Locale locale = LocaleUtils.toLocale(localeStr);
            Integer style = styles.get(outputPattern);

            if (style == null) {
                dateFormat = new SimpleDateFormat(outputPattern.toString(), locale);
            } else {
                dateFormat = DateFormat.getDateInstance(style, locale);
            }
            Object tz = options.hash("tz");
            if (tz != null) {
                final TimeZone timeZone = tz instanceof TimeZone ? (TimeZone) tz : TimeZone.getTimeZone(tz
                        .toString());
                dateFormat.setTimeZone(timeZone);
            }
            return dateFormat.format(date);
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
        DateHelpers[] helpers = values();
        for (DateHelpers helper : helpers) {
            helper.registerHelper(handlebars);
        }
    }

}
