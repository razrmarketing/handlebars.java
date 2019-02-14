package com.razr.handlebars.helpers;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;
import java.util.Objects;

public class MerchandiseSortCriteria implements Comparable {

    private String supplierId = null;
    private String id = null;
    private String itemId = null;
    private String parentItemId = null;

    MerchandiseSortCriteria(Map userGoods) {
        if (userGoods.containsKey("OrderDetails") && userGoods.get("OrderDetails") instanceof Map) {
            Map orderDetails = (Map) userGoods.get("OrderDetails");

            //Types are a bit inconsistent, so make no assumptions about input data
            this.supplierId = stringify(orderDetails.get("supplierid"));
            this.id = stringify(userGoods.get("Id"));
            this.itemId = stringify(orderDetails.get("itemid"));
            this.parentItemId = stringify(orderDetails.get("parentitemid"));
        }
    }

    private static String stringify(Object value) {
        return (value == null) ? null : value.toString();
    }

    public String getSupplierId() {
        return supplierId;
    }

    public String getId() {
        return id;
    }

    public String getItemId() {
        return itemId;
    }

    public String getParentItemId() {
        return parentItemId;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public int compareTo(Object o) {

        MerchandiseSortCriteria that = (MerchandiseSortCriteria) o;

        if (this.equals(that)) {
            return 0;
        }

        // First sort by supplier id
        if (!Objects.equals(this.getSupplierId(), that.getSupplierId())) {
            return this.getSupplierId().compareTo(that.supplierId);
        }

        // Next sort by the main item id
        String thisMainItemId = this.parentItemId == null ? this.itemId : this.parentItemId;
        String thatMainItemId = that.parentItemId == null ? that.itemId : that.parentItemId;
        if (!Objects.equals(thisMainItemId, thatMainItemId)) {
            return thisMainItemId.compareTo(thatMainItemId);
        }

        // Finally, sort the child after the parent
        if (this.parentItemId == null && that.parentItemId != null) {
            return -1;
        } else if (this.parentItemId != null && that.parentItemId != null) {
            return this.id.compareTo(that.id);
        }

        return 1;

    }
}
