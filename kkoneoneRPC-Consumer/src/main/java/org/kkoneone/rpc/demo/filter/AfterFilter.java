package org.kkoneone.rpc.demo.filter;

import org.kkoneone.rpc.Filter.ClientAfterFilter;
import org.kkoneone.rpc.Filter.FilterData;

/**
 * @Author：kkoneone11
 * @name：AfterFilter
 * @Date：2023/12/15 22:02
 */
public class AfterFilter implements ClientAfterFilter {
    @Override
    public void doFilter(FilterData filterData) {
        System.out.println("客户端后置处理器启动咯");
    }
}
