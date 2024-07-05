package test;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class TopicManagerSingleton {

    public static class TopicManager {

        private static ConcurrentHashMap<String, Topic> topicMap = new ConcurrentHashMap<>();

        public static Topic getTopic(String name) {
            return topicMap.computeIfAbsent(name, Topic::new);
        }

        public static Collection<Topic> getTopics() {
            return topicMap.values();
        }

        public static void clear() {
            topicMap.clear();
        }
    }

    private final static TopicManager instance = new TopicManager();
    
    public static TopicManager get(){
        return instance;
    }
}