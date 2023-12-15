package org.kkoneone.rpc.Filter.client;

import org.kkoneone.rpc.Filter.ClientBeforeFilter;
import org.kkoneone.rpc.Filter.FilterData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author：kkoneone11
 * @name：ClientLogFilter
 * @Date：2023/12/5 17:34
 */
public class ClientLogFilter implements ClientBeforeFilter {

    private Logger logger = LoggerFactory.getLogger(ClientLogFilter.class);


    @Override
    public void doFilter(FilterData filterData) {
        logger.info(filterData.toString());
    }
}
