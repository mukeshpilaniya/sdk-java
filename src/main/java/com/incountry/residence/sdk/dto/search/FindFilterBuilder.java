package com.incountry.residence.sdk.dto.search;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Builder for cosy creation of FindFilter
 */
public class FindFilterBuilder {

    private static final Logger LOG = LogManager.getLogger(FindFilterBuilder.class);

    private static final String MSG_ERR_KEY1_KEY10_AND_SEARCH_KEYS = "SEARCH_KEYS cannot be used in conjunction with regular KEY1...KEY10 lookup";
    private static final String MSG_ERR_SEARCH_KEYS_LEN = "SEARCH_KEYS must be more than 3 and less then 200";
    private static final String MSG_ERR_SEARCH_KEYS_ADD = "SEARCH_KEYS can be used only via searchKeysLike method";

    public static final String OPER_NOT = "$not";
    public static final String OPER_GT = "$gt";
    public static final String OPER_GTE = "$gte";
    public static final String OPER_LT = "$lt";
    public static final String OPER_LTE = "$lte";

    private FindFilter filter;
    private static List<StringField> keys = new ArrayList<>();

    static {
        keys.add(StringField.KEY1);
        keys.add(StringField.KEY2);
        keys.add(StringField.KEY3);
        keys.add(StringField.KEY4);
        keys.add(StringField.KEY5);
        keys.add(StringField.KEY6);
        keys.add(StringField.KEY7);
        keys.add(StringField.KEY8);
        keys.add(StringField.KEY9);
        keys.add(StringField.KEY10);
    }

    public static FindFilterBuilder create() {
        return new FindFilterBuilder();
    }

    private FindFilterBuilder() {
        filter = new FindFilter();
    }

    private FindFilterBuilder(FindFilter filter) {
        this.filter = filter;
    }

    public FindFilter build() throws StorageClientException {
        return filter.copy();
    }

    public FindFilterBuilder clear() {
        filter = new FindFilter();
        return this;
    }

    public FindFilterBuilder limitAndOffset(int limit, int offset) throws StorageClientException {
        filter.setLimit(limit);
        filter.setOffset(offset);
        return this;
    }

    public FindFilterBuilder keyEq(StringField field, String... keys) throws StorageClientException {
        validateStringFilters(field);
        filter.setStringFilter(field, new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder keyEq(NumberField field, Long... keys) throws StorageClientException {
        filter.setNumberFilter(field, new FilterNumberParam(keys));
        return this;
    }

    public FindFilterBuilder keyNotEq(StringField field, String... keys) throws StorageClientException {
        validateStringFilters(field);
        filter.setStringFilter(field, new FilterStringParam(keys, true));
        return this;
    }

    public FindFilterBuilder keyGT(NumberField field, long key) throws StorageClientException {
        filter.setNumberFilter(field, new FilterNumberParam(OPER_GT, key));
        return this;
    }

    public FindFilterBuilder keyGTE(NumberField field, long key) throws StorageClientException {
        filter.setNumberFilter(field, new FilterNumberParam(OPER_GTE, key));
        return this;
    }

    public FindFilterBuilder keyLT(NumberField field, long key) throws StorageClientException {
        filter.setNumberFilter(field, new FilterNumberParam(OPER_LT, key));
        return this;
    }

    public FindFilterBuilder keyLTE(NumberField field, long key) throws StorageClientException {
        filter.setNumberFilter(field, new FilterNumberParam(OPER_LTE, key));
        return this;
    }

    public FindFilterBuilder keyBetween(NumberField field, long fromValue, long toValue) throws StorageClientException {
        return keyBetween(field, fromValue, true, toValue, true);
    }

    public FindFilterBuilder keyBetween(NumberField field, long fromValue, boolean includeFrom, long toValue, boolean includeTo) throws StorageClientException {
        filter.setNumberFilter(field, new FilterNumberParam(includeFrom ? OPER_GTE : OPER_GT,
                fromValue,
                includeTo ? OPER_LTE : OPER_LT,
                toValue));
        return this;
    }

    public FindFilterBuilder searchKeysLike(String value) throws StorageClientException {
        Set<StringField> searchKeys = filter.getStringFilterMap().keySet();
        for (StringField key : keys) {
            if (searchKeys.contains(key)) {
                LOG.error(MSG_ERR_KEY1_KEY10_AND_SEARCH_KEYS);
                throw new StorageClientException(MSG_ERR_KEY1_KEY10_AND_SEARCH_KEYS);
            }
        }
        if (value.length() < 3 || value.length() > 200) {
            LOG.error(MSG_ERR_SEARCH_KEYS_LEN);
            throw new StorageClientException(MSG_ERR_SEARCH_KEYS_LEN);
        }
        filter.setStringFilter(StringField.SEARCH_KEYS, new FilterStringParam(new String[] {value}));
        return this;
    }

    @Override
    public String toString() {
        return "FindFilterBuilder{" +
                "filter=" + filter +
                '}';
    }

    public FindFilterBuilder copy() throws StorageClientException {
        return new FindFilterBuilder(this.filter.copy());
    }

    private void validateStringFilters(StringField field) throws StorageClientException {
        if (field == StringField.SEARCH_KEYS) {
            LOG.error(MSG_ERR_SEARCH_KEYS_ADD);
            throw new StorageClientException(MSG_ERR_SEARCH_KEYS_ADD);
        }
        if (keys.contains(field) && filter.getStringFilterMap().keySet().contains(StringField.SEARCH_KEYS)) {
            LOG.error(MSG_ERR_KEY1_KEY10_AND_SEARCH_KEYS);
            throw new StorageClientException(MSG_ERR_KEY1_KEY10_AND_SEARCH_KEYS);
        }
    }
}
