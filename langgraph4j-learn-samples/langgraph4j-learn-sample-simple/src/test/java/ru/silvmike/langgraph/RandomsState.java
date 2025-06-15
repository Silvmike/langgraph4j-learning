package ru.silvmike.langgraph;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RandomsState extends AgentState {
    public static final String RANDOMS_KEY = "randoms";

    public static final Map<String, Channel<?>> SCHEMA = Map.of(
        RANDOMS_KEY, Channels.appender(ArrayList::new)
    );

    public RandomsState(Map<String, Object> initData) {
        super(initData);
    }

    public List<String> randoms() {
        return this.<List<String>>value(RANDOMS_KEY)
                .orElse( List.of() );
    }
}