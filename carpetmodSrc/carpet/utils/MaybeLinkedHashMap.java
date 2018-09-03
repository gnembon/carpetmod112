package carpet.utils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Class for easy togglability between hash map being linked or not
// When fixes are implemeted in vanilla, the permanent fix should choose one or the other.
public class MaybeLinkedHashMap<K, V> implements Map<K, V>
{

    private HashMap<K, V> delegate = new HashMap<>();
    private LinkedHashSet<K> order = new LinkedHashSet<>();
    
    @Override
    public void clear()
    {
        delegate.clear();
        order.clear();
    }
    
    @Override
    public boolean containsKey(Object k)
    {
        return delegate.containsKey(k);
    }
    
    @Override
    public boolean containsValue(Object v)
    {
        return delegate.containsValue(v);
    }
    
    @Override
    public Set<Map.Entry<K, V>> entrySet()
    {
        return delegate.entrySet();
    }
    
    public Set<Map.Entry<K, V>> entrySetOrdered()
    {
        LinkedHashSet<Map.Entry<K, V>> set = new LinkedHashSet<>(size());
        for (K k : order)
            set.add(new AbstractMap.SimpleEntry<>(k, delegate.get(k)));
        return set;
    }
    
    @Override
    public V get(Object key)
    {
        return delegate.get(key);
    }
    
    @Override
    public boolean isEmpty()
    {
        return delegate.isEmpty();
    }
    
    @Override
    public Set<K> keySet()
    {
        return delegate.keySet();
    }
    
    public Set<K> keySetOrdered()
    {
        return order;
    }
    
    @Override
    public V put(K key, V value)
    {
        order.add(key);
        return delegate.put(key, value);
    }
    
    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {
        m.forEach(this::put);
    }
    
    @Override
    public V remove(Object key)
    {
        order.remove(key);
        return delegate.remove(key);
    }
    
    @Override
    public int size()
    {
        return delegate.size();
    }
    
    @Override
    public Collection<V> values()
    {
        return delegate.values();
    }
    
    public Collection<V> valuesOrdered()
    {
        List<V> values = new ArrayList<>(size());
        for (K k : order)
            values.add(delegate.get(k));
        return values;
    }
    
}
