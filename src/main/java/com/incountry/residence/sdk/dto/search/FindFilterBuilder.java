package com.incountry.residence.sdk.dto.search;

import java.util.List;

/**
 * Builder for cosy creation of FindFilter
 */
public class FindFilterBuilder {

    private static final String OPER_NOT = "not";
    private static final String OPER_GT = "gt";
    private static final String OPER_GTE = "gte";
    private static final String OPER_LT = "lt";
    private static final String OPER_LTE = "lte";

    private FindFilter filter;

    public static FindFilterBuilder create() {
        return new FindFilterBuilder();
    }

    private FindFilterBuilder() {
        filter = new FindFilter();
    }

    public FindFilter build() {
        return filter.clone();
    }

    public FindFilterBuilder clear() {
        filter = new FindFilter();
        return this;
    }

    public FindFilterBuilder limitAndOffset(int limit, int offset) {
        filter.setLimit(limit);
        filter.setOffset(offset);
        return this;
    }

    //key2
    public FindFilterBuilder keyEq(String key) {
        filter.setKeyFilter(new FilterStringParam(key));
        return this;
    }

    public FindFilterBuilder keyIn(List<String> keys) {
        filter.setKeyFilter(new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder keyNotEq(String key) {
        filter.setKeyFilter(new FilterStringParam(key, true));
        return this;
    }

    public FindFilterBuilder keyNotIn(List<String> keys) {
        filter.setKeyFilter(new FilterStringParam(keys, true));
        return this;
    }

    //key2
    public FindFilterBuilder key2Eq(String key) {
        filter.setKey2Filter(new FilterStringParam(key));
        return this;
    }

    public FindFilterBuilder key2In(List<String> keys) {
        filter.setKey2Filter(new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder key2NotEq(String key) {
        filter.setKey2Filter(new FilterStringParam(key, true));
        return this;
    }

    public FindFilterBuilder key2NotIn(List<String> keys) {
        filter.setKey2Filter(new FilterStringParam(keys, true));
        return this;
    }

    //key3
    public FindFilterBuilder key3Eq(String key) {
        filter.setKey3Filter(new FilterStringParam(key));
        return this;
    }

    public FindFilterBuilder key3In(List<String> keys) {
        filter.setKey3Filter(new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder key3NotEq(String key) {
        filter.setKey3Filter(new FilterStringParam(key, true));
        return this;
    }

    public FindFilterBuilder key3NotIn(List<String> keys) {
        filter.setKey3Filter(new FilterStringParam(keys, true));
        return this;
    }

    //profileKey
    public FindFilterBuilder profileKeyEq(String key) {
        filter.setProfileKeyFilter(new FilterStringParam(key));
        return this;
    }

    public FindFilterBuilder profileKeyIn(List<String> keys) {
        filter.setProfileKeyFilter(new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder profileKeyNotEq(String key) {
        filter.setProfileKeyFilter(new FilterStringParam(key, true));
        return this;
    }

    public FindFilterBuilder profileKeyNotIn(List<String> keys) {
        filter.setProfileKeyFilter(new FilterStringParam(keys, true));
        return this;
    }

    //version
    public FindFilterBuilder versionEq(String version) {
        filter.setVersionFilter(new FilterStringParam(version));
        return this;
    }

    public FindFilterBuilder versionIn(List<String> versions) {
        filter.setVersionFilter(new FilterStringParam(versions));
        return this;
    }

    public FindFilterBuilder versionNotEq(String version) {
        filter.setVersionFilter(new FilterStringParam(version, true));
        return this;
    }

    public FindFilterBuilder versionNotIn(List<String> versions) {
        filter.setVersionFilter(new FilterStringParam(versions, true));
        return this;
    }

    //rangeKey
    public FindFilterBuilder rangeKeyEq(int key) {
        filter.setRangeKeyFilter(new FilterRangeParam(key));
        return this;
    }

    public FindFilterBuilder rangeKeyIn(int[] keys) {
        filter.setRangeKeyFilter(new FilterRangeParam(keys));
        return this;
    }

    public FindFilterBuilder rangeKeyNotEq(int key) {
        filter.setRangeKeyFilter(new FilterRangeParam(OPER_NOT, key));
        return this;
    }

    public FindFilterBuilder rangeKeyNotIn(int[] keys) {
        filter.setRangeKeyFilter(new FilterRangeParam(OPER_NOT, keys));
        return this;
    }

    public FindFilterBuilder rangeKeyGT(int key) {
        filter.setRangeKeyFilter(new FilterRangeParam(OPER_GT, key));
        return this;
    }

    public FindFilterBuilder rangeKeyGTE(int key) {
        filter.setRangeKeyFilter(new FilterRangeParam(OPER_GTE, key));
        return this;
    }

    public FindFilterBuilder rangeKeyLT(int key) {
        filter.setRangeKeyFilter(new FilterRangeParam(OPER_LT, key));
        return this;
    }

    public FindFilterBuilder rangeKeyLTE(int key) {
        filter.setRangeKeyFilter(new FilterRangeParam(OPER_LTE, key));
        return this;
    }

    @Override
    public String toString() {
        return "FindFilterBuilder{" +
                "filter=" + filter +
                '}';
    }
}
