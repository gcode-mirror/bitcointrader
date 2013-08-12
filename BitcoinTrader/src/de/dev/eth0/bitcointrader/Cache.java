//$URL$
//$Id$
package de.dev.eth0.bitcointrader;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author deveth0
 */
public class Cache {

  private static final long DEFAULT_LIFETIME = 15 * 60 * 1000; // 15 minutes
  private Map<Class, CacheEntry> cache;

  public Cache() {
    cache = new HashMap<Class, CacheEntry>();
  }

  public void put(Object object) {
    put(object, DEFAULT_LIFETIME);
  }

  public void put(Object object, long lifetime) {
    if (object != null) {
      cache.put(object.getClass(), new CacheEntry(lifetime, object));
    }
  }

  public <T> T getEntry(Class<T> clazz) {
    if (cache.containsKey(clazz)) {
      CacheEntry entry = cache.get(clazz);
      if (entry.isValid()) {
        return (T) entry.getContent();
      }
    }
    return null;
  }

  public class CacheEntry {

    private final long lifetime;
    private final long created;
    private final Object content;

    public CacheEntry(long lifetime, Object content) {
      this.lifetime = lifetime;
      this.content = content;
      this.created = new Date().getTime();
    }

    public long getLifetime() {
      return lifetime;
    }

    public Object getContent() {
      return content;
    }

    public boolean isValid() {
      return (new Date().getTime() - created) < lifetime;
    }
  }
}
