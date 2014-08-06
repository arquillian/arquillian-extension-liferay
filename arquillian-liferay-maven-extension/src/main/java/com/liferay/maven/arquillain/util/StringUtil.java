/*
 * *
 *  * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *  *
 *  * This library is free software; you can redistribute it and/or modify it under
 *  * the terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *
 */

package com.liferay.maven.arquillain.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kameshs on 05/08/14.
 */
public class StringUtil {

    public static final StringUtil INSTANCE = new StringUtil();

    private static final Logger log =
        LoggerFactory.getLogger(StringUtil.class);

    public Map<String, String> expandValues(
        Map<String, String> sourceMap, final Map<String, String> lookupValues) {
        Map<String, String> expanededValuesMap = new HashMap<String, String>();
        for (String key : sourceMap.keySet()) {

            StrSubstitutor strSubstitutor =
                new StrSubstitutor(new StrLookup<String>() {

                    @Override
                    public String lookup(String key) {
                        return lookupValues.get(key);
                    }
                });

            String expandedVal = strSubstitutor.replace(sourceMap.get(key));

            log.debug("Source:" + key + " will be expaned to :" + expandedVal);

            expanededValuesMap.put(key, expandedVal);
        }
        return expanededValuesMap;
    }

}
