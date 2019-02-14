package com.razr.handlebars.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

public enum SortHelpers implements Helper<Object> {

    /**
     * <p>
     * Usage:
     * </p>
     *
     * <pre>
     *    {{eachMerchandise listOfUserGoods}}
     * </pre>
     *
     * The purpose of this helper is to sort merchandise user goods in a way
     * that makes sense -- mainly putting protection plan items immediately after
     * their parent item.
     */
    eachMerchandise {

        @Override
        protected CharSequence safeApply(final Object context, final Options options) {
            if (context == null) {
                return StringUtils.EMPTY;
            }
            isTrue(context instanceof List, "Provided context is not a List", context);

            StringBuilder buffer = new StringBuilder();
            List sortedItems = (List) MerchandiseGoodsSorter.sortGoodsList(context);

            Iterator iterator = sortedItems.iterator();
            int index = -1;
            Context parent = options.context;
            while (iterator.hasNext()) {
                index += 1;
                Object element = iterator.next();
                boolean first = index == 0;
                boolean even = index % 2 == 0;
                boolean last = !iterator.hasNext();
                Context current = Context.newBuilder(parent, element)
                        .combine("@index", index)
                        .combine("@first", first ? "first" : "")
                        .combine("@last", last ? "last" : "")
                        .combine("@odd", even ? "" : "odd")
                        .combine("@even", even ? "even" : "")
                                // 1-based index
                        .combine("@index_1", index + 1)
                        .build();
                try {
                    buffer.append(options.fn(current));
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
                current.destroy();
            }

            return buffer.toString();
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
        SortHelpers[] helpers = values();
        for (SortHelpers helper : helpers) {
            helper.registerHelper(handlebars);
        }
    }

}
