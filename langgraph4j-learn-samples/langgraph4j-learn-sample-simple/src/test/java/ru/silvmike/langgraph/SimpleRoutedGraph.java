package ru.silvmike.langgraph;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.AgentStateFactory;

import java.util.Map;
import java.util.Scanner;

import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

public class SimpleRoutedGraph {

    private interface Nodes {
        String START = StateGraph.START;
        String ASK_USER = "ask_user";
        String PRINT_USER_REPLY = "print_repoly";
        String END = StateGraph.END;
    }

    private static final String USER_REPLY_STATE_FIELD = "user_reply";

    public CompiledGraph<AgentState> createSimpleRouted() throws GraphStateException {

        AgentStateFactory<AgentState> stateFactory = AgentState::new;

        return new StateGraph<>(stateFactory)
            .addNode(
                Nodes.ASK_USER,
                node_async(
                    state -> {
                        System.out.println("User input: ");
                        String reply = new Scanner(System.in).nextLine();
                        return Map.of(USER_REPLY_STATE_FIELD, reply);
                    }
                )
            )
            .addNode(
                Nodes.PRINT_USER_REPLY,
                node_async(
                    state -> {
                        state.value(USER_REPLY_STATE_FIELD).ifPresent(System.out::println);
                        return Map.of();
                    }
                )
            )
            .addEdge(Nodes.START, Nodes.ASK_USER)
            .addEdge(Nodes.PRINT_USER_REPLY, Nodes.ASK_USER)
            .addConditionalEdges(
                Nodes.ASK_USER,
                edge_async(state -> {
                    var exitCondition = state.value(USER_REPLY_STATE_FIELD).filter(
                            value -> ":q".equalsIgnoreCase((String) value)
                    ).isPresent();
                    if (exitCondition) {
                        return Nodes.END;
                    } else {
                        return Nodes.PRINT_USER_REPLY;
                    }
                }),
                Map.of(
                    Nodes.PRINT_USER_REPLY, Nodes.PRINT_USER_REPLY,
                    Nodes.END, Nodes.END
                )
            )
            .compile();
    }

    public static void main(String[] args) throws GraphStateException {
        new SimpleRoutedGraph().createSimpleRouted()
            .invoke(Map.of())
            .ifPresent((state) -> System.exit(0));
    }
}

