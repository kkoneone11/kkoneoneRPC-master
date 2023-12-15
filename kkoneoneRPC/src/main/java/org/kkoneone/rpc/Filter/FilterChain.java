package org.kkoneone.rpc.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * 拦截器链
 * @Author：kkoneone11
 * @name：FilterChain
 * @Date：2023/11/30 20:30
 */
public class FilterChain {

    private List<Filter> filters = new ArrayList<>();

    public void addFilter(Filter filter){
        filters.add(filter);
    }

    public void addFilter(List<Object> filters){
        for(Object filter : filters){
            addFilter((Filter) filter);
        }
    }


    public void doFilter(FilterData data){
        for (Filter filter : filters) {
            filter.doFilter(data);
        }
    }
}
