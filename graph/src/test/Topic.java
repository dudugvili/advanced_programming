package test;

import java.util.ArrayList;
import java.util.List;
import test.Agent;
import test.Agent;
import test.Message;


public class Topic {
    public final String name;
    public ArrayList<Agent> subscribers = new ArrayList<Agent>();
    public ArrayList<Agent> publishers = new ArrayList<Agent>();
    
    Topic(String name){
        this.name=name;
    }

    public void subscribe(Agent a){
        subscribers.add(a);
    }

    public void unsubscribe(Agent a){
        subscribers.remove(a);
    }

    public void publish(Message m){
        subscribers.forEach(p->p.callback(name,m));
    }

    public void addPublisher(Agent a){
        publishers.add(a);
    }

    public void removePublisher(Agent a){
        publishers.remove(a);
    }
}


