package com.android.util.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConcurrentCrossList<KEY, VALUE> {

    private ConcurrentHashMap<KEY, VALUE> mapCache = new ConcurrentHashMap<KEY, VALUE>();
    private CopyOnWriteArrayList<VALUE>        listCache   = new CopyOnWriteArrayList<VALUE>();
     
    /**
     * 添加元素，存在则不添加，不存在则添加
     * @param key
     * @param value
     * @return
     */
    public boolean add(KEY key, VALUE value) {
        if(key == null || value == null) return false;
        if(!mapCache.containsKey(key)) {
            mapCache.put(key, value);
            return listCache.add(value);
        }
        return false;
    }
    
    /**
     * 更新元素，存在则更新，不存在则添加
     * @param key
     * @param value
     * @return
     */
    public boolean update(KEY key, VALUE value) {
        if(key == null || value == null) return false;
        if(!mapCache.containsKey(key)) {
            mapCache.put(key, value);
            return listCache.add(value);
        }else {
            listCache.remove(mapCache.put(key, value));
            return listCache.add(value);
        }
    }
    
    /**
     * 删除元素
     * @param key
     * @return
     */
    public boolean del(KEY key) {
        if(key == null) return false;
        if(mapCache.containsKey(key)) {
            return listCache.remove(mapCache.remove(key));
        }
        return false;
    }
    
    /**
     * 获取元素
     * @param key
     * @return
     */
    public VALUE get(KEY key) {
        if( key == null) return null;
        return mapCache.get(key);
    }
    
    /**
     * 获取大小
     * @return
     */
    public int size() {
        return listCache.size();
    }
    
    /**由位置获取值
     * @param index
     * @return
     */
    public VALUE getIndex(int index) {
        if(index < 0 || index >= size()) return null;
        return listCache.get(index);
    }
    
    /**
     * 是否存在
     * @param key
     * @return
     */
    public boolean isExists(KEY key) {
        if( key == null) return false;
        return mapCache.containsKey(key);
    }
    
    /**
     * 获取VALUE的列表
     * @return
     */
    public List<VALUE> getList(){
        int size = size();
        List<VALUE> list = new ArrayList<VALUE>(size);
        for(int i = 0; i < size; i++) {
            list.add(listCache.get(i));
        }
        return list;
    }
        
    /**
     * 按照指定排序来排序元素顺序，并更新元素列表顺序
     * @param comparator
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<VALUE> sortList(Comparator comparator){
        List<VALUE> list = getList();
        if(list != null && list.size() > 0) {
            Collections.sort(list, comparator);
            listCache.clear();
            listCache.addAll(list);
        }
        return list;
    }
    
    /**
     * 获取KEY列表
     * @return
     */
    public List<KEY> getKeyList(){
        if(!mapCache.isEmpty()) {
            Enumeration<KEY> itor = mapCache.keys();
            List<KEY> list = new ArrayList<KEY>();
            while (itor.hasMoreElements()) {
                list.add(itor.nextElement());
            }
            return list;
        }
        return null;
    }
    
    /**
     * 清除所有元素
     */
    public void clear() {
        mapCache.clear();
        listCache.clear();
    }
    
}
