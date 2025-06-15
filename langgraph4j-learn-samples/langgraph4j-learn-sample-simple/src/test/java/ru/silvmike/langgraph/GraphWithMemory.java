package ru.silvmike.langgraph;

import org.assertj.core.api.Assertions;
import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.state.AgentStateFactory;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

public class GraphWithMemory {

    private static final int ITERATIONS = 2;

    private interface Nodes {
        String START = StateGraph.START;
        String ADD_RANDOM = "add_random";
    }

    private CompiledGraph<RandomsState> createSimpleRouted() throws GraphStateException {

        Random random = new Random();
        AgentStateFactory<RandomsState> stateFactory = RandomsState::new;
        MemorySaver memorySaver = new MemorySaver();
        CompileConfig compileConfig = CompileConfig.builder()
            .checkpointSaver(memorySaver)
            .build();

        return new StateGraph<>(RandomsState.SCHEMA, stateFactory)
            .addNode(
                Nodes.ADD_RANDOM,
                node_async(
                    state -> Map.of(RandomsState.RANDOMS_KEY, random.nextInt())
                )
            )
            .addEdge(Nodes.START, Nodes.ADD_RANDOM)
            .addEdge(Nodes.ADD_RANDOM, Nodes.ADD_RANDOM)
            .compile(compileConfig);
    }

    @Test
    public void testMemory() throws GraphStateException {
        CompiledGraph<RandomsState> graph = new GraphWithMemory().createSimpleRouted();

        RunnableConfig config = RunnableConfig.builder()
            .threadId("1")
            .build();

        for (int i = 0; i < ITERATIONS; i++) {
            graph
                .stream(Map.of(), config)
                .stream().findFirst().ifPresent(System.out::println);
        }

        Assertions.assertThat(
            graph
                .stream(Map.of(), config)
                .stream().findFirst().get().state().randoms()
        ).hasSize(ITERATIONS);
    }
}

